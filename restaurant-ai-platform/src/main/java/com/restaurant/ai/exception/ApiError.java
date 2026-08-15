package com.restaurant.ai.exception;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiError(
        OffsetDateTime timestamp,
        int status,
        String error,
        String message,
        List<String> details
) {
    public static ApiError of(int status, String error, String message) {
        return new ApiError(OffsetDateTime.now(), status, error, message, List.of());
    }

    public static ApiError of(int status, String error, String message, List<String> details) {
        return new ApiError(OffsetDateTime.now(), status, error, message, details);
    }
}
