package com.restaurant.ai.ai.tool.handlers;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration.GeminiPropertySchema;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.ai.tool.ToolHandler;
import com.restaurant.ai.service.ConversationService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class EscalateToHumanTool implements ToolHandler {

    private final ConversationService conversationService;

    public EscalateToHumanTool(ConversationService conversationService) {
        this.conversationService = conversationService;
    }

    @Override
    public String getName() {
        return "escalate_to_human";
    }

    @Override
    public GeminiFunctionDeclaration getDeclaration() {
        return GeminiFunctionDeclaration.of(
                getName(),
                "Hand this conversation off to a human team member. Use this when the customer explicitly asks " +
                        "for a human, is highly dissatisfied, needs a refund/compensation decision, raises a serious " +
                        "food-safety or severe-allergy concern, or asks something outside what you can verify or do. " +
                        "After calling this, give the customer one final reassuring message that a team member will " +
                        "follow up - you will not automatically respond to their next messages in this conversation.",
                Map.of("reason", GeminiPropertySchema.string("A short summary of why this is being escalated, for the human agent.")),
                List.of("reason")
        );
    }

    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> args) {
        if (context.conversationId() == null) {
            return ToolExecutionResult.failure(getName(), "No active conversation to escalate.");
        }
        String reason = (String) args.get("reason");
        conversationService.escalateToHuman(context.conversationId(), reason);
        return ToolExecutionResult.ok(getName(), Map.of("escalated", true));
    }
}
