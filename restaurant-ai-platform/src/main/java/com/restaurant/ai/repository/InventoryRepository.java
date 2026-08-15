package com.restaurant.ai.repository;

import com.restaurant.ai.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByMenuItemId(UUID menuItemId);
}
