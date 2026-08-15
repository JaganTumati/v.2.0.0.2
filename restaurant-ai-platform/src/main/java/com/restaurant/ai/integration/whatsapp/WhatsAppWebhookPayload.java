package com.restaurant.ai.integration.whatsapp;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WhatsAppWebhookPayload(String object, List<Entry> entry) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Entry(String id, List<Change> changes) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Change(Value value, String field) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Value(
            List<Contact> contacts,
            List<InboundMessage> messages
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Contact(Profile profile, @JsonProperty("wa_id") String waId) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Profile(String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record InboundMessage(
            String from,
            String id,
            String timestamp,
            String type,
            TextBody text
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TextBody(String body) {
    }
}
