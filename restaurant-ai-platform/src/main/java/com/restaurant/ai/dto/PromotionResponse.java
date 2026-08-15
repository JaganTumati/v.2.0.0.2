package com.restaurant.ai.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PromotionResponse(
        String code,
        String description,
        String discountType,
        BigDecimal discountValue,
        OffsetDateTime validUntil
) {
}
