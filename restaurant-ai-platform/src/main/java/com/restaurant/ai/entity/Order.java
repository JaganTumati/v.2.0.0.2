package com.restaurant.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "conversation_id")
    private UUID conversationId;

    @Column(name = "delivery_zone_id")
    private UUID deliveryZoneId;

    /** DELIVERY, TAKEAWAY */
    @Column(name = "fulfillment_type", nullable = false, length = 20)
    private String fulfillmentType = "DELIVERY";

    /** DRAFT, CONFIRMATION_REQUIRED, CONFIRMED, PREPARING, READY, OUT_FOR_DELIVERY, DELIVERED, CANCELLED */
    @Column(nullable = false, length = 30)
    private String status = "DRAFT";

    @Column(name = "delivery_address", length = 300)
    private String deliveryAddress;

    @Column(name = "special_instructions", columnDefinition = "TEXT")
    private String specialInstructions;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "delivery_fee", nullable = false, precision = 10, scale = 2)
    private BigDecimal deliveryFee = BigDecimal.ZERO;

    @Column(name = "discount_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Column(name = "tax_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal taxTotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(name = "promotion_id")
    private UUID promotionId;

    @Column(name = "idempotency_key", nullable = false, length = 150)
    private String idempotencyKey;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
