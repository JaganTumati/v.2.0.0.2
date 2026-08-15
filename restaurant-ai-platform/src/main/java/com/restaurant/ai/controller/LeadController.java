package com.restaurant.ai.controller;

import com.restaurant.ai.config.RestaurantProperties;
import com.restaurant.ai.dto.LeadResponse;
import com.restaurant.ai.service.LeadService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/leads")
public class LeadController {

    private final LeadService leadService;
    private final RestaurantProperties restaurantProperties;

    public LeadController(LeadService leadService, RestaurantProperties restaurantProperties) {
        this.leadService = leadService;
        this.restaurantProperties = restaurantProperties;
    }

    @GetMapping
    public List<LeadResponse> list() {
        return leadService.list(restaurantProperties.getDefaultId());
    }
}
