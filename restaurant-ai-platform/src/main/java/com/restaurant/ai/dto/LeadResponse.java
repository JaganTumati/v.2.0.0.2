package com.restaurant.ai.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record LeadResponse(
        UUID id,
        String source,
        String intentNotes,
        BigDecimal estimatedValue,
        String status,
        OffsetDateTime createdAt
) {
}
