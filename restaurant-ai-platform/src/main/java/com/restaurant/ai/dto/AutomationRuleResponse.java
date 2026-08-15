package com.restaurant.ai.dto;

import java.util.Map;
import java.util.UUID;

public record AutomationRuleResponse(
        UUID id,
        String name,
        String triggerType,
        Map<String, Object> condition,
        String actionType,
        Map<String, Object> actionConfig,
        boolean active
) {
}
