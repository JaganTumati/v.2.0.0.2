package com.restaurant.ai.repository;

import com.restaurant.ai.entity.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuCategoryRepository extends JpaRepository<MenuCategory, UUID> {

    List<MenuCategory> findByRestaurantIdOrderByDisplayOrderAsc(UUID restaurantId);

    Optional<MenuCategory> findByIdAndRestaurantId(UUID id, UUID restaurantId);

    boolean existsByRestaurantIdAndNameIgnoreCase(UUID restaurantId, String name);
}
