package com.restaurant.ai.controller;

import com.restaurant.ai.ai.ChatResult;
import com.restaurant.ai.ai.GeminiService;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.config.RestaurantProperties;
import com.restaurant.ai.dto.AiChatRequest;
import com.restaurant.ai.entity.Customer;
import com.restaurant.ai.service.ConversationService;
import com.restaurant.ai.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * TEST-ONLY endpoint for exercising the full AI -> tool -> database -> Gemini
 * loop without needing WhatsApp wired up (Phase 9/10). Not authenticated
 * beyond the placeholder SecurityConfig, and not meant for production
 * traffic - the real path is Webhook -> ConversationService -> GeminiService.
 *
 * If customerWhatsappNumber is supplied, a real Customer/Conversation is
 * resolved (same as the webhook path) so the order tools (which require a
 * customer identity) can be exercised, not just menu lookups.
 */
@RestController
@RequestMapping("/api/ai")
public class AiTestController {

    private final GeminiService geminiService;
    private final RestaurantProperties restaurantProperties;
    private final CustomerService customerService;
    private final ConversationService conversationService;

    public AiTestController(GeminiService geminiService, RestaurantProperties restaurantProperties,
                             CustomerService customerService, ConversationService conversationService) {
        this.geminiService = geminiService;
        this.restaurantProperties = restaurantProperties;
        this.customerService = customerService;
        this.conversationService = conversationService;
    }

    @PostMapping("/chat")
    public ChatResult chat(@Valid @RequestBody AiChatRequest request) {
        UUID restaurantId = restaurantProperties.getDefaultId();

        UUID customerId = null;
        UUID conversationId = null;
        if (request.customerWhatsappNumber() != null && !request.customerWhatsappNumber().isBlank()) {
            Customer customer = customerService.findOrCreate(restaurantId, request.customerWhatsappNumber(), "Test Customer");
            customerId = customer.getId();
            conversationId = conversationService.findOrCreateOpenConversation(restaurantId, customerId).getId();
        }

        ToolExecutionContext context = new ToolExecutionContext(
                restaurantId, customerId, conversationId, UUID.randomUUID().toString());

        List<com.restaurant.ai.ai.ConversationTurn> history =
                request.history() == null ? List.of() : request.history();
        return geminiService.chat(context, history, request.message());
    }
}
