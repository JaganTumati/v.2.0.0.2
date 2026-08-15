package com.restaurant.ai.dto;

import java.math.BigDecimal;

public record AnalyticsOverviewResponse(
        long conversationsToday,
        long ordersToday,
        BigDecimal revenueToday,
        long activeOrders,
        long openTickets,
        long newLeads,
        long totalConversations,
        long conversationsCurrentlyWithHuman,
        double aiHandledShare
) {
}
