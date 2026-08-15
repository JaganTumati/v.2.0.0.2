package com.restaurant.ai.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record ConversationResponse(
        UUID id,
        UUID customerId,
        String customerDisplayName,
        String customerWhatsappNumber,
        String mode,
        String lastIntent,
        String summary,
        OffsetDateTime updatedAt
) {
}
