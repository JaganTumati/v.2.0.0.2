package com.restaurant.ai.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record SupportTicketResponse(
        UUID id,
        UUID conversationId,
        UUID orderId,
        String issue,
        String priority,
        String status,
        OffsetDateTime createdAt
) {
}
