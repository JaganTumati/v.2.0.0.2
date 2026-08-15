package com.restaurant.ai.controller;

import com.restaurant.ai.config.RestaurantProperties;
import com.restaurant.ai.dto.SupportTicketResponse;
import com.restaurant.ai.service.SupportTicketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/support-tickets")
public class SupportTicketController {

    private final SupportTicketService supportTicketService;
    private final RestaurantProperties restaurantProperties;

    public SupportTicketController(SupportTicketService supportTicketService, RestaurantProperties restaurantProperties) {
        this.supportTicketService = supportTicketService;
        this.restaurantProperties = restaurantProperties;
    }

    @GetMapping
    public List<SupportTicketResponse> list() {
        return supportTicketService.list(restaurantProperties.getDefaultId());
    }
}
