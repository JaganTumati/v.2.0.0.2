package com.restaurant.ai.ai;

/**
 * role is "user" or "model" (Gemini's convention). Placeholder for the test
 * endpoint - Phase 8 will replace ad-hoc history lists like this with
 * ConversationService loading persisted Message rows and mapping them here.
 */
public record ConversationTurn(String role, String text) {
}
