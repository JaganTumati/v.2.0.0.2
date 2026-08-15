package com.restaurant.ai.ai.tool.handlers;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration.GeminiPropertySchema;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.ai.tool.ToolHandler;
import com.restaurant.ai.dto.OrderResponse;
import com.restaurant.ai.exception.BusinessRuleViolationException;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.service.OrderService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CancelOrderTool implements ToolHandler {

    private final OrderService orderService;

    public CancelOrderTool(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public String getName() {
        return "cancel_order";
    }

    @Override
    public GeminiFunctionDeclaration getDeclaration() {
        return GeminiFunctionDeclaration.of(
                getName(),
                "Cancel an order if it's still eligible (not yet preparing or beyond). Never assume cancellation " +
                        "is allowed - this checks the order's current status and the restaurant's rules. Only the " +
                        "customer's own order can be cancelled.",
                Map.of("orderId", GeminiPropertySchema.string("The order id to cancel.")),
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
            // Ownership is enforced inside OrderService, keyed off context.customerId() -
            // never off anything Gemini/the customer supplied in args.
            OrderResponse cancelled = orderService.cancelOrder(context.restaurantId(), context.customerId(), orderId);
            return ToolExecutionResult.ok(getName(), Map.of("orderId", orderId.toString(), "status", cancelled.status()));

        } catch (ResourceNotFoundException e) {
            return ToolExecutionResult.failure(getName(), "No such order for this customer.");
        } catch (BusinessRuleViolationException e) {
            return ToolExecutionResult.failure(getName(), e.getMessage());
        } catch (Exception e) {
            return ToolExecutionResult.failure(getName(), "Couldn't cancel that order.");
        }
    }
}
