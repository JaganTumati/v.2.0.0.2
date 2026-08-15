package com.restaurant.ai.ai.tool;

import java.util.UUID;

/**
 * Authoritative context for a tool call, resolved by the backend BEFORE
 * Gemini is ever invoked - never taken from the AI's function-call arguments.
 * This is what prevents a prompt-injected "cancel someone else's order" or
 * "access another customer's data" from working: the tool handler only ever
 * trusts restaurantId/customerId/conversationId from here, never from args.
 */
public record ToolExecutionContext(
        UUID restaurantId,
        UUID customerId,
        UUID conversationId,
        String correlationId
) {
}
