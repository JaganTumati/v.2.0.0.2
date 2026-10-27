// package com.restaurant.ai.integration.whatsapp;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.restaurant.ai.config.RestaurantProperties;
// import com.restaurant.ai.config.WhatsAppProperties;
// import com.restaurant.ai.entity.WebhookEvent;
// import com.restaurant.ai.repository.WebhookEventRepository;
// import com.restaurant.ai.service.MessageProcessingService;
// import jakarta.servlet.http.HttpServletRequest;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;
// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.*;

// /**
//  * Meta requires: (1) a GET verification handshake at setup time, and (2) a
//  * fast ack (2xx, quickly) on every POST or it will retry/back off. Real work
//  * happens in MessageProcessingService off-thread (Phase 9/10) - nothing
//  * expensive runs on this request thread.
//  *
//  * NOTE: this does NOT yet verify Meta's X-Hub-Signature-256 payload signature
//  * (requires a separate WHATSAPP_APP_SECRET not yet in scope). In mock mode
//  * this endpoint is exercised via manual/test POSTs rather than real Meta
//  * traffic, so it's flagged here rather than silently skipped.
//  */
// @RestController
// @RequestMapping("/api/webhooks/whatsapp")
// public class WhatsAppWebhookController {

//     private static final Logger log = LoggerFactory.getLogger(WhatsAppWebhookController.class);

//     private final WhatsAppProperties whatsAppProperties;
//     private final RestaurantProperties restaurantProperties;
//     private final WebhookEventRepository webhookEventRepository;
//     private final MessageProcessingService messageProcessingService;
//     private final ObjectMapper objectMapper;

//     public WhatsAppWebhookController(WhatsAppProperties whatsAppProperties,
//                                       RestaurantProperties restaurantProperties,
//                                       WebhookEventRepository webhookEventRepository,
//                                       MessageProcessingService messageProcessingService,
//                                       ObjectMapper objectMapper) {
//         this.whatsAppProperties = whatsAppProperties;
//         this.restaurantProperties = restaurantProperties;
//         this.webhookEventRepository = webhookEventRepository;
//         this.messageProcessingService = messageProcessingService;
//         this.objectMapper = objectMapper;
//     }

//     @GetMapping
//     public ResponseEntity<String> verify(
//             @RequestParam("hub.mode") String mode,
//             @RequestParam("hub.verify_token") String verifyToken,
//             @RequestParam("hub.challenge") String challenge) {

//         if ("subscribe".equals(mode) && whatsAppProperties.getVerifyToken() != null
//                 && whatsAppProperties.getVerifyToken().equals(verifyToken)) {
//             return ResponseEntity.ok(challenge);
//         }
//         log.warn("WhatsApp webhook verification failed");
//         return ResponseEntity.status(403).build();
//     }

//     @PostMapping
//     public ResponseEntity<Void> receive(@RequestBody WhatsAppWebhookPayload payload, HttpServletRequest request) {
//         if (payload.entry() == null) {
//             return ResponseEntity.ok().build();
//         }

//         for (WhatsAppWebhookPayload.Entry entry : payload.entry()) {
//             if (entry.changes() == null) continue;
//             for (WhatsAppWebhookPayload.Change change : entry.changes()) {
//                 if (change.value() == null || change.value().messages() == null) continue;

//                 String senderName = extractSenderName(change.value());

//                 for (WhatsAppWebhookPayload.InboundMessage message : change.value().messages()) {
//                     handleInboundMessage(message, senderName);
//                 }
//             }
//         }

//         // Fast ack regardless of processing outcome - failures are handled/logged inside
//         // MessageProcessingService, and Meta must not see a non-2xx or it will retry the whole batch.
//         return ResponseEntity.ok().build();
//     }

//     private void handleInboundMessage(WhatsAppWebhookPayload.InboundMessage message, String senderName) {
//         if (message.id() == null) {
//             log.warn("Inbound WhatsApp message missing id, skipping");
//             return;
//         }

//         // Idempotency: Meta may redeliver the same event on retry.
//         if (webhookEventRepository.existsByWaMessageId(message.id())) {
//             log.info("Duplicate WhatsApp message id {} - skipping", message.id());
//             return;
//         }

//         WebhookEvent event = new WebhookEvent();
//         event.setWaMessageId(message.id());
//         event.setPayloadJson(toJson(message));
//         event.setStatus("RECEIVED");
//         webhookEventRepository.save(event);

//         String text = message.text() != null ? message.text().body() : null;
//         if (text == null || text.isBlank()) {
//             log.info("Ignoring non-text WhatsApp message type '{}' for now", message.type());
//             return;
//         }

//         messageProcessingService.processInboundMessage(
//                 restaurantProperties.getDefaultId(),
//                 message.from(),
//                 senderName,
//                 text,
//                 message.id()
//         );
//     }

//     private String extractSenderName(WhatsAppWebhookPayload.Value value) {
//         if (value.contacts() == null || value.contacts().isEmpty()) {
//             return null;
//         }
//         var profile = value.contacts().get(0).profile();
//         return profile != null ? profile.name() : null;
//     }

//     private String toJson(Object o) {
//         try {
//             return objectMapper.writeValueAsString(o);
//         } catch (Exception e) {
//             return "{}";
//         }
//     }
//} 



package com.restaurant.ai.integration.whatsapp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.ai.config.RestaurantProperties;
import com.restaurant.ai.config.WhatsAppProperties;
import com.restaurant.ai.entity.WebhookEvent;
import com.restaurant.ai.repository.WebhookEventRepository;
import com.restaurant.ai.service.MessageProcessingService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * WhatsApp webhook controller.
 *
 * Handles:
 * 1. GET verification handshake from Meta.
 * 2. POST incoming WhatsApp messages.
 *
 * The POST endpoint acknowledges Meta quickly with HTTP 200.
 * Message processing is delegated to MessageProcessingService.
 *
 * NOTE:
 * X-Hub-Signature-256 verification is not implemented yet.
 * This should be added before production deployment.
 */
@RestController
@RequestMapping("/api/webhooks/whatsapp")
public class WhatsAppWebhookController {

    private static final Logger log =
            LoggerFactory.getLogger(WhatsAppWebhookController.class);

    private final WhatsAppProperties whatsAppProperties;
    private final RestaurantProperties restaurantProperties;
    private final WebhookEventRepository webhookEventRepository;
    private final MessageProcessingService messageProcessingService;
    private final ObjectMapper objectMapper;

    public WhatsAppWebhookController(
            WhatsAppProperties whatsAppProperties,
            RestaurantProperties restaurantProperties,
            WebhookEventRepository webhookEventRepository,
            MessageProcessingService messageProcessingService,
            ObjectMapper objectMapper) {

        this.whatsAppProperties = whatsAppProperties;
        this.restaurantProperties = restaurantProperties;
        this.webhookEventRepository = webhookEventRepository;
        this.messageProcessingService = messageProcessingService;
        this.objectMapper = objectMapper;
    }

    /**
     * Meta webhook verification endpoint.
     *
     * Meta sends:
     * hub.mode
     * hub.verify_token
     * hub.challenge
     *
     * If the verify token matches our configured token,
     * we return the challenge with HTTP 200.
     */
    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam(value = "hub.mode", required = false) String mode,
            @RequestParam(value = "hub.verify_token", required = false) String verifyToken,
            @RequestParam(value = "hub.challenge", required = false) String challenge) {

        log.info(
                "WhatsApp webhook verification request received: mode={}",
                mode
        );

        if ("subscribe".equals(mode)
            && challenge != null
                && whatsAppProperties.getVerifyToken() != null
                && whatsAppProperties.getVerifyToken().equals(verifyToken)) {

            log.info("WhatsApp webhook verification successful");

            return ResponseEntity.ok(challenge);
        }

        log.warn("WhatsApp webhook verification failed");

        return ResponseEntity.status(403).build();
    }

    /**
     * Receives incoming WhatsApp webhook events from Meta.
     */
    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestBody WhatsAppWebhookPayload payload,
            HttpServletRequest request) {

        log.info("=== WhatsApp POST webhook received ===");

        if (payload == null) {
            log.warn("WhatsApp webhook payload is null");
            return ResponseEntity.ok().build();
        }

        if (payload.entry() == null || payload.entry().isEmpty()) {
            log.warn("WhatsApp webhook received with no entries");
            return ResponseEntity.ok().build();
        }

        log.info(
                "WhatsApp webhook contains {} entr{}",
                payload.entry().size(),
                payload.entry().size() == 1 ? "y" : "ies"
        );

        for (WhatsAppWebhookPayload.Entry entry : payload.entry()) {

            if (entry == null) {
                log.warn("WhatsApp webhook contains a null entry");
                continue;
            }

            if (entry.changes() == null || entry.changes().isEmpty()) {
                log.warn("WhatsApp webhook entry contains no changes");
                continue;
            }

            for (WhatsAppWebhookPayload.Change change : entry.changes()) {

                if (change == null) {
                    log.warn("WhatsApp webhook contains a null change");
                    continue;
                }

                if (change.value() == null) {
                    log.warn("WhatsApp webhook change contains no value");
                    continue;
                }

                if (change.value().messages() == null
                        || change.value().messages().isEmpty()) {

                    log.info(
                            "WhatsApp webhook change contains no messages"
                    );

                    continue;
                }

                String senderName =
                        extractSenderName(change.value());

                log.info(
                        "Found {} incoming WhatsApp message(s), senderName={}",
                        change.value().messages().size(),
                        senderName
                );

                for (WhatsAppWebhookPayload.InboundMessage message
                        : change.value().messages()) {

                    if (message == null) {
                        log.warn("WhatsApp webhook contains a null message");
                        continue;
                    }

                    log.info(
                            "WhatsApp inbound message: id={}, from={}, type={}, text={}",
                            message.id(),
                            message.from(),
                            message.type(),
                            message.text() != null
                                    ? message.text().body()
                                    : null
                    );

                    handleInboundMessage(message, senderName);
                }
            }
        }

        /*
         * Always acknowledge Meta with HTTP 200.
         *
         * Meta expects a fast response. If we return an error,
         * Meta may retry the same webhook event.
         */
        log.info("=== WhatsApp POST webhook processing accepted ===");

        return ResponseEntity.ok().build();
    }

    /**
     * Handles one incoming WhatsApp message.
     */
    private void handleInboundMessage(
            WhatsAppWebhookPayload.InboundMessage message,
            String senderName) {

        if (message.id() == null || message.id().isBlank()) {

            log.warn(
                    "Inbound WhatsApp message missing message id, skipping"
            );

            return;
        }

        /*
         * Idempotency:
         *
         * Meta can redeliver the same event.
         * We don't want to process the same WhatsApp message twice.
         */
        if (webhookEventRepository.existsByWaMessageId(message.id())) {

            log.info(
                    "Duplicate WhatsApp message id {} - skipping",
                    message.id()
            );

            return;
        }

        /*
         * Store the incoming webhook event.
         */
        WebhookEvent event = new WebhookEvent();

        event.setWaMessageId(message.id());
        event.setPayloadJson(toJson(message));
        event.setStatus("RECEIVED");

        webhookEventRepository.save(event);

        log.info(
                "Stored WhatsApp webhook event: messageId={}",
                message.id()
        );

        /*
         * Currently we process text messages only.
         */
        String text = message.text() != null
                ? message.text().body()
                : null;

        if (text == null || text.isBlank()) {

            log.info(
                    "Ignoring non-text WhatsApp message: type={}, messageId={}",
                    message.type(),
                    message.id()
            );

            return;
        }

        log.info(
                "Processing WhatsApp text message: from={}, senderName={}, text={}, messageId={}",
                message.from(),
                senderName,
                text,
                message.id()
        );

        /*
         * Delegate the actual restaurant/AI processing.
         */
        try {

            messageProcessingService.processInboundMessage(
                    restaurantProperties.getDefaultId(),
                    message.from(),
                    senderName,
                    text,
                    message.id()
            );

            log.info(
                    "MessageProcessingService accepted message: messageId={}",
                    message.id()
            );

        } catch (Exception e) {

            /*
             * Do not throw the exception back to Meta.
             * The webhook should still be acknowledged.
             */
            log.error(
                    "Error processing WhatsApp message: messageId={}",
                    message.id(),
                    e
            );
        }
    }

    /**
     * Extracts the WhatsApp user's profile name.
     */
    private String extractSenderName(
            WhatsAppWebhookPayload.Value value) {

        if (value == null
                || value.contacts() == null
                || value.contacts().isEmpty()) {

            return null;
        }

        var profile = value.contacts().get(0).profile();

        return profile != null
                ? profile.name()
                : null;
    }

    /**
     * Converts an object to JSON for database storage.
     */
    private String toJson(Object o) {

        try {
            return objectMapper.writeValueAsString(o);

        } catch (Exception e) {

            log.warn(
                    "Could not serialize WhatsApp webhook object to JSON",
                    e
            );

            return "{}";
        }
    }
}