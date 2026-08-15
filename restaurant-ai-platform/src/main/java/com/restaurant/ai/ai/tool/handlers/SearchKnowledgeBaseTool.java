package com.restaurant.ai.ai.tool.handlers;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration.GeminiPropertySchema;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.ai.tool.ToolHandler;
import com.restaurant.ai.dto.KnowledgeDocumentResponse;
import com.restaurant.ai.service.KnowledgeDocumentService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class SearchKnowledgeBaseTool implements ToolHandler {

    private final KnowledgeDocumentService knowledgeDocumentService;

    public SearchKnowledgeBaseTool(KnowledgeDocumentService knowledgeDocumentService) {
        this.knowledgeDocumentService = knowledgeDocumentService;
    }

    @Override
    public String getName() {
        return "search_knowledge_base";
    }

    @Override
    public GeminiFunctionDeclaration getDeclaration() {
        return GeminiFunctionDeclaration.of(
                getName(),
                "Search the restaurant's verified policy/FAQ/allergy/refund/delivery documentation. Use this for " +
                        "any question about policies, allergens, refunds, or how something works that isn't covered " +
                        "by the menu or order tools - especially allergy and food-safety questions, where you must " +
                        "never answer from assumption. If this returns no results, say honestly that you don't have " +
                        "verified information on that and offer to connect the customer with a team member - never " +
                        "fill the gap with a guess.",
                Map.of("query", GeminiPropertySchema.string("What the customer is asking about, in a few keywords (e.g. 'peanut allergy', 'refund policy', 'late delivery').")),
                List.of("query")
        );
    }

    @Override
    public ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> args) {
        String query = (String) args.get("query");
        if (query == null || query.isBlank()) {
            return ToolExecutionResult.failure(getName(), "query is required.");
        }

        List<KnowledgeDocumentResponse> results = knowledgeDocumentService.search(context.restaurantId(), query);
        if (results.isEmpty()) {
            return ToolExecutionResult.ok(getName(), Map.of(
                    "found", false,
                    "note", "No verified documentation matches this query."
            ));
        }

        List<Map<String, Object>> payload = results.stream()
                .map(d -> Map.<String, Object>of("title", d.title(), "content", d.content()))
                .toList();
        return ToolExecutionResult.ok(getName(), Map.of("found", true, "documents", payload));
    }
}
