package com.restaurant.ai.controller;

import com.restaurant.ai.config.RestaurantProperties;
import com.restaurant.ai.dto.RestaurantResponse;
import com.restaurant.ai.dto.RestaurantUpdateRequest;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * MVP is single-restaurant: {id} is accepted for API shape consistency with a
 * future multi-tenant version, but is validated against the configured
 * default restaurant rather than resolved from an auth context yet.
 */
@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantService restaurantService;
    private final RestaurantProperties restaurantProperties;

    public RestaurantController(RestaurantService restaurantService, RestaurantProperties restaurantProperties) {
        this.restaurantService = restaurantService;
        this.restaurantProperties = restaurantProperties;
    }

    @GetMapping("/{id}")
    public RestaurantResponse get(@PathVariable UUID id) {
        return restaurantService.get(resolveTenantScoped(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RESTAURANT_OWNER')")
    public RestaurantResponse update(@PathVariable UUID id, @Valid @RequestBody RestaurantUpdateRequest request) {
        return restaurantService.update(resolveTenantScoped(id), request);
    }

    /**
     * Tenant-boundary seam: today this just enforces callers can only address
     * the single configured restaurant. When multi-tenancy lands, this method
     * is replaced by resolving restaurantId from the authenticated principal.
     */
    private UUID resolveTenantScoped(UUID requestedId) {
        UUID defaultId = restaurantProperties.getDefaultId();
        if (!defaultId.equals(requestedId)) {
            throw new ResourceNotFoundException("Restaurant not found: " + requestedId);
        }
        return requestedId;
    }
}
