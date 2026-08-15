package com.restaurant.ai.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderLineItemResponse(
        UUID menuItemId,
        String itemName,
        BigDecimal unitPrice,
        int quantity,
        BigDecimal lineTotal
) {
}
