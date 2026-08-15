package com.restaurant.ai.dto;

import java.util.UUID;

public record KnowledgeDocumentResponse(
        UUID id,
        String title,
        String category,
        String content
) {
}
