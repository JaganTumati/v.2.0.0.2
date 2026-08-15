package com.restaurant.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RestaurantUpdateRequest(
        @NotBlank @Size(max = 200) String name,
        @Size(max = 120) String cuisineType,
        @Size(max = 30) String phone,
        @NotBlank @Pattern(regexp = "OPEN|CLOSED|PAUSED") String status,
        @Size(max = 300) String addressLine,
        @Size(max = 120) String city,
        @Size(max = 120) String state,
        @Size(max = 20) String postalCode,
        @Size(max = 120) String country
) {
}
