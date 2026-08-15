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
@Table(name = "leads")
@Getter
@Setter
@NoArgsConstructor
public class Lead {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "conversation_id")
    private UUID conversationId;

    /** CATERING, BULK_ORDER, EVENT, REPEAT_INTENT */
    @Column(length = 50)
    private String source;

    @Column(name = "intent_notes", columnDefinition = "TEXT")
    private String intentNotes;

    @Column(name = "estimated_value", precision = 12, scale = 2)
    private BigDecimal estimatedValue;

    /** NEW, CONTACTED, QUALIFIED, CONVERTED, LOST */
    @Column(nullable = false, length = 20)
    private String status = "NEW";

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
