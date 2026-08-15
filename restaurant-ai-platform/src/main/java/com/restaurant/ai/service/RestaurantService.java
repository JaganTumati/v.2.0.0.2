package com.restaurant.ai.service;

import com.restaurant.ai.dto.RestaurantResponse;
import com.restaurant.ai.dto.RestaurantUpdateRequest;
import com.restaurant.ai.entity.Restaurant;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.mapper.RestaurantMapper;
import com.restaurant.ai.repository.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;

    public RestaurantService(RestaurantRepository restaurantRepository) {
        this.restaurantRepository = restaurantRepository;
    }

    public RestaurantResponse get(UUID restaurantId) {
        Restaurant restaurant = findOrThrow(restaurantId);
        return RestaurantMapper.toResponse(restaurant);
    }

    @Transactional
    public RestaurantResponse update(UUID restaurantId, RestaurantUpdateRequest request) {
        Restaurant restaurant = findOrThrow(restaurantId);
        restaurant.setName(request.name());
        restaurant.setCuisineType(request.cuisineType());
        restaurant.setPhone(request.phone());
        restaurant.setStatus(request.status());
        restaurant.setAddressLine(request.addressLine());
        restaurant.setCity(request.city());
        restaurant.setState(request.state());
        restaurant.setPostalCode(request.postalCode());
        restaurant.setCountry(request.country());
        return RestaurantMapper.toResponse(restaurant);
    }

    private Restaurant findOrThrow(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> ResourceNotFoundException.of("Restaurant", restaurantId));
    }
}
