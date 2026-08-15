package com.restaurant.ai.dto;

import com.restaurant.ai.ai.ConversationTurn;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

/**
 * Stateless test-only request shape: caller supplies whatever prior turns
 * they want considered. Once Phase 8 (Conversation/Message persistence) and
 * Phase 9 (WhatsApp webhook) land, real traffic won't use this endpoint -
 * it exists so the AI -> tool -> DB -> Gemini loop can be verified today.
 */
public record AiChatRequest(
        List<ConversationTurn> history,
        @NotBlank String message,
        String customerWhatsappNumber
) {
}
