package com.restaurant.ai.repository;

import com.restaurant.ai.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {

    List<SupportTicket> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);

    Optional<SupportTicket> findByIdAndRestaurantId(UUID id, UUID restaurantId);
}
