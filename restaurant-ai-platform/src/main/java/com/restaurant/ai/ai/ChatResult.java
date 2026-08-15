package com.restaurant.ai.ai;

import java.util.List;

public record ChatResult(
        String replyText,
        List<ToolCallSummary> toolCalls
) {
    public record ToolCallSummary(String name, java.util.Map<String, Object> args, boolean success, java.util.Map<String, Object> result) {
    }
}
