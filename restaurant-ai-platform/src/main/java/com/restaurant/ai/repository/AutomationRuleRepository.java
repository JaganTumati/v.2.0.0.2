package com.restaurant.ai.repository;

import com.restaurant.ai.entity.AutomationRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AutomationRuleRepository extends JpaRepository<AutomationRule, UUID> {

    List<AutomationRule> findByRestaurantIdAndTriggerTypeAndActiveTrue(UUID restaurantId, String triggerType);

    List<AutomationRule> findByRestaurantIdOrderByCreatedAtDesc(UUID restaurantId);
}
