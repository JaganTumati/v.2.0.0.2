package com.restaurant.ai.repository;

import com.restaurant.ai.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PromotionRepository extends JpaRepository<Promotion, UUID> {

    List<Promotion> findByRestaurantIdAndActiveTrue(UUID restaurantId);

    Optional<Promotion> findByRestaurantIdAndCodeIgnoreCase(UUID restaurantId, String code);
}
