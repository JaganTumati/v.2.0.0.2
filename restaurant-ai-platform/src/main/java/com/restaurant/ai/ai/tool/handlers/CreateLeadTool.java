package com.restaurant.ai.ai.tool.handlers;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration.GeminiPropertySchema;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.ai.tool.ToolHandler;
import com.restaurant.ai.dto.LeadResponse;
import com.restaurant.ai.service.AutomationEngine;
import com.restaurant.ai.service.LeadService;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Component
public class CreateLeadTool implements ToolHandler {

    private final LeadService leadService;
    private final AutomationEngine automationEngine;

    public CreateLeadTool(LeadService leadService, AutomationEngine automationEngine) {
        this.leadService = leadService;
        this.automationEngine = automationEngine;
    }

    @Override
    public String getName() {
        return "create_lead";
    }

    @Override
    public GeminiFunctionDeclaration getDeclaration() {
        return GeminiFunctionDeclaration.of(
                getName(),
                "Create a sales lead when a customer shows interest in catering, a bulk/event order, or repeated " +
                        "purchase intent beyond a normal order. Do not use this for a normal single order - use " +
                        "create_order for that.",
                Map.of(
                        "source", GeminiPropertySchema.stringEnum(
                                "The type of opportunity.", List.of("CATERING", "BULK_ORDER", "EVENT", "REPEAT_INTENT")),
                        "intentNotes", GeminiPropertySchema.string("A short summary of what the customer is looking for (event type, date, guest count, preferences, etc.)."),
                        "estimatedValue", GeminiPropertySchema.number("Optional rough estimated order value if mentioned.")
                ),
                List.of("source", "intentNotes")
        );
    }

    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> args) {
        String source = (String) args.get("source");
        String intentNotes = (String) args.get("intentNotes");
        BigDecimal estimatedValue = args.get("estimatedValue") == null
                ? null : new BigDecimal(args.get("estimatedValue").toString());

        LeadResponse lead = leadService.create(
                context.restaurantId(), context.customerId(), context.conversationId(), source, intentNotes, estimatedValue);

        automationEngine.fire(context.restaurantId(), "LEAD_CREATED", buildEventData(
                lead.id(), source, estimatedValue, context.customerId(), context.conversationId()));

        return ToolExecutionResult.ok(getName(), Map.of("leadId", lead.id().toString(), "status", lead.status()));
    }

    private Map<String, Object> buildEventData(java.util.UUID leadId, String source, BigDecimal estimatedValue,
                                                 java.util.UUID customerId, java.util.UUID conversationId) {
        // Mutable map, not Map.of() - several of these fields are legitimately null
        // (no estimated value given, no customer/conversation in a given context),
        // and AutomationEngine's action handlers cast typed fields like UUIDs directly.
        java.util.Map<String, Object> eventData = new java.util.HashMap<>();
        eventData.put("leadId", leadId);
        eventData.put("source", source);
        eventData.put("estimatedValue", estimatedValue);
        eventData.put("customerId", customerId);
        eventData.put("conversationId", conversationId);
        return eventData;
    }
}
