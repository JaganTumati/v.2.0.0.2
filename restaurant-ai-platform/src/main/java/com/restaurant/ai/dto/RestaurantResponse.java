package com.restaurant.ai.dto;

import java.util.UUID;

public record RestaurantResponse(
        UUID id,
        String name,
        String cuisineType,
        String phone,
        String currency,
        String status,
        String addressLine,
        String city,
        String state,
        String postalCode,
        String country
) {
}
