package com.restaurant.ai.repository;

import com.restaurant.ai.entity.WebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WebhookEventRepository extends JpaRepository<WebhookEvent, UUID> {

    boolean existsByWaMessageId(String waMessageId);
}
