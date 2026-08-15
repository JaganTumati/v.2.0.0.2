package com.restaurant.ai.repository;

import com.restaurant.ai.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findByIdAndRestaurantId(UUID id, UUID restaurantId);

    Optional<Order> findByIdempotencyKey(String idempotencyKey);

    List<Order> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);

    long countByRestaurantIdAndCreatedAtAfter(UUID restaurantId, OffsetDateTime after);

    long countByRestaurantIdAndStatusNotIn(UUID restaurantId, List<String> excludedStatuses);

    @Query("""
            SELECT COALESCE(SUM(o.total), 0) FROM Order o
            WHERE o.restaurantId = :restaurantId
              AND o.createdAt >= :since
              AND o.status <> 'CANCELLED'
            """)
    BigDecimal sumRevenueSince(@Param("restaurantId") UUID restaurantId, @Param("since") OffsetDateTime since);
}
