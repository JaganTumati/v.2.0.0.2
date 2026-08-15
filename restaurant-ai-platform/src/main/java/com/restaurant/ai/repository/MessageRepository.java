package com.restaurant.ai.repository;

import com.restaurant.ai.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MessageRepository extends JpaRepository<Message, UUID> {

    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    List<Message> findTop20ByConversationIdOrderByCreatedAtDesc(UUID conversationId);

    Optional<Message> findTopByConversationIdAndDirectionOrderByCreatedAtDesc(UUID conversationId, String direction);

    Optional<Message> findByWaMessageId(String waMessageId);

    boolean existsByWaMessageId(String waMessageId);
}
