package com.restaurant.ai.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MessageResponse(
        UUID id,
        String direction,
        String senderType,
        String content,
        OffsetDateTime createdAt
) {
}
