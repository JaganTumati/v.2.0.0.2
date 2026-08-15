package com.restaurant.ai.integration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Active whenever app.whatsapp.mock=true (the default, per the current MVP
 * plan). Lets the full AI -> tool -> database -> Gemini loop be tested
 * end-to-end without Meta WhatsApp Business API approval/setup.
 */
@Component
@ConditionalOnProperty(prefix = "app.whatsapp", name = "mock", havingValue = "true", matchIfMissing = true)
public class MockWhatsAppClient implements WhatsAppClient {

    private static final Logger log = LoggerFactory.getLogger(MockWhatsAppClient.class);

    @Override
    public void sendTextMessage(String toWhatsappNumber, String text) {
        log.info("[MOCK WHATSAPP OUTBOUND text] to={} text={}", toWhatsappNumber, text);
    }

    @Override
    public void sendTemplateMessage(String toWhatsappNumber, String templateName, String languageCode, List<String> bodyParams) {
        log.info("[MOCK WHATSAPP OUTBOUND template] to={} template={} lang={} params={}",
                toWhatsappNumber, templateName, languageCode, bodyParams);
    }
}
