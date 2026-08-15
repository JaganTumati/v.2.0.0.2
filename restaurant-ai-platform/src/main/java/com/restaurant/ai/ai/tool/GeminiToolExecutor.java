package com.restaurant.ai.ai.tool;

import com.restaurant.ai.ai.gemini.GeminiPart;
import com.restaurant.ai.audit.AuditLogService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * The only bridge between "Gemini asked for a tool" and "a domain service ran".
 * Every call here is:
 *  1. checked against the allowlist (unknown tool name => rejected, never executed)
 *  2. executed with context resolved by the backend, not by Gemini
 *  3. audited, success or failure
 *  4. guaranteed to return SOME ToolExecutionResult - a handler exception
 *     becomes an honest failure result, never a silently fabricated success
 */
@Component
public class GeminiToolExecutor {

    private static final Logger log = LoggerFactory.getLogger(GeminiToolExecutor.class);

    private final GeminiToolRegistry registry;
    private final AuditLogService auditLogService;

    public GeminiToolExecutor(GeminiToolRegistry registry, AuditLogService auditLogService) {
        this.registry = registry;
        this.auditLogService = auditLogService;
    }

    public ToolExecutionResult execute(ToolExecutionContext context, GeminiPart.GeminiFunctionCall call) {
        String toolName = call.name();
        Map<String, Object> args = call.args() == null ? Map.of() : call.args();

        if (!registry.isAllowlisted(toolName)) {
            log.warn("Rejected non-allowlisted tool call: {} (correlationId={})", toolName, context.correlationId());
            audit(context, toolName, "DENIED");
            return ToolExecutionResult.failure(toolName, "This tool is not available.");
        }

        ToolHandler handler = registry.getHandler(toolName);
        try {
            ToolExecutionResult result = handler.execute(context, args);
            audit(context, toolName, result.success() ? "SUCCESS" : "FAILURE");
            return result;
        } catch (Exception e) {
            log.error("Tool handler '{}' threw an exception (correlationId={})", toolName, context.correlationId(), e);
            audit(context, toolName, "FAILURE");
            return ToolExecutionResult.failure(toolName, "Something went wrong checking that. Please try again.");
        }
    }

    private void audit(ToolExecutionContext context, String toolName, String result) {
        auditLogService.log(
                context.restaurantId(),
                null,
                "AI",
                "TOOL_CALL:" + toolName,
                "TOOL",
                null,
                result,
                context.correlationId()
        );
    }
}
