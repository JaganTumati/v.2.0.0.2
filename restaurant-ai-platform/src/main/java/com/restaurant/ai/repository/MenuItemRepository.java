package com.restaurant.ai.repository;

import com.restaurant.ai.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItem, UUID> {

    List<MenuItem> findByRestaurantId(UUID restaurantId);

    List<MenuItem> findByRestaurantIdAndCategoryId(UUID restaurantId, UUID categoryId);

    List<MenuItem> findByRestaurantIdAndAvailableTrue(UUID restaurantId);

    Optional<MenuItem> findByIdAndRestaurantId(UUID id, UUID restaurantId);

    /**
     * Backing query for the future search_menu Claude tool: all filters optional,
     * always scoped to a single restaurant, only ever returns available items
     * unless includeUnavailable is explicitly true (dashboard use only).
     */
    @Query("""
        SELECT m FROM MenuItem m
        WHERE m.restaurantId = :restaurantId
          AND (:categoryId IS NULL OR m.categoryId = :categoryId)
          AND (:vegetarian IS NULL OR m.vegetarian = :vegetarian)
          AND (:maxPrice IS NULL OR m.price <= :maxPrice)
          AND (:maxSpicyLevel IS NULL OR m.spicyLevel <= :maxSpicyLevel)
          AND (:includeUnavailable = true OR m.available = true)
          AND (:searchText IS NULL OR
               LOWER(m.name) LIKE LOWER(CONCAT('%', :searchText, '%')) OR
               LOWER(m.description) LIKE LOWER(CONCAT('%', :searchText, '%')))
        ORDER BY m.name ASC
        """)
    List<MenuItem> search(
            @Param("restaurantId") UUID restaurantId,
            @Param("categoryId") UUID categoryId,
            @Param("vegetarian") Boolean vegetarian,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("maxSpicyLevel") Short maxSpicyLevel,
            @Param("searchText") String searchText,
            @Param("includeUnavailable") boolean includeUnavailable
    );
}
