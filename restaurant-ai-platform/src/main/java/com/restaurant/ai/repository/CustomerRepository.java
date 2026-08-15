package com.restaurant.ai.repository;

import com.restaurant.ai.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByRestaurantIdAndWhatsappNumber(UUID restaurantId, String whatsappNumber);

    Optional<Customer> findByIdAndRestaurantId(UUID id, UUID restaurantId);
}
