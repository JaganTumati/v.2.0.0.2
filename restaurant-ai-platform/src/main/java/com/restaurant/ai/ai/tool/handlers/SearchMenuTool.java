package com.restaurant.ai.ai.tool.handlers;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration.GeminiPropertySchema;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.ai.tool.ToolHandler;
import com.restaurant.ai.dto.MenuItemResponse;
import com.restaurant.ai.service.MenuItemService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class SearchMenuTool implements ToolHandler {

    private final MenuItemService menuItemService;

    public SearchMenuTool(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @Override
    public String getName() {
        return "search_menu";
    }

    @Override
    public GeminiFunctionDeclaration getDeclaration() {
        return GeminiFunctionDeclaration.of(
                getName(),
                "Find menu items matching customer preferences. All filters are optional and combine with AND. " +
                        "Only returns currently available items. Use this instead of guessing when a customer gives " +
                        "constraints like budget, spice level, or dietary preference.",
                Map.of(
                        "vegetarian", GeminiPropertySchema.bool("True to return only vegetarian items."),
                        "maxPrice", GeminiPropertySchema.number("Maximum price in the restaurant's currency."),
                        "maxSpicyLevel", GeminiPropertySchema.integer("Maximum spice level, 0 (mild) to 3 (very spicy)."),
                        "searchText", GeminiPropertySchema.string("Free-text search against item name/description, e.g. 'chicken' or 'paneer'.")
                ),
                List.of()
        );
    }

    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> args) {
        Boolean vegetarian = args.get("vegetarian") == null ? null : (Boolean) args.get("vegetarian");
        BigDecimal maxPrice = args.get("maxPrice") == null ? null : new BigDecimal(args.get("maxPrice").toString());
        Short maxSpicyLevel = args.get("maxSpicyLevel") == null ? null
                : Short.valueOf(String.valueOf(((Number) args.get("maxSpicyLevel")).intValue()));
        String searchText = args.get("searchText") == null ? null : (String) args.get("searchText");

        List<MenuItemResponse> items = menuItemService.search(
                context.restaurantId(), null, vegetarian, maxPrice, maxSpicyLevel, searchText);

        List<Map<String, Object>> payload = items.stream().map(i -> {
            Map<String, Object> m = new java.util.LinkedHashMap<>();
            m.put("id", i.id().toString());
            m.put("name", i.name());
            m.put("price", i.price());
            m.put("category", i.categoryName());
            m.put("vegetarian", i.vegetarian());
            m.put("spicyLevel", i.spicyLevel());
            return m;
        }).toList();

        return ToolExecutionResult.ok(getName(), Map.of("items", payload, "count", payload.size()));
    }
}
