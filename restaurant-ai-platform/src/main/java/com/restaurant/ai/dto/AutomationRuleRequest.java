package com.restaurant.ai.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.Map;

public record AutomationRuleRequest(
        @NotBlank String name,
        @NotBlank String triggerType,
        Map<String, Object> condition,
        @NotBlank String actionType,
        Map<String, Object> actionConfig,
        Boolean active
) {
}
