package com.restaurant.ai.mapper;

import com.restaurant.ai.dto.MenuCategoryResponse;
import com.restaurant.ai.dto.MenuItemOptionDto;
import com.restaurant.ai.dto.MenuItemResponse;
import com.restaurant.ai.entity.MenuCategory;
import com.restaurant.ai.entity.MenuItem;
import com.restaurant.ai.entity.MenuItemOption;

import java.util.List;

public final class MenuMapper {

    private MenuMapper() {
    }

    public static MenuCategoryResponse toResponse(MenuCategory c) {
        return new MenuCategoryResponse(c.getId(), c.getName(), c.getDisplayOrder(), c.isActive());
    }

    public static MenuItemOptionDto toDto(MenuItemOption o) {
        return new MenuItemOptionDto(o.getId(), o.getName(), o.getPriceDelta(), o.isActive());
    }

    public static MenuItemResponse toResponse(MenuItem item, String categoryName, List<MenuItemOption> options) {
        return new MenuItemResponse(
                item.getId(),
                item.getCategoryId(),
                categoryName,
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.isVegetarian(),
                item.getSpicyLevel(),
                item.isAvailable(),
                item.getImageUrl(),
                options.stream().map(MenuMapper::toDto).toList()
        );
    }
}
