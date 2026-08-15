package com.restaurant.ai.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "automation_rules")
@Getter
@Setter
@NoArgsConstructor
public class AutomationRule {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "restaurant_id", nullable = false)
    private UUID restaurantId;

    @Column(nullable = false, length = 150)
    private String name;

    /** e.g. ORDER_STATUS_CHANGED, LEAD_CREATED, TICKET_CREATED */
    @Column(name = "trigger_type", nullable = false, length = 50)
    private String triggerType;

    /** JSON object of simple match conditions - see AutomationEngine for the matching rules. */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "condition_json", nullable = false)
    private String conditionJson = "{}";

    /** e.g. NOTIFY, SEND_FOLLOWUP, CREATE_LEAD, CREATE_TICKET, ESCALATE */
    @Column(name = "action_type", nullable = false, length = 50)
    private String actionType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "action_config_json", nullable = false)
    private String actionConfigJson = "{}";

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
