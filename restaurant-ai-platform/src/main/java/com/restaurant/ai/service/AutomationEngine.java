package com.restaurant.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.ai.entity.AutomationRule;
import com.restaurant.ai.entity.Customer;
import com.restaurant.ai.integration.WhatsAppClient;
import com.restaurant.ai.repository.AutomationRuleRepository;
import com.restaurant.ai.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Phase 15. Rules are stored data (AutomationRule), not hardcoded if/else -
 * new automations are added by inserting a row, not shipping code. This
 * engine only supports triggers callers actually fire (see the three
 * call sites: OrderService, LeadService, SupportTicketService) - an
 * intent-based trigger would need a real intent classifier first, which
 * doesn't exist yet, so it's not claimed here.
 *
 * Condition matching is intentionally simple:
 *   - "key": "value"      -> exact match (case-insensitive string compare)
 *   - "min_key": number   -> eventData[key] >= number
 *   - "max_key": number   -> eventData[key] <= number
 * An empty condition matches everything for that trigger type.
 *
 * A rule failing to execute NEVER propagates back to the caller - firing
 * an automation is always best-effort relative to the operation that
 * triggered it (e.g. an order status change must succeed even if a
 * misconfigured follow-up rule would fail).
 */
@Service
public class AutomationEngine {

    private static final Logger log = LoggerFactory.getLogger(AutomationEngine.class);

    private final AutomationRuleRepository automationRuleRepository;
    private final ObjectMapper objectMapper;
    private final CustomerRepository customerRepository;
    private final WhatsAppClient whatsAppClient;
    private final LeadService leadService;
    private final SupportTicketService supportTicketService;
    private final ConversationService conversationService;

    public AutomationEngine(AutomationRuleRepository automationRuleRepository, ObjectMapper objectMapper,
                             CustomerRepository customerRepository, WhatsAppClient whatsAppClient,
                             LeadService leadService, SupportTicketService supportTicketService,
                             ConversationService conversationService) {
        this.automationRuleRepository = automationRuleRepository;
        this.objectMapper = objectMapper;
        this.customerRepository = customerRepository;
        this.whatsAppClient = whatsAppClient;
        this.leadService = leadService;
        this.supportTicketService = supportTicketService;
        this.conversationService = conversationService;
    }

    public void fire(UUID restaurantId, String triggerType, Map<String, Object> eventData) {
        List<AutomationRule> rules = automationRuleRepository
                .findByRestaurantIdAndTriggerTypeAndActiveTrue(restaurantId, triggerType);

        for (AutomationRule rule : rules) {
            try {
                if (matches(readJson(rule.getConditionJson()), eventData)) {
                    execute(rule, restaurantId, eventData);
                }
            } catch (Exception e) {
                log.error("Automation rule '{}' ({}) failed to execute for trigger {} - continuing",
                        rule.getName(), rule.getId(), triggerType, e);
            }
        }
    }

    private boolean matches(Map<String, Object> condition, Map<String, Object> eventData) {
        for (Map.Entry<String, Object> entry : condition.entrySet()) {
            String key = entry.getKey();
            Object expected = entry.getValue();

            if (key.startsWith("min_")) {
                Double actual = toDouble(eventData.get(key.substring(4)));
                Double bound = toDouble(expected);
                if (actual == null || bound == null || actual < bound) return false;
            } else if (key.startsWith("max_")) {
                Double actual = toDouble(eventData.get(key.substring(4)));
                Double bound = toDouble(expected);
                if (actual == null || bound == null || actual > bound) return false;
            } else {
                Object actual = eventData.get(key);
                if (actual == null || expected == null) return false;
                if (!String.valueOf(actual).equalsIgnoreCase(String.valueOf(expected))) return false;
            }
        }
        return true;
    }

    private Double toDouble(Object value) {
        if (value instanceof Number n) return n.doubleValue();
        if (value instanceof String s) {
            try {
                return Double.parseDouble(s);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private void execute(AutomationRule rule, UUID restaurantId, Map<String, Object> eventData) {
        Map<String, Object> config = readJson(rule.getActionConfigJson());

        switch (rule.getActionType()) {
            case "NOTIFY" -> log.info("[AUTOMATION NOTIFY] rule='{}' message='{}' event={}",
                    rule.getName(), config.getOrDefault("message", ""), eventData);

            case "SEND_FOLLOWUP" -> sendFollowup(restaurantId, eventData, config);

            case "CREATE_LEAD" -> leadService.create(
                    restaurantId,
                    (UUID) eventData.get("customerId"),
                    (UUID) eventData.get("conversationId"),
                    (String) config.getOrDefault("source", "AUTOMATION"),
                    (String) config.getOrDefault("notes", "Created by automation rule: " + rule.getName()),
                    null);

            case "CREATE_TICKET" -> supportTicketService.create(
                    restaurantId,
                    (UUID) eventData.get("conversationId"),
                    (UUID) eventData.get("orderId"),
                    (String) config.getOrDefault("issue", "Flagged by automation rule: " + rule.getName()),
                    (String) config.getOrDefault("priority", "NORMAL"));

            case "ESCALATE" -> {
                UUID conversationId = (UUID) eventData.get("conversationId");
                if (conversationId != null) {
                    conversationService.escalateToHuman(conversationId, "Automation rule: " + rule.getName());
                }
            }

            default -> log.warn("Unknown automation action type '{}' on rule '{}'", rule.getActionType(), rule.getName());
        }
    }

    /**
     * Decides text vs. template based on WhatsApp's real 24-hour customer
     * service window, using the conversation's actual last-inbound-message
     * timestamp rather than assuming. Config keys (all optional, but at least
     * one of message/templateName should be set for this action to do anything):
     *   message           - free text, used only when inside the 24h window
     *   templateName       - approved template name, used when outside the window
     *                        (or when there's no conversation/no prior inbound message at all)
     *   templateLanguageCode - defaults to en_US
     *   templateParams      - list of strings; "{{fieldName}}" segments are
     *                         substituted from eventData before sending
     */
    private void sendFollowup(UUID restaurantId, Map<String, Object> eventData, Map<String, Object> config) {
        UUID customerId = (UUID) eventData.get("customerId");
        if (customerId == null) {
            return;
        }
        String toNumber = customerRepository.findById(customerId)
                .filter(c -> c.getRestaurantId().equals(restaurantId))
                .map(Customer::getWhatsappNumber)
                .orElse(null);
        if (toNumber == null) {
            return;
        }

        boolean withinWindow = isWithin24HourWindow(eventData);
        String message = (String) config.get("message");
        String templateName = (String) config.get("templateName");

        if (withinWindow && message != null && !message.isBlank()) {
            whatsAppClient.sendTextMessage(toNumber, message);
        } else if (templateName != null && !templateName.isBlank()) {
            String languageCode = (String) config.getOrDefault("templateLanguageCode", "en_US");
            @SuppressWarnings("unchecked")
            List<String> rawParams = (List<String>) config.getOrDefault("templateParams", List.of());
            List<String> resolvedParams = rawParams.stream().map(p -> substitute(p, eventData)).toList();
            whatsAppClient.sendTemplateMessage(toNumber, templateName, languageCode, resolvedParams);
        } else if (!withinWindow) {
            log.warn("SEND_FOLLOWUP skipped: outside the 24h window and no templateName configured " +
                    "(a plain 'message' would be rejected by WhatsApp here) - customerId={}", customerId);
        }
    }

    private boolean isWithin24HourWindow(Map<String, Object> eventData) {
        Object conversationIdObj = eventData.get("conversationId");
        if (!(conversationIdObj instanceof UUID conversationId)) {
            return false; // no conversation at all -> must use a template
        }
        return conversationService.getLastInboundMessageTime(conversationId)
                .map(last -> last.isAfter(java.time.OffsetDateTime.now().minusHours(24)))
                .orElse(false);
    }

    private String substitute(String template, Map<String, Object> eventData) {
        String result = template;
        for (Map.Entry<String, Object> entry : eventData.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            if (result.contains(placeholder)) {
                result = result.replace(placeholder, String.valueOf(entry.getValue()));
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readJson(String json) {
        try {
            return json == null ? Map.of() : objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            return Map.of();
        }
    }
}
