package com.restaurant.ai.controller;

import com.restaurant.ai.config.RestaurantProperties;
import com.restaurant.ai.dto.ConversationResponse;
import com.restaurant.ai.dto.MessageResponse;
import com.restaurant.ai.service.ConversationQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/conversations")
public class ConversationController {

    private final ConversationQueryService conversationQueryService;
    private final RestaurantProperties restaurantProperties;

    public ConversationController(ConversationQueryService conversationQueryService, RestaurantProperties restaurantProperties) {
        this.conversationQueryService = conversationQueryService;
        this.restaurantProperties = restaurantProperties;
    }

    @GetMapping
    public List<ConversationResponse> list(@RequestParam(required = false) String mode) {
        return conversationQueryService.listByMode(restaurantProperties.getDefaultId(), mode);
    }

    @GetMapping("/{id}")
    public ConversationResponse get(@PathVariable UUID id) {
        return conversationQueryService.get(restaurantProperties.getDefaultId(), id);
    }

    @GetMapping("/{id}/messages")
    public List<MessageResponse> messages(@PathVariable UUID id) {
        return conversationQueryService.getMessages(restaurantProperties.getDefaultId(), id);
    }
}
