package com.restaurant.ai.config;

import com.restaurant.ai.repository.UserRepository;
import com.restaurant.ai.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Dev/MVP convenience only: on startup, if the single seeded restaurant has
 * no users yet, create a RESTAURANT_OWNER account with a random one-time
 * password logged to the console. This exists because V2__seed_dev_data.sql
 * can't bake in a real BCrypt hash (that requires the app's PasswordEncoder),
 * and hardcoding a known dev password in source would be a bad habit to
 * establish even for local-only use.
 *
 * Not something a real multi-tenant product would do at boot - Phase 25
 * replaces this with a proper signup/invite flow.
 */
@Component
public class DevUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DevUserSeeder.class);

    private final UserRepository userRepository;
    private final UserService userService;
    private final RestaurantProperties restaurantProperties;

    public DevUserSeeder(UserRepository userRepository, UserService userService, RestaurantProperties restaurantProperties) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.restaurantProperties = restaurantProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (userRepository.countByRestaurantId(restaurantProperties.getDefaultId()) > 0) {
            return;
        }
        String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 12);
        userService.createUser(
                restaurantProperties.getDefaultId(), "Dev Owner", "owner@spiceroute.test", tempPassword, "RESTAURANT_OWNER");

        log.warn("======================================================================");
        log.warn(" No dashboard users existed - created a default owner account:");
        log.warn("   email:    owner@spiceroute.test");
        log.warn("   password: {}", tempPassword);
        log.warn(" This is logged once. Change it via the dashboard once user management is built.");
        log.warn("======================================================================");
    }
}
