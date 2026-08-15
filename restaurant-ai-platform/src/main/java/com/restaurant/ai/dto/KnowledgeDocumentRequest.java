package com.restaurant.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record KnowledgeDocumentRequest(
        @NotBlank @Size(max = 200) String title,
        @Size(max = 60) String category,
        @NotBlank String content,
        Boolean active
) {
}
