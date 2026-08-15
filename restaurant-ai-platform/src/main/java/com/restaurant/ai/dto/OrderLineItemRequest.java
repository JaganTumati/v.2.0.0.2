package com.restaurant.ai.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record OrderLineItemRequest(
        @NotNull UUID menuItemId,
        @Min(1) int quantity
) {
}
