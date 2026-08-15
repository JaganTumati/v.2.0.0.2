package com.restaurant.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MenuCategoryRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull Integer displayOrder,
        Boolean active
) {
}
