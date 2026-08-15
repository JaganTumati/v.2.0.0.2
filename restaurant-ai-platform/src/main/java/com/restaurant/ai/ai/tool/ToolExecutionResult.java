package com.restaurant.ai.ai.tool;

import java.util.Map;

/**
 * Outcome of one tool execution. "success" distinguishes a validation/business
 * rule rejection from a real result, so GeminiService can hand Gemini an
 * honest functionResponse either way - we never fabricate a result.
 */
public record ToolExecutionResult(
        String toolName,
        boolean success,
        Map<String, Object> data,
        String errorMessage
) {

    public static ToolExecutionResult ok(String toolName, Map<String, Object> data) {
        return new ToolExecutionResult(toolName, true, data, null);
    }

    public static ToolExecutionResult failure(String toolName, String errorMessage) {
        return new ToolExecutionResult(toolName, false, Map.of(), errorMessage);
    }

    /** What gets sent back to Gemini as the functionResponse body. */
    public Map<String, Object> toGeminiResponsePayload() {
        if (success) {
            return data;
        }
        return Map.of("error", errorMessage != null ? errorMessage : "Tool execution failed.");
    }
}
