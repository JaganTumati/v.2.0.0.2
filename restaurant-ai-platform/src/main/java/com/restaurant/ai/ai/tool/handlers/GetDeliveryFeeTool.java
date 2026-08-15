package com.restaurant.ai.ai.tool.handlers;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration.GeminiPropertySchema;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.ai.tool.ToolHandler;
import com.restaurant.ai.entity.DeliveryZone;
import com.restaurant.ai.exception.ResourceNotFoundException;
import com.restaurant.ai.service.DeliveryZoneService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class GetDeliveryFeeTool implements ToolHandler {

    private final DeliveryZoneService deliveryZoneService;

    public GetDeliveryFeeTool(DeliveryZoneService deliveryZoneService) {
        this.deliveryZoneService = deliveryZoneService;
    }

    @Override
    public String getName() {
        return "get_delivery_fee";
    }

    @Override
    public GeminiFunctionDeclaration getDeclaration() {
        return GeminiFunctionDeclaration.of(
                getName(),
                "Get the authoritative delivery fee for a specific zone id, typically obtained from check_delivery_area.",
                Map.of("deliveryZoneId", GeminiPropertySchema.string("The zone id.")),
                List.of("deliveryZoneId")
        );
    }

    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> args) {
        try {
            UUID zoneId = UUID.fromString((String) args.get("deliveryZoneId"));
            DeliveryZone zone = deliveryZoneService.getOrThrow(context.restaurantId(), zoneId);
            return ToolExecutionResult.ok(getName(), Map.of(
                    "deliveryZoneId", zoneId.toString(),
                    "deliveryFee", zone.getDeliveryFee(),
                    "estimatedMinutes", zone.getEstimatedMinutes() == null ? "" : zone.getEstimatedMinutes()
            ));
        } catch (ResourceNotFoundException e) {
            return ToolExecutionResult.failure(getName(), "No such delivery zone.");
        } catch (Exception e) {
            return ToolExecutionResult.failure(getName(), "Couldn't look up that delivery fee.");
        }
    }
}
