package com.restaurant.ai.controller;

import com.restaurant.ai.config.RestaurantProperties;
import com.restaurant.ai.dto.AvailabilityUpdateRequest;
import com.restaurant.ai.dto.MenuItemRequest;
import com.restaurant.ai.dto.MenuItemResponse;
import com.restaurant.ai.service.MenuItemService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/menu-items")
public class MenuItemController {

    private final MenuItemService menuItemService;
    private final RestaurantProperties restaurantProperties;

    public MenuItemController(MenuItemService menuItemService, RestaurantProperties restaurantProperties) {
        this.menuItemService = menuItemService;
        this.restaurantProperties = restaurantProperties;
    }

    @GetMapping
    public List<MenuItemResponse> list(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false, defaultValue = "false") boolean includeUnavailable,
            @RequestParam(required = false) Boolean vegetarian,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Short maxSpicyLevel,
            @RequestParam(required = false) String q) {

        UUID restaurantId = restaurantProperties.getDefaultId();

        boolean hasSearchFilters = vegetarian != null || maxPrice != null || maxSpicyLevel != null
                || (q != null && !q.isBlank());
        if (hasSearchFilters) {
            return menuItemService.search(restaurantId, categoryId, vegetarian, maxPrice, maxSpicyLevel, q);
        }
        return menuItemService.list(restaurantId, categoryId, includeUnavailable);
    }

    @GetMapping("/{id}")
    public MenuItemResponse get(@PathVariable UUID id) {
        return menuItemService.get(restaurantProperties.getDefaultId(), id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER','MANAGER')")
    public MenuItemResponse create(@Valid @RequestBody MenuItemRequest request) {
        return menuItemService.create(restaurantProperties.getDefaultId(), request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER','MANAGER')")
    public MenuItemResponse update(@PathVariable UUID id, @Valid @RequestBody MenuItemRequest request) {
        return menuItemService.update(restaurantProperties.getDefaultId(), id, request);
    }

    @PatchMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER','MANAGER','STAFF')")
    public MenuItemResponse setAvailability(@PathVariable UUID id, @Valid @RequestBody AvailabilityUpdateRequest request) {
        return menuItemService.setAvailability(restaurantProperties.getDefaultId(), id, request.available());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER','MANAGER')")
    public void delete(@PathVariable UUID id) {
        menuItemService.delete(restaurantProperties.getDefaultId(), id);
    }
}
