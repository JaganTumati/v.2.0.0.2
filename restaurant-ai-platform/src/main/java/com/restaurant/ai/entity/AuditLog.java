package com.restaurant.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
@Getter
@Setter
@NoArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "restaurant_id")
    private UUID restaurantId;

    @Column(name = "actor_id")
    private UUID actorId;

    /** USER, AI, SYSTEM */
    @Column(name = "actor_type", nullable = false, length = 20)
    private String actorType;

    @Column(nullable = false, length = 100)
    private String action;

    @Column(name = "target_type", length = 60)
    private String targetType;

    @Column(name = "target_id")
    private UUID targetId;

    /** SUCCESS, FAILURE, DENIED */
    @Column(nullable = false, length = 20)
    private String result;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "details_json")
    private String detailsJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
