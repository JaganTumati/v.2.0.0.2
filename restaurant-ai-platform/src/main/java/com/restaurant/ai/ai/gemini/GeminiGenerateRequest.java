package com.restaurant.ai.ai.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiGenerateRequest {

    private SystemInstruction systemInstruction;
    private List<GeminiContent> contents;
    private List<ToolsWrapper> tools;
    private GenerationConfig generationConfig;

    public static GeminiGenerateRequest of(String systemPrompt, List<GeminiContent> contents,
                                            List<GeminiFunctionDeclaration> functionDeclarations,
                                            int maxOutputTokens) {
        GeminiGenerateRequest req = new GeminiGenerateRequest();
        req.systemInstruction = new SystemInstruction(List.of(GeminiPart.ofText(systemPrompt)));
        req.contents = contents;
        req.tools = functionDeclarations == null || functionDeclarations.isEmpty()
                ? null
                : List.of(new ToolsWrapper(functionDeclarations));
        req.generationConfig = new GenerationConfig(maxOutputTokens, 0.4);
        return req;
    }

    public SystemInstruction getSystemInstruction() {
        return systemInstruction;
    }

    public List<GeminiContent> getContents() {
        return contents;
    }

    public List<ToolsWrapper> getTools() {
        return tools;
    }

    public GenerationConfig getGenerationConfig() {
        return generationConfig;
    }

    public record SystemInstruction(List<GeminiPart> parts) {
    }

    public record ToolsWrapper(List<GeminiFunctionDeclaration> functionDeclarations) {
    }

    public record GenerationConfig(int maxOutputTokens, double temperature) {
    }
}
