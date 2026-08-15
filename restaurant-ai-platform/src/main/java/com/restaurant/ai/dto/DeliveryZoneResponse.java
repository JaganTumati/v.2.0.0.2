package com.restaurant.ai.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record DeliveryZoneResponse(
        UUID id,
        String name,
        BigDecimal deliveryFee,
        BigDecimal minOrderAmount,
        Integer estimatedMinutes
) {
}
