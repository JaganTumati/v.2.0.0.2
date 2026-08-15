package com.restaurant.ai.ai.tool.handlers;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration.GeminiPropertySchema;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.ai.tool.ToolHandler;
import com.restaurant.ai.dto.DeliveryZoneResponse;
import com.restaurant.ai.service.DeliveryZoneService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class CheckDeliveryAreaTool implements ToolHandler {

    private final DeliveryZoneService deliveryZoneService;

    public CheckDeliveryAreaTool(DeliveryZoneService deliveryZoneService) {
        this.deliveryZoneService = deliveryZoneService;
    }

    @Override
    public String getName() {
        return "check_delivery_area";
    }

    @Override
    public GeminiFunctionDeclaration getDeclaration() {
        return GeminiFunctionDeclaration.of(
                getName(),
                "Check whether a postal code is within a deliverable zone, and get that zone's id/fee/minimum " +
                        "order/estimated time if so. Never assume an address is deliverable without checking this.",
                Map.of("postalCode", GeminiPropertySchema.string("The customer's postal/PIN code.")),
                List.of("postalCode")
        );
    }

    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> args) {
        String postalCode = (String) args.get("postalCode");
        if (postalCode == null || postalCode.isBlank()) {
            return ToolExecutionResult.failure(getName(), "postalCode is required.");
        }

        Optional<DeliveryZoneResponse> zone = deliveryZoneService.findZoneForPostalCode(context.restaurantId(), postalCode);
        if (zone.isEmpty()) {
            return ToolExecutionResult.ok(getName(), Map.of("deliverable", false));
        }

        DeliveryZoneResponse z = zone.get();
        Map<String, Object> result = new java.util.LinkedHashMap<>();
        result.put("deliverable", true);
        result.put("deliveryZoneId", z.id().toString());
        result.put("deliveryFee", z.deliveryFee());
        result.put("minOrderAmount", z.minOrderAmount());
        result.put("estimatedMinutes", z.estimatedMinutes());
        return ToolExecutionResult.ok(getName(), result);
    }
}
