package com.restaurant.ai.service;

import com.restaurant.ai.entity.Customer;
import com.restaurant.ai.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    /** Find the customer for this WhatsApp number, or create one on first contact. */
    public Customer findOrCreate(UUID restaurantId, String whatsappNumber, String displayNameIfNew) {
        return customerRepository.findByRestaurantIdAndWhatsappNumber(restaurantId, whatsappNumber)
                .orElseGet(() -> {
                    Customer customer = new Customer();
                    customer.setRestaurantId(restaurantId);
                    customer.setWhatsappNumber(whatsappNumber);
                    customer.setDisplayName(displayNameIfNew);
                    return customerRepository.save(customer);
                });
    }
}
