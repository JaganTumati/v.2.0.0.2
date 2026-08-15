package com.restaurant.ai.audit;

import com.restaurant.ai.entity.AuditLog;
import com.restaurant.ai.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Thin write-only audit logger. Called from domain services for
 * privileged/sensitive operations (Phase 20) and, from Phase 6 onward,
 * from ClaudeToolExecutor for every tool invocation.
 *
 * Never log secrets or full customer PII beyond what's already an id.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(UUID restaurantId, UUID actorId, String actorType, String action,
                     String targetType, UUID targetId, String result, String correlationId) {
        AuditLog entry = new AuditLog();
        entry.setRestaurantId(restaurantId);
        entry.setActorId(actorId);
        entry.setActorType(actorType);
        entry.setAction(action);
        entry.setTargetType(targetType);
        entry.setTargetId(targetId);
        entry.setResult(result);
        entry.setCorrelationId(correlationId);
        auditLogRepository.save(entry);
    }
}
