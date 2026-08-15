package com.restaurant.ai.dto;

public record AuthTokenResponse(
        String accessToken,
        String refreshToken,
        String role,
        String email
) {
}
