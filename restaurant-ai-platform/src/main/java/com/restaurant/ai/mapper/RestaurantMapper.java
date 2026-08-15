package com.restaurant.ai.mapper;

import com.restaurant.ai.dto.RestaurantResponse;
import com.restaurant.ai.entity.Restaurant;

public final class RestaurantMapper {

    private RestaurantMapper() {
    }

    public static RestaurantResponse toResponse(Restaurant r) {
        return new RestaurantResponse(
                r.getId(),
                r.getName(),
                r.getCuisineType(),
                r.getPhone(),
                r.getCurrency(),
                r.getStatus(),
                r.getAddressLine(),
                r.getCity(),
                r.getState(),
                r.getPostalCode(),
                r.getCountry()
        );
    }
}
