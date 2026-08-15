package com.restaurant.ai.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemRequest(
        @NotNull UUID categoryId,
        @NotBlank @Size(max = 150) String name,
        @Size(max = 2000) String description,
        @NotNull @DecimalMin(value = "0.0", inclusive = true) BigDecimal price,
        boolean vegetarian,
        @Min(0) @Max(3) Short spicyLevel,
        Boolean available,
        @Size(max = 500) String imageUrl
) {
}
