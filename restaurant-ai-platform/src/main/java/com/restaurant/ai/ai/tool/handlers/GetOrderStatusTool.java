package com.restaurant.ai.ai.tool.handlers;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration.GeminiPropertySchema;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.ai.tool.ToolHandler;
import com.restaurant.ai.dto.OrderResponse;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.service.OrderService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GetOrderStatusTool implements ToolHandler {

    private final OrderService orderService;

    public GetOrderStatusTool(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String getName() {
        return "get_order_status";
    }

    @Override
    public GeminiFunctionDeclaration getDeclaration() {
        return GeminiFunctionDeclaration.of(
                getName(),
                "Get the authoritative current status of an order. Never guess or assume a status - always call this.",
                Map.of("orderId", GeminiPropertySchema.string("The order id.")),
                List.of("orderId")
        );
    }

    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> args) {
        if (context.customerId() == null) {
            return ToolExecutionResult.failure(getName(), "No customer is associated with this conversation yet.");
        }
        try {
            UUID orderId = UUID.fromString((String) args.get("orderId"));
            OrderResponse order = orderService.getStatus(context.restaurantId(), context.customerId(), orderId);

            Map<String, Object> result = new java.util.LinkedHashMap<>();
            result.put("orderId", order.id().toString());
            result.put("status", order.status());
            result.put("total", order.total());
            return ToolExecutionResult.ok(getName(), result);

        } catch (ResourceNotFoundException e) {
            return ToolExecutionResult.failure(getName(), "No such order for this customer.");
        } catch (Exception e) {
            return ToolExecutionResult.failure(getName(), "Couldn't look up that order.");
        }
    }
}
