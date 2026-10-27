package com.restaurant.ai.integration;

import com.restaurant.ai.config.WhatsAppProperties;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

/**
 * Active whenever app.whatsapp.mock=false. Not wired into any test path yet -
 * this becomes live once real WHATSAPP_ACCESS_TOKEN / WHATSAPP_PHONE_NUMBER_ID
 * credentials are supplied (Phase 25, production integration).
 */
@Component
@ConditionalOnProperty(prefix = "app.whatsapp", name = "mock", havingValue = "false")
public class RealWhatsAppClient implements WhatsAppClient {

    private static final Logger log = LoggerFactory.getLogger(RealWhatsAppClient.class);

    private final WebClient webClient;
    private final WhatsAppProperties whatsAppProperties;

    public RealWhatsAppClient(WebClient.Builder webClientBuilder, WhatsAppProperties whatsAppProperties) {
        this.whatsAppProperties = whatsAppProperties;
        this.webClient = webClientBuilder.baseUrl(whatsAppProperties.getGraphApiBaseUrl()).build();
    }

    @Override
    @Retry(name = "whatsappApi")
    public void sendTextMessage(String toWhatsappNumber, String text) {
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", toWhatsappNumber,
                "type", "text",
                "text", Map.of("body", text)
        );
        post(body, toWhatsappNumber);
    }

    @Override
    @Retry(name = "whatsappApi")
    public void sendTemplateMessage(String toWhatsappNumber, String templateName, String languageCode, List<String> bodyParams) {
        List<Map<String, Object>> parameters = bodyParams == null ? List.of() : bodyParams.stream()
                .map(p -> (Map<String, Object>) Map.<String, Object>of("type", "text", "text", p))
                .toList();

        Map<String, Object> template = new java.util.LinkedHashMap<>();
        template.put("name", templateName);
        template.put("language", Map.of("code", languageCode == null ? "en_US" : languageCode));
        if (!parameters.isEmpty()) {
            template.put("components", List.of(Map.of("type", "body", "parameters", parameters)));
        }

        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", toWhatsappNumber,
                "type", "template",
                "template", template
        );
        post(body, toWhatsappNumber);
    }

    private void post(Map<String, Object> body, String toWhatsappNumber) {
        String path = "/" + whatsAppProperties.getPhoneNumberId() + "/messages";
        try {
            webClient.post()
                    .uri(path)
                    .header("Authorization", "Bearer " + whatsAppProperties.getAccessToken())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(body)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
                } catch (WebClientResponseException e) {
                    log.error("Meta WhatsApp API rejected message to {}: status={}, response={}",
                        toWhatsappNumber, e.getStatusCode(), e.getResponseBodyAsString(), e);
                    throw e;
        } catch (Exception e) {
            // Never let an outbound WhatsApp failure surface a fake success up the stack.
            log.error("Failed to send WhatsApp message to {}", toWhatsappNumber, e);
            throw e;
        }
    }
}
