package com.restaurant.ai.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;
import java.util.UUID;

public record OrderRequest(
        @NotEmpty @Valid List<OrderLineItemRequest> items,
        @NotNull @Pattern(regexp = "DELIVERY|TAKEAWAY") String fulfillmentType,
        UUID deliveryZoneId,     // required if fulfillmentType=DELIVERY
        String deliveryAddress,  // required if fulfillmentType=DELIVERY
        String promotionCode,    // optional
        String specialInstructions,
        String idempotencyKey    // caller-supplied, required for create_order so retries can't double-create
) {
}
