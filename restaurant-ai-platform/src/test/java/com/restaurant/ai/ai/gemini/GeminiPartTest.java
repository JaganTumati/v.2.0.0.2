package com.restaurant.ai.ai.gemini;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GeminiPartTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void preservesThoughtSignatureWhenRoundTrippingFunctionCall() throws Exception {
        String responseJson = """
                {
                  "functionCall": {
                    "name": "get_menu",
                    "args": {}
                  },
                  "thoughtSignature": "opaque-signature"
                }
                """;

        GeminiPart part = objectMapper.readValue(responseJson, GeminiPart.class);

        assertEquals("opaque-signature", part.getThoughtSignature());
        assertEquals("opaque-signature",
                objectMapper.readTree(objectMapper.writeValueAsString(part)).get("thoughtSignature").asText());
    }
}
