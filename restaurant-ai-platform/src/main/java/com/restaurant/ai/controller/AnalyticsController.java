package com.restaurant.ai.controller;

import com.restaurant.ai.config.RestaurantProperties;
import com.restaurant.ai.dto.AnalyticsOverviewResponse;
import com.restaurant.ai.dto.MenuPopularityResponse;
import com.restaurant.ai.service.AnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final RestaurantProperties restaurantProperties;

    public AnalyticsController(AnalyticsService analyticsService, RestaurantProperties restaurantProperties) {
        this.analyticsService = analyticsService;
        this.restaurantProperties = restaurantProperties;
    }

    @GetMapping("/overview")
    public AnalyticsOverviewResponse overview() {
        return analyticsService.overview(restaurantProperties.getDefaultId());
    }

    @GetMapping("/menu-popularity")
    public List<MenuPopularityResponse> menuPopularity() {
        return analyticsService.menuPopularity(restaurantProperties.getDefaultId());
    }
}
