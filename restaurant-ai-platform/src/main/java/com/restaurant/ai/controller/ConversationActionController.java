package com.restaurant.ai.controller;

import com.restaurant.ai.config.RestaurantProperties;
import com.restaurant.ai.dto.AgentMessageRequest;
import com.restaurant.ai.service.AgentReplyService;
import com.restaurant.ai.service.ConversationQueryService;
import com.restaurant.ai.service.ConversationService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Phase 13/17 dashboard actions. No auth/role check yet (Phase 19) - agentUserId
 * is accepted as a request param rather than resolved from a JWT principal,
 * as a placeholder until authentication exists.
 */
@RestController
@RequestMapping("/api/conversations")
public class ConversationActionController {

    private final ConversationService conversationService;
    private final ConversationQueryService conversationQueryService;
    private final AgentReplyService agentReplyService;
    private final RestaurantProperties restaurantProperties;

    public ConversationActionController(ConversationService conversationService,
                                         ConversationQueryService conversationQueryService,
                                         AgentReplyService agentReplyService,
                                         RestaurantProperties restaurantProperties) {
        this.conversationService = conversationService;
        this.conversationQueryService = conversationQueryService;
        this.agentReplyService = agentReplyService;
        this.restaurantProperties = restaurantProperties;
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<?> sendAgentMessage(@PathVariable UUID id, @RequestParam(required = false) UUID agentUserId,
                                               @Valid @RequestBody AgentMessageRequest request) {
        agentReplyService.sendAgentReply(restaurantProperties.getDefaultId(), id, agentUserId, request.content());
        return ResponseEntity.ok(Map.of("sent", true));
    }

    @PostMapping("/{id}/takeover")
    public ResponseEntity<?> takeover(@PathVariable UUID id, @RequestParam(required = false) UUID agentUserId) {
        conversationQueryService.get(restaurantProperties.getDefaultId(), id); // 404s if not this restaurant's conversation
        conversationService.takeover(id, agentUserId);
        return ResponseEntity.ok(Map.of("conversationId", id, "mode", "HUMAN_ACTIVE"));
    }

    @PostMapping("/{id}/return-to-ai")
    public ResponseEntity<?> returnToAi(@PathVariable UUID id) {
        conversationQueryService.get(restaurantProperties.getDefaultId(), id);
        conversationService.returnToAi(id);
        return ResponseEntity.ok(Map.of("conversationId", id, "mode", "AI_ACTIVE"));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> close(@PathVariable UUID id) {
        conversationQueryService.get(restaurantProperties.getDefaultId(), id);
        conversationService.close(id);
        return ResponseEntity.ok(Map.of("conversationId", id, "mode", "CLOSED"));
    }
}
