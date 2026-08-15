package com.restaurant.ai.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public static ResourceNotFoundException of(String type, Object id) {
        return new ResourceNotFoundException(type + " not found: " + id);
    }
}
