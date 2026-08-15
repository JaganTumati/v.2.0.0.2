package com.restaurant.ai.repository;

import com.restaurant.ai.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {

    List<Lead> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);
}
