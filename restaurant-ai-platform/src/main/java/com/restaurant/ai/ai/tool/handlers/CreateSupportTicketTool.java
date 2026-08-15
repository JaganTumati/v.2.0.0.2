package com.restaurant.ai.ai.tool.handlers;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration.GeminiPropertySchema;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.ai.tool.ToolHandler;
import com.restaurant.ai.dto.SupportTicketResponse;
import com.restaurant.ai.service.AutomationEngine;
import com.restaurant.ai.service.SupportTicketService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class CreateSupportTicketTool implements ToolHandler {

    private final SupportTicketService supportTicketService;
    private final AutomationEngine automationEngine;

    public CreateSupportTicketTool(SupportTicketService supportTicketService, AutomationEngine automationEngine) {
        this.supportTicketService = supportTicketService;
        this.automationEngine = automationEngine;
    }

    @Override
    public String getName() {
        return "create_support_ticket";
    }

    @Override
    public GeminiFunctionDeclaration getDeclaration() {
        return GeminiFunctionDeclaration.of(
                getName(),
                "Create a support ticket for a complaint or issue that needs staff follow-up (wrong item, missing " +
                        "item, quality issue, etc.). This does not itself resolve anything or authorize a refund - " +
                        "use escalate_to_human as well if the customer needs to talk to someone now.",
                Map.of(
                        "issue", GeminiPropertySchema.string("A clear description of the problem."),
                        "orderId", GeminiPropertySchema.string("The related order id, if any."),
                        "priority", GeminiPropertySchema.stringEnum("Urgency.", List.of("LOW", "NORMAL", "HIGH", "URGENT"))
                ),
                List.of("issue")
        );
    }

    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> args) {
        String issue = (String) args.get("issue");
        String priority = (String) args.get("priority");
        UUID orderId = args.get("orderId") == null ? null : safeUuid((String) args.get("orderId"));

        SupportTicketResponse ticket = supportTicketService.create(
                context.restaurantId(), context.conversationId(), orderId, issue, priority);

        java.util.Map<String, Object> eventData = new java.util.HashMap<>();
        eventData.put("ticketId", ticket.id());
        eventData.put("priority", ticket.priority());
        eventData.put("conversationId", context.conversationId());
        eventData.put("orderId", orderId);
        automationEngine.fire(context.restaurantId(), "TICKET_CREATED", eventData);

        return ToolExecutionResult.ok(getName(), Map.of(
                "ticketId", ticket.id().toString(), "status", ticket.status(), "priority", ticket.priority()));
    }

    private UUID safeUuid(String raw) {
        try {
            return UUID.fromString(raw);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
