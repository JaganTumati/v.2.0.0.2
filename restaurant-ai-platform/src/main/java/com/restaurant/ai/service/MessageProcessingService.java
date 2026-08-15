package com.restaurant.ai.service;

import com.restaurant.ai.ai.ChatResult;
import com.restaurant.ai.ai.ConversationTurn;
import com.restaurant.ai.ai.GeminiService;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.entity.Conversation;
import com.restaurant.ai.entity.Customer;
import com.restaurant.ai.integration.WhatsAppClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Wires together the real pipeline described in Phase 10:
 *   inbound message -> conversation -> Gemini (+ tools) -> persist reply -> WhatsApp
 *
 * Runs off the webhook request thread (@Async) so the webhook controller can
 * ack Meta immediately per their fast-acknowledgment requirement. Never lets
 * a failure anywhere in this chain result in silence to the customer - the
 * worst case is an honest "having trouble" message, never a dropped request.
 */
@Service
public class MessageProcessingService {

    private static final Logger log = LoggerFactory.getLogger(MessageProcessingService.class);

    private static final String APOLOGY_FALLBACK =
            "Sorry, I'm having trouble right now. A team member will follow up with you shortly.";

    private final CustomerService customerService;
    private final ConversationService conversationService;
    private final GeminiService geminiService;
    private final WhatsAppClient whatsAppClient;

    public MessageProcessingService(CustomerService customerService, ConversationService conversationService,
                                     GeminiService geminiService, WhatsAppClient whatsAppClient) {
        this.customerService = customerService;
        this.conversationService = conversationService;
        this.geminiService = geminiService;
        this.whatsAppClient = whatsAppClient;
    }

    @Async
    public void processInboundMessage(UUID restaurantId, String fromWhatsappNumber, String customerDisplayName,
                                       String messageText, String waMessageId) {
        try {
            Customer customer = customerService.findOrCreate(restaurantId, fromWhatsappNumber, customerDisplayName);
            Conversation conversation = conversationService.findOrCreateOpenConversation(restaurantId, customer.getId());

            // Always persist the inbound message, regardless of AI/human mode.
            conversationService.appendMessage(conversation.getId(), "INBOUND", "CUSTOMER", messageText, waMessageId);

            if ("HUMAN_ACTIVE".equals(conversation.getMode()) || "HUMAN_PENDING".equals(conversation.getMode())) {
                // Phase 13 rule: the AI must not auto-respond once a human has taken over
                // or a takeover is pending. A human agent (dashboard, not yet built) handles this.
                log.info("Conversation {} is in {} mode - AI reply suppressed", conversation.getId(), conversation.getMode());
                return;
            }

            List<ConversationTurn> history = conversationService.getHistoryWindow(conversation.getId());

            ToolExecutionContext context = new ToolExecutionContext(
                    restaurantId, customer.getId(), conversation.getId(), UUID.randomUUID().toString());

            ChatResult result = geminiService.chat(context, history, messageText);

            conversationService.appendMessage(conversation.getId(), "OUTBOUND", "AI", result.replyText(), null);
            whatsAppClient.sendTextMessage(fromWhatsappNumber, result.replyText());

        } catch (Exception e) {
            log.error("Failed to process inbound message from {} (waMessageId={})", fromWhatsappNumber, waMessageId, e);
            try {
                whatsAppClient.sendTextMessage(fromWhatsappNumber, APOLOGY_FALLBACK);
            } catch (Exception sendFailure) {
                log.error("Also failed to send fallback message to {}", fromWhatsappNumber, sendFailure);
            }
        }
    }
}
