package com.restaurant.ai.repository;

import com.restaurant.ai.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConversationRepository extends JpaRepository<Conversation, UUID> {

    /** Most recent non-closed conversation for a customer, if any - used to continue rather than fork a new thread. */
    Optional<Conversation> findFirstByRestaurantIdAndCustomerIdAndModeNotOrderByCreatedAtDesc(
            UUID restaurantId, UUID customerId, String excludedMode);

    Optional<Conversation> findByIdAndRestaurantId(UUID id, UUID restaurantId);

    List<Conversation> findByRestaurantIdAndModeOrderByUpdatedAtDesc(UUID restaurantId, String mode);

    List<Conversation> findByRestaurantIdOrderByUpdatedAtDesc(UUID restaurantId);

    long countByRestaurantId(UUID restaurantId);

    long countByRestaurantIdAndCreatedAtAfter(UUID restaurantId, java.time.OffsetDateTime after);

    long countByRestaurantIdAndModeIn(UUID restaurantId, List<String> modes);
}
