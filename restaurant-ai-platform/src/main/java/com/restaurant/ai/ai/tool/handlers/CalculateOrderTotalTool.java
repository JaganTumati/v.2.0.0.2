package com.restaurant.ai.ai.tool.handlers;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration.GeminiPropertySchema;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.ai.tool.ToolHandler;
import com.restaurant.ai.dto.OrderLineItemRequest;
import com.restaurant.ai.dto.OrderRequest;
import com.restaurant.ai.exception.BusinessRuleViolationException;
import com.restaurant.ai.service.OrderService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CalculateOrderTotalTool implements ToolHandler {

    private final OrderService orderService;

    public CalculateOrderTotalTool(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String getName() {
        return "calculate_order_total";
    }

    @Override
    public GeminiFunctionDeclaration getDeclaration() {
        return GeminiFunctionDeclaration.of(
                getName(),
                "Calculate the authoritative price for a set of items before ordering. Always use this instead of " +
                        "computing a total yourself - menu prices, delivery fees, minimum order rules, and promotion " +
                        "discounts are all applied here from live restaurant data.",
                Map.of(
                        "items", GeminiPropertySchema.arrayOfObjects(
                                "List of items to price.",
                                Map.of(
                                        "menuItemId", GeminiPropertySchema.string("The menu item id, from get_menu or search_menu."),
                                        "quantity", GeminiPropertySchema.integer("How many of this item.")
                                ),
                                List.of("menuItemId", "quantity")),
                        "fulfillmentType", GeminiPropertySchema.stringEnum("DELIVERY or TAKEAWAY.", List.of("DELIVERY", "TAKEAWAY")),
                        "deliveryZoneId", GeminiPropertySchema.string("Required if fulfillmentType is DELIVERY - the zone id from check_delivery_area."),
                        "promotionCode", GeminiPropertySchema.string("Optional promotion code the customer wants to apply.")
                ),
                List.of("items", "fulfillmentType")
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> args) {
        try {
            List<OrderLineItemRequest> items = parseItems((List<Map<String, Object>>) args.get("items"));
            String fulfillmentType = (String) args.get("fulfillmentType");
            UUID deliveryZoneId = args.get("deliveryZoneId") == null ? null : UUID.fromString((String) args.get("deliveryZoneId"));
            String promotionCode = (String) args.get("promotionCode");

            OrderRequest request = new OrderRequest(items, fulfillmentType, deliveryZoneId, null, promotionCode, null, null);
            OrderService.Quote quote = orderService.quote(context.restaurantId(), request);

            List<Map<String, Object>> lineItems = quote.lines().stream()
                    .map(l -> (Map<String, Object>) Map.<String, Object>of(
                            "name", l.name(), "quantity", l.quantity(), "unitPrice", l.unitPrice(), "lineTotal", l.lineTotal()))
                    .toList();

            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("items", lineItems);
            result.put("subtotal", quote.subtotal());
            result.put("deliveryFee", quote.deliveryFee());
            result.put("discountTotal", quote.discountTotal());
            result.put("taxTotal", quote.taxTotal());
            result.put("total", quote.total());
            if (quote.appliedPromotionCode() != null) {
                result.put("appliedPromotionCode", quote.appliedPromotionCode());
            }
            return ToolExecutionResult.ok(getName(), result);

        } catch (BusinessRuleViolationException e) {
            return ToolExecutionResult.failure(getName(), e.getMessage());
        } catch (Exception e) {
            return ToolExecutionResult.failure(getName(), "Couldn't calculate a total with those details.");
        }
    }

    private List<OrderLineItemRequest> parseItems(List<Map<String, Object>> raw) {
        return raw.stream()
                .map(m -> new OrderLineItemRequest(
                        UUID.fromString((String) m.get("menuItemId")),
                        ((Number) m.get("quantity")).intValue()))
                .toList();
    }
}
