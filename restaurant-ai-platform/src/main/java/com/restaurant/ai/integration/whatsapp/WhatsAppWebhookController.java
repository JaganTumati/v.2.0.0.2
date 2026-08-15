package com.restaurant.ai.integration.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.ai.config.RestaurantProperties;
import com.restaurant.ai.config.WhatsAppProperties;
import com.restaurant.ai.entity.WebhookEvent;
import com.restaurant.ai.repository.WebhookEventRepository;
import com.restaurant.ai.service.MessageProcessingService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Meta requires: (1) a GET verification handshake at setup time, and (2) a
 * fast ack (2xx, quickly) on every POST or it will retry/back off. Real work
 * happens in MessageProcessingService off-thread (Phase 9/10) - nothing
 * expensive runs on this request thread.
 *
 * NOTE: this does NOT yet verify Meta's X-Hub-Signature-256 payload signature
 * (requires a separate WHATSAPP_APP_SECRET not yet in scope). In mock mode
 * this endpoint is exercised via manual/test POSTs rather than real Meta
 * traffic, so it's flagged here rather than silently skipped.
 */
@RestController
@RequestMapping("/api/webhooks/whatsapp")
public class WhatsAppWebhookController {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

    private final WhatsAppProperties whatsAppProperties;
    private final RestaurantProperties restaurantProperties;
    private final WebhookEventRepository webhookEventRepository;
    private final MessageProcessingService messageProcessingService;
    private final ObjectMapper objectMapper;

    public WhatsAppWebhookController(WhatsAppProperties whatsAppProperties,
                                      RestaurantProperties restaurantProperties,
                                      WebhookEventRepository webhookEventRepository,
                                      MessageProcessingService messageProcessingService,
                                      ObjectMapper objectMapper) {
        this.whatsAppProperties = whatsAppProperties;
        this.restaurantProperties = restaurantProperties;
        this.webhookEventRepository = webhookEventRepository;
        this.messageProcessingService = messageProcessingService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge) {

        if ("subscribe".equals(mode) && whatsAppProperties.getVerifyToken() != null
                && whatsAppProperties.getVerifyToken().equals(verifyToken)) {
            return ResponseEntity.ok(challenge);
        }
        log.warn("WhatsApp webhook verification failed");
        return ResponseEntity.status(403).build();
    }

    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody WhatsAppWebhookPayload payload, HttpServletRequest request) {
        if (payload.entry() == null) {
            return ResponseEntity.ok().build();
        }

        for (WhatsAppWebhookPayload.Entry entry : payload.entry()) {
            if (entry.changes() == null) continue;
            for (WhatsAppWebhookPayload.Change change : entry.changes()) {
                if (change.value() == null || change.value().messages() == null) continue;

                String senderName = extractSenderName(change.value());

                for (WhatsAppWebhookPayload.InboundMessage message : change.value().messages()) {
                    handleInboundMessage(message, senderName);
                }
            }
        }

        // Fast ack regardless of processing outcome - failures are handled/logged inside
        // MessageProcessingService, and Meta must not see a non-2xx or it will retry the whole batch.
        return ResponseEntity.ok().build();
    }

    private void handleInboundMessage(WhatsAppWebhookPayload.InboundMessage message, String senderName) {
        if (message.id() == null) {
            log.warn("Inbound WhatsApp message missing id, skipping");
            return;
        }

        // Idempotency: Meta may redeliver the same event on retry.
        if (webhookEventRepository.existsByWaMessageId(message.id())) {
            log.info("Duplicate WhatsApp message id {} - skipping", message.id());
            return;
        }

        WebhookEvent event = new WebhookEvent();
        event.setWaMessageId(message.id());
        event.setPayloadJson(toJson(message));
        event.setStatus("RECEIVED");
        webhookEventRepository.save(event);

        String text = message.text() != null ? message.text().body() : null;
        if (text == null || text.isBlank()) {
            log.info("Ignoring non-text WhatsApp message type '{}' for now", message.type());
            return;
        }

        messageProcessingService.processInboundMessage(
                restaurantProperties.getDefaultId(),
                message.from(),
                senderName,
                text,
                message.id()
        );
    }

    private String extractSenderName(WhatsAppWebhookPayload.Value value) {
        if (value.contacts() == null || value.contacts().isEmpty()) {
            return null;
        }
        var profile = value.contacts().get(0).profile();
        return profile != null ? profile.name() : null;
    }

    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (Exception e) {
            return "{}";
        }
    }
}
