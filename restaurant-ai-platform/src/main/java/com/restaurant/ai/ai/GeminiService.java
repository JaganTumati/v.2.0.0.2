package com.restaurant.ai.ai;

import com.restaurant.ai.ai.gemini.GeminiContent;
import com.restaurant.ai.ai.gemini.GeminiGenerateRequest;
import com.restaurant.ai.ai.gemini.GeminiGenerateResponse;
import com.restaurant.ai.ai.gemini.GeminiPart;
import com.restaurant.ai.ai.tool.GeminiToolExecutor;
import com.restaurant.ai.ai.tool.GeminiToolRegistry;
import com.restaurant.ai.ai.tool.ToolExecutionContext;
import com.restaurant.ai.ai.tool.ToolExecutionResult;
import com.restaurant.ai.config.GeminiProperties;
import com.restaurant.ai.dto.RestaurantResponse;
import com.restaurant.ai.integration.GeminiClient;
import com.restaurant.ai.service.RestaurantService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class GeminiService {

    private static final Logger log = LoggerFactory.getLogger(GeminiService.class);

    /** Hard ceiling on tool-call round trips per turn, so a confused model can't loop forever or run up API cost. */
    private static final int MAX_TOOL_ITERATIONS = 5;

    private static final String FALLBACK_REPLY =
            "Sorry, I'm having trouble responding right now. Let me get a team member to help you.";

    private final GeminiClient geminiClient;
    private final GeminiPromptBuilder promptBuilder;
    private final GeminiToolRegistry toolRegistry;
    private final GeminiToolExecutor toolExecutor;
    private final RestaurantService restaurantService;
    private final GeminiProperties geminiProperties;

    public GeminiService(GeminiClient geminiClient, GeminiPromptBuilder promptBuilder,
                          GeminiToolRegistry toolRegistry, GeminiToolExecutor toolExecutor,
                          RestaurantService restaurantService, GeminiProperties geminiProperties) {
        this.geminiClient = geminiClient;
        this.promptBuilder = promptBuilder;
        this.toolRegistry = toolRegistry;
        this.toolExecutor = toolExecutor;
        this.restaurantService = restaurantService;
        this.geminiProperties = geminiProperties;
    }

    public ChatResult chat(ToolExecutionContext context, List<ConversationTurn> history, String newMessage) {
        RestaurantResponse restaurant = restaurantService.get(context.restaurantId());
        String systemPrompt = promptBuilder.build(restaurant);

        List<GeminiContent> contents = new ArrayList<>();
        for (ConversationTurn turn : history) {
            String role = "model".equalsIgnoreCase(turn.role()) ? "model" : "user";
            contents.add(new GeminiContent(role, List.of(GeminiPart.ofText(turn.text()))));
        }
        contents.add(GeminiContent.userText(newMessage));

        List<ChatResult.ToolCallSummary> toolCallLog = new ArrayList<>();

        for (int iteration = 0; iteration < MAX_TOOL_ITERATIONS; iteration++) {
            GeminiGenerateRequest request = GeminiGenerateRequest.of(
                    systemPrompt, contents, toolRegistry.getDeclarations(), geminiProperties.getMaxOutputTokens());

            GeminiGenerateResponse response;
            try {
                response = geminiClient.generateContent(request).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("Gemini call interrupted", e);
                return new ChatResult(FALLBACK_REPLY, toolCallLog);
            } catch (ExecutionException e) {
                log.error("Gemini call failed", e.getCause());
                return new ChatResult(FALLBACK_REPLY, toolCallLog);
            }

            if (response.getCandidates() == null || response.getCandidates().isEmpty()) {
                log.warn("Gemini returned no candidates");
                return new ChatResult(FALLBACK_REPLY, toolCallLog);
            }

            GeminiContent modelContent = response.getCandidates().get(0).getContent();
            List<GeminiPart> parts = modelContent.getParts();

            List<GeminiPart.GeminiFunctionCall> functionCalls = parts.stream()
                    .map(GeminiPart::getFunctionCall)
                    .filter(java.util.Objects::nonNull)
                    .toList();

            if (functionCalls.isEmpty()) {
                // No tool calls -> this is the final answer.
                String text = parts.stream()
                        .map(GeminiPart::getText)
                        .filter(java.util.Objects::nonNull)
                        .reduce("", String::concat);
                return new ChatResult(text.isBlank() ? FALLBACK_REPLY : text, toolCallLog);
            }

            // Record the model's turn (including its function call requests) before responding to it.
            contents.add(GeminiContent.modelParts(parts));

            for (GeminiPart.GeminiFunctionCall call : functionCalls) {
                ToolExecutionResult result = toolExecutor.execute(context, call);
                toolCallLog.add(new ChatResult.ToolCallSummary(
                        call.name(), call.args(), result.success(), result.toGeminiResponsePayload()));
                contents.add(GeminiContent.userFunctionResponse(call.name(), result.toGeminiResponsePayload()));
            }
        }

        log.warn("Exceeded max tool iterations ({}) for a single chat turn", MAX_TOOL_ITERATIONS);
        return new ChatResult(FALLBACK_REPLY, toolCallLog);
    }
}
