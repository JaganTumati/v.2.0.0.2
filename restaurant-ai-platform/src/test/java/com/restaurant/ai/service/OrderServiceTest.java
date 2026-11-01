package com.restaurant.ai.service;

import com.restaurant.ai.entity.Order;
import com.restaurant.ai.repository.MenuItemRepository;
import com.restaurant.ai.repository.OrderItemRepository;
import com.restaurant.ai.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private final OrderRepository orderRepository = mock(OrderRepository.class);
    private final OrderItemRepository orderItemRepository = mock(OrderItemRepository.class);
    private final AutomationEngine automationEngine = mock(AutomationEngine.class);
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderService(
                orderRepository,
                orderItemRepository,
                mock(MenuItemRepository.class),
                mock(DeliveryZoneService.class),
                mock(PromotionService.class),
                automationEngine);
    }

    @Test
    void transitionFiresStatusEventWithPreviousAndCurrentStatus() {
        UUID restaurantId = UUID.randomUUID();
        UUID orderId = UUID.fromString("12345678-0000-0000-0000-000000000001");
        Order order = new Order();
        order.setId(orderId);
        order.setRestaurantId(restaurantId);
        order.setCustomerId(UUID.randomUUID());
        order.setStatus("CONFIRMED");
        when(orderRepository.findByIdAndRestaurantId(orderId, restaurantId)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByOrderId(orderId)).thenReturn(List.of());

        orderService.transition(restaurantId, orderId, "PREPARING");

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, Object>> eventDataCaptor = ArgumentCaptor.forClass(Map.class);
        verify(automationEngine).fire(eq(restaurantId), eq("ORDER_STATUS_CHANGED"), eventDataCaptor.capture());
        Map<String, Object> eventData = eventDataCaptor.getValue();
        assertEquals("CONFIRMED", eventData.get("previousStatus"));
        assertEquals("PREPARING", eventData.get("status"));
        assertEquals("12345678", eventData.get("orderReference"));
    }
}