package com.restaurant.ai.service;

import java.util.Map;
import java.util.Set;

/**
 * DRAFT -> CONFIRMATION_REQUIRED -> CONFIRMED -> PREPARING -> READY ->
 * OUT_FOR_DELIVERY -> DELIVERED, with CANCELLED reachable from any
 * pre-PREPARING state. Nothing outside this class decides whether a
 * transition is legal (Phase 12 requirement: e.g. DELIVERED -> PREPARING
 * must always be rejected, regardless of caller).
 */
public final class OrderStateMachine {

    private static final Map<String, Set<String>> ALLOWED_TRANSITIONS = Map.of(
            "DRAFT", Set.of("CONFIRMATION_REQUIRED", "CANCELLED"),
            "CONFIRMATION_REQUIRED", Set.of("CONFIRMED", "CANCELLED", "DRAFT"),
            "CONFIRMED", Set.of("PREPARING", "CANCELLED"),
            "PREPARING", Set.of("READY", "CANCELLED"),
            "READY", Set.of("OUT_FOR_DELIVERY", "CANCELLED"),
            "OUT_FOR_DELIVERY", Set.of("DELIVERED"),
            "DELIVERED", Set.of(),
            "CANCELLED", Set.of()
    );

    private OrderStateMachine() {
    }

    public static boolean isTransitionAllowed(String fromStatus, String toStatus) {
        Set<String> allowed = ALLOWED_TRANSITIONS.get(fromStatus);
        return allowed != null && allowed.contains(toStatus);
    }

    public static boolean isCancellable(String currentStatus) {
        return isTransitionAllowed(currentStatus, "CANCELLED");
    }
}
