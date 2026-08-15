package com.restaurant.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.UUID;

/**
 * MVP is single-restaurant, so "current restaurant" is a fixed config value
 * rather than resolved from an authenticated tenant context. When multi-tenancy
 * is introduced, this bean is replaced by a per-request resolver (e.g. from JWT
 * claims) without touching the services that consume it, since they only ever
 * receive a restaurantId parameter.
 */
@ConfigurationProperties(prefix = "app.restaurant")
public class RestaurantProperties {

    private UUID defaultId;

    public UUID getDefaultId() {
        return defaultId;
    }

    public void setDefaultId(UUID defaultId) {
        this.defaultId = defaultId;
    }
}
