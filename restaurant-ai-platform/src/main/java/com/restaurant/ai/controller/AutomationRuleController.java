package com.restaurant.ai.controller;

import com.restaurant.ai.config.RestaurantProperties;
import com.restaurant.ai.dto.AutomationRuleRequest;
import com.restaurant.ai.dto.AutomationRuleResponse;
import com.restaurant.ai.service.AutomationRuleService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/automation-rules")
@PreAuthorize("hasAnyRole('RESTAURANT_OWNER','MANAGER')")
public class AutomationRuleController {

    private final AutomationRuleService automationRuleService;
    private final RestaurantProperties restaurantProperties;

    public AutomationRuleController(AutomationRuleService automationRuleService, RestaurantProperties restaurantProperties) {
        this.automationRuleService = automationRuleService;
        this.restaurantProperties = restaurantProperties;
    }

    @GetMapping
    public List<AutomationRuleResponse> list() {
        return automationRuleService.list(restaurantProperties.getDefaultId());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AutomationRuleResponse create(@Valid @RequestBody AutomationRuleRequest request) {
        return automationRuleService.create(restaurantProperties.getDefaultId(), request);
    }

    @PutMapping("/{id}")
    public AutomationRuleResponse update(@PathVariable UUID id, @Valid @RequestBody AutomationRuleRequest request) {
        return automationRuleService.update(restaurantProperties.getDefaultId(), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        automationRuleService.delete(restaurantProperties.getDefaultId(), id);
    }
}
