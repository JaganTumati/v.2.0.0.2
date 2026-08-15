package com.restaurant.ai.repository;

import com.restaurant.ai.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    List<OrderItem> findByOrderId(UUID orderId);

    void deleteByOrderId(UUID orderId);

    /**
     * Popularity ranking across all orders for a restaurant. Native query since
     * OrderItem has no JPA relation to Order (only a raw orderId column), and a
     * JPQL join can't traverse that without one.
     */
    @Query(value = """
            SELECT oi.item_name_snapshot AS itemName, SUM(oi.quantity) AS totalQuantity
            FROM order_items oi
            JOIN orders o ON o.id = oi.order_id
            WHERE o.restaurant_id = :restaurantId
              AND o.status <> 'CANCELLED'
            GROUP BY oi.item_name_snapshot
            ORDER BY SUM(oi.quantity) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<ItemPopularity> findMenuPopularity(@Param("restaurantId") UUID restaurantId, @Param("limit") int limit);

    interface ItemPopularity {
        String getItemName();
        Long getTotalQuantity();
    }
}
