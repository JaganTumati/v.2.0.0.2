package com.restaurant.ai.dto;

public record MenuPopularityResponse(
        String itemName,
        long totalQuantity
) {
}
