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
@Table(name = "support_tickets")
@Getter
@Setter
@NoArgsConstructor
public class SupportTicket {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "conversation_id")
    private UUID conversationId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String issue;

    /** LOW, NORMAL, HIGH, URGENT */
    @Column(nullable = false, length = 20)
    private String priority = "NORMAL";

    /** OPEN, IN_PROGRESS, RESOLVED, CLOSED */
    @Column(nullable = false, length = 20)
    private String status = "OPEN";

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
