package com.restaurant.ai.service;

import com.restaurant.ai.dto.*;
import com.restaurant.ai.entity.DeliveryZone;
import com.restaurant.ai.entity.MenuItem;
import com.restaurant.ai.entity.Order;
import com.restaurant.ai.entity.OrderItem;
import com.restaurant.ai.entity.Promotion;
import com.restaurant.ai.exception.BusinessRuleViolationException;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.repository.MenuItemRepository;
import com.restaurant.ai.repository.OrderItemRepository;
import com.restaurant.ai.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final DeliveryZoneService deliveryZoneService;
    private final PromotionService promotionService;
    private final AutomationEngine automationEngine;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository,
                         MenuItemRepository menuItemRepository, DeliveryZoneService deliveryZoneService,
                         PromotionService promotionService, AutomationEngine automationEngine) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.menuItemRepository = menuItemRepository;
        this.deliveryZoneService = deliveryZoneService;
        this.promotionService = promotionService;
        this.automationEngine = automationEngine;
    }

    /**
     * Pricing quote - does NOT persist anything. This is what calculate_order_total
     * calls, and what create_order calls internally before committing. The backend
     * always recomputes from current menu prices; a caller-supplied total is never trusted.
     */
    public Quote quote(UUID restaurantId, OrderRequest request) {
        validateFulfillmentFields(request);

        List<Line> lines = request.items().stream()
                .map(li -> priceLine(restaurantId, li))
                .toList();

        BigDecimal subtotal = lines.stream()
                .map(Line::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal deliveryFee = BigDecimal.ZERO;
        DeliveryZone zone = null;
        if ("DELIVERY".equals(request.fulfillmentType())) {
            zone = deliveryZoneService.getOrThrow(restaurantId, request.deliveryZoneId());
            if (!zone.isActive()) {
                throw new BusinessRuleViolationException("This delivery zone is not currently serviceable.");
            }
            if (subtotal.compareTo(zone.getMinOrderAmount()) < 0) {
                throw new BusinessRuleViolationException(
                        "Minimum order for delivery to this area is " + zone.getMinOrderAmount() + ".");
            }
            deliveryFee = zone.getDeliveryFee();
        }

        BigDecimal discountTotal = BigDecimal.ZERO;
        String appliedCode = null;
        if (request.promotionCode() != null && !request.promotionCode().isBlank()) {
            Promotion promo = promotionService.findValidByCode(restaurantId, request.promotionCode())
                    .orElseThrow(() -> new BusinessRuleViolationException("That promotion code is not valid or has expired."));
            discountTotal = "PERCENT".equals(promo.getDiscountType())
                    ? subtotal.multiply(promo.getDiscountValue()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : promo.getDiscountValue().min(subtotal);
            appliedCode = promo.getCode();
        }

        BigDecimal taxTotal = BigDecimal.ZERO; // tax rules not yet configurable per restaurant - left at 0 for MVP
        BigDecimal total = subtotal.add(deliveryFee).add(taxTotal).subtract(discountTotal);

        return new Quote(lines, subtotal, deliveryFee, discountTotal, taxTotal, total, appliedCode, zone);
    }

    @Transactional
    public OrderResponse createOrder(UUID restaurantId, UUID customerId, UUID conversationId, OrderRequest request) {
        if (request.idempotencyKey() == null || request.idempotencyKey().isBlank()) {
            throw new BusinessRuleViolationException("An idempotency key is required to create an order.");
        }
        var existing = orderRepository.findByIdempotencyKey(request.idempotencyKey());
        if (existing.isPresent()) {
            // Safe retry: return the already-created order instead of creating a duplicate.
            return toResponse(existing.get());
        }

        Quote quote = quote(restaurantId, request);

        Order order = new Order();
        order.setRestaurantId(restaurantId);
        order.setCustomerId(customerId);
        order.setConversationId(conversationId);
        order.setFulfillmentType(request.fulfillmentType());
        order.setStatus("CONFIRMATION_REQUIRED");
        order.setDeliveryAddress(request.deliveryAddress());
        order.setSpecialInstructions(request.specialInstructions());
        order.setDeliveryZoneId(quote.zone() != null ? quote.zone().getId() : null);
        order.setSubtotal(quote.subtotal());
        order.setDeliveryFee(quote.deliveryFee());
        order.setDiscountTotal(quote.discountTotal());
        order.setTaxTotal(quote.taxTotal());
        order.setTotal(quote.total());
        order.setIdempotencyKey(request.idempotencyKey());
        orderRepository.save(order);

        for (Line line : quote.lines()) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setMenuItemId(line.menuItemId());
            item.setItemNameSnapshot(line.name());
            item.setUnitPriceSnapshot(line.unitPrice());
            item.setQuantity(line.quantity());
            item.setLineTotal(line.lineTotal());
            orderItemRepository.save(item);
        }

        return toResponse(order);
    }

    @Transactional
    public OrderResponse confirmOrder(UUID restaurantId, UUID orderId) {
        return transition(restaurantId, orderId, "CONFIRMED");
    }

    @Transactional
    public OrderResponse cancelOrder(UUID restaurantId, UUID customerId, UUID orderId) {
        Order order = findOrThrowForCustomer(restaurantId, customerId, orderId);
        if (!OrderStateMachine.isCancellable(order.getStatus())) {
            throw new BusinessRuleViolationException(
                    "This order can no longer be cancelled (current status: " + order.getStatus() + "). Please contact the restaurant.");
        }
        String previousStatus = order.getStatus();
        order.setStatus("CANCELLED");
        fireStatusChanged(order, previousStatus);
        return toResponse(order);
    }

    /** Dashboard/staff path - not customer-scoped, used by future admin endpoints only, never by AI tools. */
    @Transactional
    public OrderResponse cancelOrderAsStaff(UUID restaurantId, UUID orderId) {
        Order order = findOrThrow(restaurantId, orderId);
        if (!OrderStateMachine.isCancellable(order.getStatus())) {
            throw new BusinessRuleViolationException(
                    "This order can no longer be cancelled (current status: " + order.getStatus() + ").");
        }
        String previousStatus = order.getStatus();
        order.setStatus("CANCELLED");
        fireStatusChanged(order, previousStatus);
        return toResponse(order);
    }

    @Transactional
    public OrderResponse transition(UUID restaurantId, UUID orderId, String toStatus) {
        Order order = findOrThrow(restaurantId, orderId);
        String previousStatus = order.getStatus();
        if (!OrderStateMachine.isTransitionAllowed(previousStatus, toStatus)) {
            throw new BusinessRuleViolationException(
                    "Cannot move order from " + previousStatus + " to " + toStatus + ".");
        }
        order.setStatus(toStatus);
        fireStatusChanged(order, previousStatus);
        return toResponse(order);
    }

    /** Phase 15: fires ORDER_STATUS_CHANGED for any active automation rules. Never lets a rule failure affect the order itself. */
    private void fireStatusChanged(Order order, String previousStatus) {
        // Mutable map, not Map.of() - Map.of() rejects null values, and conversationId
        // is legitimately null for takeaway/no-conversation orders. AutomationEngine's
        // action handlers (e.g. ESCALATE) cast this straight to UUID, so a sentinel
        // string here would cause a ClassCastException instead of a clean no-op.
        java.util.Map<String, Object> eventData = new java.util.HashMap<>();
        eventData.put("orderId", order.getId());
        eventData.put("orderReference", order.getId().toString().substring(0, 8).toUpperCase(java.util.Locale.ROOT));
        eventData.put("previousStatus", previousStatus);
        eventData.put("status", order.getStatus());
        eventData.put("customerId", order.getCustomerId());
        eventData.put("conversationId", order.getConversationId());
        automationEngine.fire(order.getRestaurantId(), "ORDER_STATUS_CHANGED", eventData);
    }

    public OrderResponse getStatus(UUID restaurantId, UUID customerId, UUID orderId) {
        return toResponse(findOrThrowForCustomer(restaurantId, customerId, orderId));
    }

    /** Dashboard/staff path - not customer-scoped. */
    public OrderResponse getStatusAsStaff(UUID restaurantId, UUID orderId) {
        return toResponse(findOrThrow(restaurantId, orderId));
    }

    public List<OrderResponse> listForRestaurant(UUID restaurantId) {
        return orderRepository.findByRestaurantIdOrderByCreatedAtDesc(restaurantId).stream()
                .map(this::toResponse)
                .toList();
    }

    private void validateFulfillmentFields(OrderRequest request) {
        if ("DELIVERY".equals(request.fulfillmentType())) {
            if (request.deliveryZoneId() == null) {
                throw new BusinessRuleViolationException("A delivery zone is required for delivery orders.");
            }
            if (request.deliveryAddress() == null || request.deliveryAddress().isBlank()) {
                throw new BusinessRuleViolationException("A delivery address is required for delivery orders.");
            }
        }
    }

    private Line priceLine(UUID restaurantId, OrderLineItemRequest lineRequest) {
        MenuItem item = menuItemRepository.findByIdAndRestaurantId(lineRequest.menuItemId(), restaurantId)
                .orElseThrow(() -> new BusinessRuleViolationException("One of the requested items is no longer on the menu."));
        if (!item.isAvailable()) {
            throw new BusinessRuleViolationException("'" + item.getName() + "' is currently unavailable.");
        }
        BigDecimal lineTotal = item.getPrice().multiply(BigDecimal.valueOf(lineRequest.quantity()));
        return new Line(item.getId(), item.getName(), item.getPrice(), lineRequest.quantity(), lineTotal);
    }

    private Order findOrThrow(UUID restaurantId, UUID orderId) {
        return orderRepository.findByIdAndRestaurantId(orderId, restaurantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Order", orderId));
    }

    /**
     * Ownership-scoped lookup for every AI-tool-facing order operation. Returns
     * "not found" (never a distinct "forbidden") when the order exists but
     * belongs to someone else, so a probing customer can't use the error
     * message to confirm another customer's order id is valid.
     */
    private Order findOrThrowForCustomer(UUID restaurantId, UUID customerId, UUID orderId) {
        Order order = findOrThrow(restaurantId, orderId);
        if (customerId == null || !order.getCustomerId().equals(customerId)) {
            throw ResourceNotFoundException.of("Order", orderId);
        }
        return order;
    }

    private OrderResponse toResponse(Order order) {
        List<OrderLineItemResponse> items = orderItemRepository.findByOrderId(order.getId()).stream()
                .map(i -> new OrderLineItemResponse(i.getMenuItemId(), i.getItemNameSnapshot(), i.getUnitPriceSnapshot(), i.getQuantity(), i.getLineTotal()))
                .toList();
        return new OrderResponse(
                order.getId(), order.getStatus(), order.getFulfillmentType(), order.getDeliveryAddress(),
                items, order.getSubtotal(), order.getDeliveryFee(), order.getDiscountTotal(), order.getTaxTotal(),
                order.getTotal(), null, order.getCreatedAt()
        );
    }

    /** Internal pricing result - not persisted, used both for calculate_order_total and as the pre-commit step of createOrder. */
    public record Line(UUID menuItemId, String name, BigDecimal unitPrice, int quantity, BigDecimal lineTotal) {
    }

    public record Quote(List<Line> lines, BigDecimal subtotal, BigDecimal deliveryFee, BigDecimal discountTotal,
                         BigDecimal taxTotal, BigDecimal total, String appliedPromotionCode, DeliveryZone zone) {
    }
}
