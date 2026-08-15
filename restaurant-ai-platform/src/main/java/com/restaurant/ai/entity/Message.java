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
@Table(name = "messages")
@Getter
@Setter
@NoArgsConstructor
public class Message {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;

    /** INBOUND, OUTBOUND */
    @Column(nullable = false, length = 10)
    private String direction;

    /** CUSTOMER, AI, AGENT */
    @Column(name = "sender_type", nullable = false, length = 10)
    private String senderType;

    @Column(name = "sender_user_id")
    private UUID senderUserId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "wa_message_id", length = 150)
    private String waMessageId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "tool_calls")
    private String toolCallsJson;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
