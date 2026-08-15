package com.restaurant.ai.controller;

import com.restaurant.ai.config.RestaurantProperties;
import com.restaurant.ai.dto.MenuCategoryRequest;
import com.restaurant.ai.dto.MenuCategoryResponse;
import com.restaurant.ai.service.MenuCategoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Single-restaurant MVP: restaurantId is resolved server-side from config
 * (see RestaurantProperties) rather than taken from the request, so a
 * caller can never address another tenant's data even before auth/JWT
 * (Phase 19) is wired in.
 */
@RestController
@RequestMapping("/api/menu-categories")
public class MenuCategoryController {

    private final MenuCategoryService menuCategoryService;
    private final RestaurantProperties restaurantProperties;

    public MenuCategoryController(MenuCategoryService menuCategoryService, RestaurantProperties restaurantProperties) {
        this.menuCategoryService = menuCategoryService;
        this.restaurantProperties = restaurantProperties;
    }

    @GetMapping
    public List<MenuCategoryResponse> list() {
        return menuCategoryService.list(restaurantProperties.getDefaultId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER','MANAGER')")
    public MenuCategoryResponse create(@Valid @RequestBody MenuCategoryRequest request) {
        return menuCategoryService.create(restaurantProperties.getDefaultId(), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER','MANAGER')")
    public MenuCategoryResponse update(@PathVariable UUID id, @Valid @RequestBody MenuCategoryRequest request) {
        return menuCategoryService.update(restaurantProperties.getDefaultId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER','MANAGER')")
    public void delete(@PathVariable UUID id) {
        menuCategoryService.delete(restaurantProperties.getDefaultId(), id);
    }
}
