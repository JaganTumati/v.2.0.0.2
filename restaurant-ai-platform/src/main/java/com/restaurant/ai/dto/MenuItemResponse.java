package com.restaurant.ai.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record MenuItemResponse(
        UUID id,
        UUID categoryId,
        String categoryName,
        String name,
        String description,
        BigDecimal price,
        boolean vegetarian,
        Short spicyLevel,
        boolean available,
        String imageUrl,
        List<MenuItemOptionDto> options
) {
}
