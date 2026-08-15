package com.restaurant.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record MenuItemOptionDto(
        UUID id,
        @NotBlank String name,
        @NotNull BigDecimal priceDelta,
        Boolean active
) {
}
