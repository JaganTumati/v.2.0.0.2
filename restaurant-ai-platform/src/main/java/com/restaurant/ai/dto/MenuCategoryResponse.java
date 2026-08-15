package com.restaurant.ai.dto;

import java.util.UUID;

public record MenuCategoryResponse(
        UUID id,
        String name,
        Integer displayOrder,
        boolean active
) {
}
