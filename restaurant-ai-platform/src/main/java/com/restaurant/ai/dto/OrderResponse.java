package com.restaurant.ai.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String status,
        String fulfillmentType,
        String deliveryAddress,
        List<OrderLineItemResponse> items,
        BigDecimal subtotal,
        BigDecimal deliveryFee,
        BigDecimal discountTotal,
        BigDecimal taxTotal,
        BigDecimal total,
        String appliedPromotionCode,
        OffsetDateTime createdAt
) {
}
