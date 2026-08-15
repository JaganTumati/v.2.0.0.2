package com.restaurant.ai.ai.gemini;

import java.util.List;

/**
 * A single turn in the conversation as Gemini expects it.
 * role is "user" or "model" - tool results are sent back as role "user"
 * with a functionResponse part, per Gemini's function-calling convention.
 */
public class GeminiContent {

    private String role;
    private List<GeminiPart> parts;

    public GeminiContent() {
    }

    public GeminiContent(String role, List<GeminiPart> parts) {
        this.role = role;
        this.parts = parts;
    }

    public static GeminiContent userText(String text) {
        return new GeminiContent("user", List.of(GeminiPart.ofText(text)));
    }

    public static GeminiContent modelParts(List<GeminiPart> parts) {
        return new GeminiContent("model", parts);
    }

    public static GeminiContent userFunctionResponse(String name, java.util.Map<String, Object> response) {
        return new GeminiContent("user", List.of(
                GeminiPart.ofFunctionResponse(new GeminiPart.GeminiFunctionResponse(name, response))));
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public List<GeminiPart> getParts() {
        return parts;
    }

    public void setParts(List<GeminiPart> parts) {
        this.parts = parts;
    }
}
