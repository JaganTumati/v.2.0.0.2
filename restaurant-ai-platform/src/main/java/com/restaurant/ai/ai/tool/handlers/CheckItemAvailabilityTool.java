package com.restaurant.ai.ai.tool.handlers;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration.GeminiPropertySchema;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.ai.tool.ToolHandler;
import com.restaurant.ai.exception.ResourceNotFoundException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CheckItemAvailabilityTool implements ToolHandler {

    private final com.restaurant.ai.service.MenuItemService menuItemService;

    public CheckItemAvailabilityTool(com.restaurant.ai.service.MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @Override
    public String getName() {
        return "check_item_availability";
    }

    @Override
    public GeminiFunctionDeclaration getDeclaration() {
        return GeminiFunctionDeclaration.of(
                getName(),
                "Check whether a specific menu item (by its id, from get_menu or search_menu results) is " +
                        "currently available. Never assume an item is available without calling this.",
                Map.of(
                        "menuItemId", GeminiPropertySchema.string("The id of the menu item to check, as returned by get_menu or search_menu.")
                ),
                List.of("menuItemId")
        );
    }

    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> args) {
        String rawId = (String) args.get("menuItemId");
        if (rawId == null || rawId.isBlank()) {
            return ToolExecutionResult.failure(getName(), "menuItemId is required.");
        }

        UUID itemId;
        try {
            itemId = UUID.fromString(rawId.trim());
        } catch (IllegalArgumentException e) {
            return ToolExecutionResult.failure(getName(), "menuItemId is not a valid identifier.");
        }

        try {
            boolean available = menuItemService.checkAvailability(context.restaurantId(), itemId);
            return ToolExecutionResult.ok(getName(), Map.of("menuItemId", rawId, "available", available));
        } catch (ResourceNotFoundException e) {
            return ToolExecutionResult.failure(getName(), "No such menu item.");
        }
    }
}
