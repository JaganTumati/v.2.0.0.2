package com.restaurant.ai.ai.tool.handlers;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration.GeminiPropertySchema;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.ai.tool.ToolHandler;
import com.restaurant.ai.dto.OrderLineItemRequest;
import com.restaurant.ai.dto.OrderRequest;
import com.restaurant.ai.dto.OrderResponse;
import com.restaurant.ai.exception.BusinessRuleViolationException;
import com.restaurant.ai.service.OrderService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CreateOrderTool implements ToolHandler {

    private final OrderService orderService;

    public CreateOrderTool(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String getName() {
        return "create_order";
    }

    @Override
    public GeminiFunctionDeclaration getDeclaration() {
        return GeminiFunctionDeclaration.of(
                getName(),
                "Create the order. Only call this AFTER you have shown the customer the full itemized total " +
                        "(via calculate_order_total) and they have explicitly confirmed they want to place the order - " +
                        "never call this on the customer's first mention of wanting items. idempotencyKey should be a " +
                        "value stable for this specific confirmed order (e.g. reuse it if you retry after an error).",
                Map.of(
                        "items", GeminiPropertySchema.arrayOfObjects(
                                "The confirmed items.",
                                Map.of(
                                        "menuItemId", GeminiPropertySchema.string("The menu item id."),
                                        "quantity", GeminiPropertySchema.integer("Quantity.")
                                ),
                                List.of("menuItemId", "quantity")),
                        "fulfillmentType", GeminiPropertySchema.stringEnum("DELIVERY or TAKEAWAY.", List.of("DELIVERY", "TAKEAWAY")),
                        "deliveryZoneId", GeminiPropertySchema.string("Required if fulfillmentType is DELIVERY."),
                        "deliveryAddress", GeminiPropertySchema.string("Required if fulfillmentType is DELIVERY."),
                        "promotionCode", GeminiPropertySchema.string("Optional promotion code."),
                        "specialInstructions", GeminiPropertySchema.string("Optional notes from the customer."),
                        "idempotencyKey", GeminiPropertySchema.string("A stable identifier for this confirmed order attempt.")
                ),
                List.of("items", "fulfillmentType", "idempotencyKey")
        );
    }

    @SuppressWarnings("unchecked")
    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> args) {
        if (context.customerId() == null) {
            return ToolExecutionResult.failure(getName(), "No customer is associated with this conversation yet.");
        }
        try {
            List<OrderLineItemRequest> items = ((List<Map<String, Object>>) args.get("items")).stream()
                    .map(m -> new OrderLineItemRequest(
                            UUID.fromString((String) m.get("menuItemId")),
                            ((Number) m.get("quantity")).intValue()))
                    .toList();

            String fulfillmentType = (String) args.get("fulfillmentType");
            UUID deliveryZoneId = args.get("deliveryZoneId") == null ? null : UUID.fromString((String) args.get("deliveryZoneId"));
            String deliveryAddress = (String) args.get("deliveryAddress");
            String promotionCode = (String) args.get("promotionCode");
            String specialInstructions = (String) args.get("specialInstructions");
            String idempotencyKey = (String) args.get("idempotencyKey");

            OrderRequest request = new OrderRequest(
                    items, fulfillmentType, deliveryZoneId, deliveryAddress, promotionCode, specialInstructions, idempotencyKey);

            OrderResponse order = orderService.createOrder(context.restaurantId(), context.customerId(), context.conversationId(), request);

            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("orderId", order.id().toString());
            result.put("status", order.status());
            result.put("total", order.total());
            return ToolExecutionResult.ok(getName(), result);

        } catch (BusinessRuleViolationException e) {
            return ToolExecutionResult.failure(getName(), e.getMessage());
        } catch (Exception e) {
            return ToolExecutionResult.failure(getName(), "Couldn't create the order with those details.");
        }
    }
}
