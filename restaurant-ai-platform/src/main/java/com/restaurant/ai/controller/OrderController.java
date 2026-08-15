package com.restaurant.ai.controller;

import com.restaurant.ai.config.RestaurantProperties;
import com.restaurant.ai.dto.OrderResponse;
import com.restaurant.ai.dto.OrderStatusUpdateRequest;
import com.restaurant.ai.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Dashboard/staff order management - distinct from the customer-scoped
 * paths OrderService exposes for AI tools (get_order_status, cancel_order).
 * Staff here can see and act on ANY order for the restaurant, not just
 * one customer's own order.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final RestaurantProperties restaurantProperties;

    public OrderController(OrderService orderService, RestaurantProperties restaurantProperties) {
        this.orderService = orderService;
        this.restaurantProperties = restaurantProperties;
    }

    @GetMapping
    public List<OrderResponse> list() {
        return orderService.listForRestaurant(restaurantProperties.getDefaultId());
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable UUID id) {
        return orderService.getStatusAsStaff(restaurantProperties.getDefaultId(), id);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER','MANAGER','STAFF')")
    public OrderResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody OrderStatusUpdateRequest request) {
        return orderService.transition(restaurantProperties.getDefaultId(), id, request.status().toUpperCase());
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('RESTAURANT_OWNER','MANAGER','STAFF')")
    public OrderResponse cancel(@PathVariable UUID id) {
        return orderService.cancelOrderAsStaff(restaurantProperties.getDefaultId(), id);
    }
}
