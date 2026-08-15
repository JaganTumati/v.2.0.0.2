package com.restaurant.ai.repository;

import com.restaurant.ai.entity.MenuItemOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MenuItemOptionRepository extends JpaRepository<MenuItemOption, UUID> {

    List<MenuItemOption> findByMenuItemIdAndActiveTrue(UUID menuItemId);

    List<MenuItemOption> findByMenuItemId(UUID menuItemId);
}
