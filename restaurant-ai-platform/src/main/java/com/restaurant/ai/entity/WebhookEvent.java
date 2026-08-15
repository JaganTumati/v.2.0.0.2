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
@Table(name = "webhook_events")
@Getter
@Setter
@NoArgsConstructor
public class WebhookEvent {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "wa_message_id", nullable = false, length = 150)
    private String waMessageId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", nullable = false)
    private String payloadJson;

    /** RECEIVED, PROCESSING, PROCESSED, FAILED */
    @Column(nullable = false, length = 20)
    private String status = "RECEIVED";

    @Column(name = "processed_at")
    private OffsetDateTime processedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
