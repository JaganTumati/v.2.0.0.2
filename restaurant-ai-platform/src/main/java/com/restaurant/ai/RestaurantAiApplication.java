package com.restaurant.ai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class RestaurantAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestaurantAiApplication.class, args);
    }
}
