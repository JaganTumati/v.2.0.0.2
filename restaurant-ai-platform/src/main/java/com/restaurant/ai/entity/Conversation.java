package com.restaurant.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "conversations")
@Getter
@Setter
@NoArgsConstructor
public class Conversation {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    /** AI_ACTIVE, HUMAN_PENDING, HUMAN_ACTIVE, CLOSED */
    @Column(nullable = false, length = 20)
    private String mode = "AI_ACTIVE";

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(name = "current_order_id")
    private UUID currentOrderId;

    @Column(name = "last_intent", length = 50)
    private String lastIntent;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
