package com.restaurant.ai.service;

import com.restaurant.ai.ai.ConversationTurn;
import com.restaurant.ai.entity.Conversation;
import com.restaurant.ai.entity.Message;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.repository.ConversationRepository;
import com.restaurant.ai.repository.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * Owns Conversation/Message persistence and the bounded history window sent
 * to Gemini each turn (Phase 8). Summarization here is intentionally simple
 * (truncated concatenation) rather than an extra Gemini call, to avoid the
 * service layer depending back on the ai layer - swap regenerateSummary's
 * body for a real summarization call later if conversations get long enough
 * to need it.
 */
@Service
@Transactional
public class ConversationService {

    private static final int HISTORY_WINDOW_SIZE = 20;
    private static final int SUMMARIZE_EVERY_N_MESSAGES = 10;
    private static final int SUMMARY_MAX_CHARS = 800;

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ConversationService(ConversationRepository conversationRepository, MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    /** Continues the customer's most recent open conversation, or starts a new one. */
    public Conversation findOrCreateOpenConversation(UUID restaurantId, UUID customerId) {
        return conversationRepository
                .findFirstByRestaurantIdAndCustomerIdAndModeNotOrderByCreatedAtDesc(restaurantId, customerId, "CLOSED")
                .orElseGet(() -> {
                    Conversation conversation = new Conversation();
                    conversation.setRestaurantId(restaurantId);
                    conversation.setCustomerId(customerId);
                    conversation.setMode("AI_ACTIVE");
                    return conversationRepository.save(conversation);
                });
    }

    public Conversation getOrThrow(UUID restaurantId, UUID conversationId) {
        return conversationRepository.findByIdAndRestaurantId(conversationId, restaurantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Conversation", conversationId));
    }

    public Message appendMessage(UUID conversationId, String direction, String senderType, String content, String waMessageId) {
        return appendMessage(conversationId, direction, senderType, content, waMessageId, null);
    }

    public Message appendMessage(UUID conversationId, String direction, String senderType, String content,
                                  String waMessageId, UUID senderUserId) {
        Message message = new Message();
        message.setConversationId(conversationId);
        message.setDirection(direction);
        message.setSenderType(senderType);
        message.setContent(content);
        message.setWaMessageId(waMessageId);
        message.setSenderUserId(senderUserId);
        messageRepository.save(message);

        maybeRefreshSummary(conversationId);
        return message;
    }

    /** Bounded, chronological history for the AI - most recent HISTORY_WINDOW_SIZE messages. */
    public List<ConversationTurn> getHistoryWindow(UUID conversationId) {
        List<Message> recentDesc = messageRepository.findTop20ByConversationIdOrderByCreatedAtDesc(conversationId);
        List<Message> chronological = new java.util.ArrayList<>(recentDesc);
        Collections.reverse(chronological);
        return chronological.stream()
                .map(m -> new ConversationTurn("AI".equals(m.getSenderType()) ? "model" : "user", m.getContent()))
                .toList();
    }

    /**
     * Timestamp of the customer's most recent inbound message, if any. Used to
     * determine whether a proactive send falls inside WhatsApp's 24-hour
     * customer service window (free-form text allowed) or outside it
     * (a pre-approved template is required instead) - see AutomationEngine.
     */
    public java.util.Optional<java.time.OffsetDateTime> getLastInboundMessageTime(UUID conversationId) {
        return messageRepository.findTopByConversationIdAndDirectionOrderByCreatedAtDesc(conversationId, "INBOUND")
                .map(Message::getCreatedAt);
    }

    public void setMode(UUID conversationId, String mode) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Conversation", conversationId));
        conversation.setMode(mode);
    }

    public void setLastIntent(UUID conversationId, String intent) {
        conversationRepository.findById(conversationId).ifPresent(c -> c.setLastIntent(intent));
    }

    /**
     * Phase 13: hands the conversation to a human. The AI must not auto-respond
     * once this is called (enforced in MessageProcessingService, which checks
     * mode before invoking Gemini). "Notify dashboard" is a log line for now -
     * becomes a real WebSocket/SSE push (Phase 17) once the dashboard exists.
     */
    public void escalateToHuman(UUID conversationId, String reason) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Conversation", conversationId));
        if ("CLOSED".equals(conversation.getMode())) {
            throw new com.restaurant.ai.exception.BusinessRuleViolationException("This conversation is closed.");
        }
        conversation.setMode("HUMAN_PENDING");
        String note = "[Escalated to human] " + (reason == null ? "" : reason);
        conversation.setSummary(conversation.getSummary() == null ? note : conversation.getSummary() + " | " + note);
    }

    /** Dashboard action: an agent picks up a pending/active-AI conversation. */
    public void takeover(UUID conversationId, UUID agentUserId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Conversation", conversationId));
        if ("CLOSED".equals(conversation.getMode())) {
            throw new com.restaurant.ai.exception.BusinessRuleViolationException("This conversation is closed.");
        }
        conversation.setMode("HUMAN_ACTIVE");
        conversation.setAssignedUserId(agentUserId);
    }

    /** Dashboard action: hand the conversation back to the AI. */
    public void returnToAi(UUID conversationId) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Conversation", conversationId));
        if ("CLOSED".equals(conversation.getMode())) {
            throw new com.restaurant.ai.exception.BusinessRuleViolationException("This conversation is closed.");
        }
        conversation.setMode("AI_ACTIVE");
        conversation.setAssignedUserId(null);
    }

    public void close(UUID conversationId) {
        conversationRepository.findById(conversationId)
                .orElseThrow(() -> ResourceNotFoundException.of("Conversation", conversationId))
                .setMode("CLOSED");
    }

    private void maybeRefreshSummary(UUID conversationId) {
        List<Message> all = messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
        if (all.size() % SUMMARIZE_EVERY_N_MESSAGES != 0) {
            return;
        }
        String naiveSummary = all.stream()
                .map(m -> ("CUSTOMER".equals(m.getSenderType()) ? "Customer: " : "Assistant: ") + m.getContent())
                .reduce((a, b) -> a + " | " + b)
                .orElse("");
        if (naiveSummary.length() > SUMMARY_MAX_CHARS) {
            naiveSummary = "..." + naiveSummary.substring(naiveSummary.length() - SUMMARY_MAX_CHARS);
        }
        String finalSummary = naiveSummary;
        conversationRepository.findById(conversationId).ifPresent(c -> c.setSummary(finalSummary));
    }
}
