package com.restaurant.ai.service;

import com.restaurant.ai.dto.AnalyticsOverviewResponse;
import com.restaurant.ai.dto.MenuPopularityResponse;
import com.restaurant.ai.repository.ConversationRepository;
import com.restaurant.ai.repository.LeadRepository;
import com.restaurant.ai.repository.OrderItemRepository;
import com.restaurant.ai.repository.OrderRepository;
import com.restaurant.ai.repository.SupportTicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Phase 18. Every number here comes from a real query against existing
 * tables - nothing is estimated except aiHandledShare, which is explicitly
 * a proxy (current conversation mode, not a true historical resolution
 * rate) since mode-change history isn't separately logged yet. A real
 * version would read from audit_logs once escalation events are recorded
 * there distinctly from tool-call audits.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private static final List<String> TERMINAL_ORDER_STATUSES = List.of("DELIVERED", "CANCELLED");
    private static final List<String> HUMAN_MODES = List.of("HUMAN_PENDING", "HUMAN_ACTIVE");
    private static final int POPULARITY_LIMIT = 10;

    private final ConversationRepository conversationRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final LeadRepository leadRepository;
    private final SupportTicketRepository supportTicketRepository;

    public AnalyticsService(ConversationRepository conversationRepository, OrderRepository orderRepository,
                             OrderItemRepository orderItemRepository, LeadRepository leadRepository,
                             SupportTicketRepository supportTicketRepository) {
        this.conversationRepository = conversationRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.leadRepository = leadRepository;
        this.supportTicketRepository = supportTicketRepository;
    }

    public AnalyticsOverviewResponse overview(UUID restaurantId) {
        OffsetDateTime startOfToday = LocalDate.now().atStartOfDay().atOffset(ZoneOffset.UTC);

        long conversationsToday = conversationRepository.countByRestaurantIdAndCreatedAtAfter(restaurantId, startOfToday);
        long ordersToday = orderRepository.countByRestaurantIdAndCreatedAtAfter(restaurantId, startOfToday);
        BigDecimal revenueToday = orderRepository.sumRevenueSince(restaurantId, startOfToday);
        long activeOrders = orderRepository.countByRestaurantIdAndStatusNotIn(restaurantId, TERMINAL_ORDER_STATUSES);
        long openTickets = supportTicketRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .filter(t -> "OPEN".equals(t.getStatus()))
                .count();
        long newLeads = leadRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .filter(l -> "NEW".equals(l.getStatus()))
                .count();
        long totalConversations = conversationRepository.countByRestaurantId(restaurantId);
        long conversationsWithHuman = conversationRepository.countByRestaurantIdAndModeIn(restaurantId, HUMAN_MODES);

        double aiHandledShare = totalConversations == 0
                ? 1.0
                : 1.0 - ((double) conversationsWithHuman / totalConversations);

        return new AnalyticsOverviewResponse(
                conversationsToday, ordersToday, revenueToday, activeOrders, openTickets, newLeads,
                totalConversations, conversationsWithHuman, aiHandledShare
        );
    }

    public List<MenuPopularityResponse> menuPopularity(UUID restaurantId) {
        return orderItemRepository.findMenuPopularity(restaurantId, POPULARITY_LIMIT).stream()
                .map(p -> new MenuPopularityResponse(p.getItemName(), p.getTotalQuantity()))
                .toList();
    }
}
