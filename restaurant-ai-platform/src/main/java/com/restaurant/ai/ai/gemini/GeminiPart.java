package com.restaurant.ai.ai.gemini;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * Mirrors Gemini's "Part" union type. Exactly one of text / functionCall /
 * functionResponse should be set per instance - Gemini's API itself enforces
 * this as a oneof. thoughtSignature is preserved alongside the part when the
 * API requires it for tool-call round trips.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GeminiPart {

    private String text;
    private GeminiFunctionCall functionCall;
    private GeminiFunctionResponse functionResponse;
    @JsonProperty("thoughtSignature")
    private String thoughtSignature;

    public static GeminiPart ofText(String text) {
        GeminiPart p = new GeminiPart();
        p.text = text;
        return p;
    }

    public static GeminiPart ofFunctionCall(GeminiFunctionCall call) {
        GeminiPart p = new GeminiPart();
        p.functionCall = call;
        return p;
    }

    public static GeminiPart ofFunctionResponse(GeminiFunctionResponse response) {
        GeminiPart p = new GeminiPart();
        p.functionResponse = response;
        return p;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public GeminiFunctionCall getFunctionCall() {
        return functionCall;
    }

    public void setFunctionCall(GeminiFunctionCall functionCall) {
        this.functionCall = functionCall;
    }

    public GeminiFunctionResponse getFunctionResponse() {
        return functionResponse;
    }

    public void setFunctionResponse(GeminiFunctionResponse functionResponse) {
        this.functionResponse = functionResponse;
    }

    public String getThoughtSignature() {
        return thoughtSignature;
    }

    public void setThoughtSignature(String thoughtSignature) {
        this.thoughtSignature = thoughtSignature;
    }

    public record GeminiFunctionCall(String name, Map<String, Object> args) {
    }

    public record GeminiFunctionResponse(String name, Map<String, Object> response) {
    }
}
