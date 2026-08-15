package com.restaurant.ai.ai.tool;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;

import java.util.Map;

/**
 * Contract for a single allowlisted tool. Implementations must never trust
 * tenant/customer identifiers out of `args` - those come from
 * ToolExecutionContext, which the backend resolves independently of
 * anything Gemini said.
 */
public interface ToolHandler {

    /** Must match the name used in the GeminiFunctionDeclaration and in Gemini's functionCall. */
    String getName();

    GeminiFunctionDeclaration getDeclaration();

    ToolExecutionResult execute(ToolExecutionContext context, Map<String, Object> args);
}
