package com.restaurant.ai.integration;

import java.util.List;

/**
 * Outbound WhatsApp actions. Exactly one implementation is active at a time,
 * selected by app.whatsapp.mock (MockWhatsAppClient vs RealWhatsAppClient),
 * so business logic never knows or cares which one it's talking to.
 */
public interface WhatsAppClient {

    void sendTextMessage(String toWhatsappNumber, String text);

    /**
     * Sends a pre-approved WhatsApp message template. Required by Meta for any
     * outbound message sent outside the 24-hour customer service window (i.e.
     * more than 24h since the customer's last inbound message, or when there's
     * been no inbound message at all) - free-form text via sendTextMessage()
     * will be REJECTED by Meta outside that window.
     *
     * templateName/languageCode must match a template already approved in
     * Meta Business Manager. bodyParams fill the template's {{1}}, {{2}}, ...
     * placeholders in order.
     */
    void sendTemplateMessage(String toWhatsappNumber, String templateName, String languageCode, List<String> bodyParams);

    // sendInteractiveMessage / sendImageMessage land here when the
    // dashboard/automation features that need them are built.
}
