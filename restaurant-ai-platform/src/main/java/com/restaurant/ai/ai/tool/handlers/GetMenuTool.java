package com.restaurant.ai.ai.tool.handlers;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration.GeminiPropertySchema;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.ai.tool.ToolHandler;
import com.restaurant.ai.dto.MenuCategoryResponse;
import com.restaurant.ai.dto.MenuItemResponse;
import com.restaurant.ai.service.MenuCategoryService;
import com.restaurant.ai.service.MenuItemService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GetMenuTool implements ToolHandler {

    private final MenuItemService menuItemService;
    private final MenuCategoryService menuCategoryService;

    public GetMenuTool(MenuItemService menuItemService, MenuCategoryService menuCategoryService) {
        this.menuItemService = menuItemService;
        this.menuCategoryService = menuCategoryService;
    }

    @Override
    public String getName() {
        return "get_menu";
    }

    @Override
    public GeminiFunctionDeclaration getDeclaration() {
        return GeminiFunctionDeclaration.of(
                getName(),
                "Retrieve the restaurant's currently available menu, optionally filtered by category name. " +
                        "Only returns verified, currently available items - never invent menu items, prices, or " +
                        "availability beyond what this tool returns.",
                Map.of(
                        "categoryName", GeminiPropertySchema.string(
                                "Optional category name to filter by, e.g. 'Biryani', 'Starters', 'Beverages'. Omit to get the full menu.")
                ),
                List.of()
        );
    }

    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> args) {
        String categoryName = args == null ? null : (String) args.get("categoryName");

        UUID categoryId = null;
        if (categoryName != null && !categoryName.isBlank()) {
            List<MenuCategoryResponse> categories = menuCategoryService.list(context.restaurantId());
            categoryId = categories.stream()
                    .filter(c -> c.name().equalsIgnoreCase(categoryName.trim()))
                    .map(MenuCategoryResponse::id)
                    .findFirst()
                    .orElse(null);
            if (categoryId == null) {
                // Not an error - just means nothing matches, Gemini should report that honestly.
                return ToolExecutionResult.ok(getName(), Map.of(
                        "items", List.of(),
                        "note", "No category found matching '" + categoryName + "'."
                ));
            }
        }

        List<MenuItemResponse> items = menuItemService.list(context.restaurantId(), categoryId, false);
        return ToolExecutionResult.ok(getName(), Map.of("items", toToolPayload(items)));
    }

    private List<Map<String, Object>> toToolPayload(List<MenuItemResponse> items) {
        return items.stream().map(i -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", i.id().toString());
            m.put("name", i.name());
            m.put("description", i.description());
            m.put("price", i.price());
            m.put("category", i.categoryName());
            m.put("vegetarian", i.vegetarian());
            m.put("spicyLevel", i.spicyLevel());
            m.put("available", i.available());
            return m;
        }).toList();
    }
}
