package com.restaurant.ai.dto;

import jakarta.validation.constraints.NotBlank;

public record AgentMessageRequest(
        @NotBlank String content
) {
}
