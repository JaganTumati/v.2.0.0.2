package com.restaurant.ai.dto;

import jakarta.validation.constraints.NotNull;

public record AvailabilityUpdateRequest(
        @NotNull Boolean available
) {
}
