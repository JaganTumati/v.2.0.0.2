package com.restaurant.ai.repository;

import com.restaurant.ai.entity.DeliveryZone;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeliveryZoneRepository extends JpaRepository<DeliveryZone, UUID> {

    List<DeliveryZone> findByRestaurantIdAndActiveTrue(UUID restaurantId);
}
