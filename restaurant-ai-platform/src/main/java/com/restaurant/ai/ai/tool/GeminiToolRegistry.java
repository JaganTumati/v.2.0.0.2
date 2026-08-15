package com.restaurant.ai.ai.tool;

import com.restaurant.ai.ai.gemini.GeminiFunctionDeclaration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * Single source of truth for which tools exist. Spring collects every
 * ToolHandler bean automatically; nothing outside this list is ever
 * offered to Gemini or executable by GeminiToolExecutor.
 */
@Component
public class GeminiToolRegistry {

    private final Map<String, ToolHandler> handlersByName;

    public GeminiToolRegistry(List<ToolHandler> handlers) {
        this.handlersByName = handlers.stream()
                .collect(java.util.stream.Collectors.toMap(ToolHandler::getName, h -> h));
    }

    public List<GeminiFunctionDeclaration> getDeclarations() {
        return handlersByName.values().stream()
                .map(ToolHandler::getDeclaration)
                .toList();
    }

    public ToolHandler getHandler(String name) {
        return handlersByName.get(name);
    }

    public boolean isAllowlisted(String name) {
        return handlersByName.containsKey(name);
    }
}
