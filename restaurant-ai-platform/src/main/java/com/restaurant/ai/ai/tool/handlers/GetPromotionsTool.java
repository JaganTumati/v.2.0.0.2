package com.restaurant.ai.ai.tool.handlers;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.ai.tool.ToolHandler;
import com.restaurant.ai.dto.PromotionResponse;
import com.restaurant.ai.service.PromotionService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class GetPromotionsTool implements ToolHandler {

    private final PromotionService promotionService;

    public GetPromotionsTool(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    @Override
    public String getName() {
        return "get_promotions";
    }

    @Override
    public GeminiFunctionDeclaration getDeclaration() {
        return GeminiFunctionDeclaration.of(
                getName(),
                "Get currently active, valid promotions for this restaurant. Never mention a discount, code, or " +
                        "offer that isn't returned by this tool.",
                Map.of(),
                List.of()
        );
    }

    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> args) {
        List<PromotionResponse> promos = promotionService.listCurrentlyValid(context.restaurantId());
        List<Map<String, Object>> payload = promos.stream()
                .map(p -> Map.<String, Object>of(
                        "code", p.code(),
                        "description", p.description() == null ? "" : p.description(),
                        "discountType", p.discountType(),
                        "discountValue", p.discountValue()))
                .toList();
        return ToolExecutionResult.ok(getName(), Map.of("promotions", payload));
    }
}
