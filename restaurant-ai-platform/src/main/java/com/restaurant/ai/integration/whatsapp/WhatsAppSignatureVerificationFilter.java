package com.restaurant.ai.integration.whatsapp;

import com.restaurant.ai.config.WhatsAppProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Verifies Meta's HMAC-SHA256 payload signature (X-Hub-Signature-256 header)
 * on inbound WhatsApp webhook POSTs, using the app secret from the Meta App
 * Dashboard (Settings > Basic > App Secret) - NOT the same as the access
 * token. Without this, anyone who finds the webhook URL could POST fake
 * "customer messages" and have them processed as real.
 *
 * Skipped entirely when app.whatsapp.mock=true, since mock/local testing
 * traffic (curl, the AI test endpoint, manual webhook simulation) never
 * carries a real Meta-computed signature and app-secret may not even be set.
 */
@Component
public class WhatsAppSignatureVerificationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(WhatsAppSignatureVerificationFilter.class);
    private static final String SIGNATURE_HEADER = "X-Hub-Signature-256";
    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private final WhatsAppProperties whatsAppProperties;

    public WhatsAppSignatureVerificationFilter(WhatsAppProperties whatsAppProperties) {
        this.whatsAppProperties = whatsAppProperties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Only the WhatsApp webhook POST needs signature verification; the GET
        // verification handshake and every other endpoint are untouched by this filter.
        return !("POST".equalsIgnoreCase(request.getMethod())
                && request.getRequestURI().startsWith("/api/webhooks/whatsapp"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        byte[] body = request.getInputStream().readAllBytes();
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request, body);

        if (whatsAppProperties.isMock()) {
            filterChain.doFilter(cachedRequest, response);
            return;
        }

        String appSecret = whatsAppProperties.getAppSecret();
        if (appSecret == null || appSecret.isBlank()) {
            log.error("WHATSAPP_APP_SECRET is not configured but MOCK_WHATSAPP=false - rejecting webhook traffic. " +
                    "Set WHATSAPP_APP_SECRET from the Meta App Dashboard (Settings > Basic > App Secret).");
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String receivedSignature = request.getHeader(SIGNATURE_HEADER);
        if (receivedSignature == null || !receivedSignature.startsWith("sha256=")) {
            log.warn("Rejected WhatsApp webhook POST with missing/malformed {} header", SIGNATURE_HEADER);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        String expectedSignature = "sha256=" + computeHmacSha256Hex(body, appSecret);

        if (!constantTimeEquals(receivedSignature, expectedSignature)) {
            log.warn("Rejected WhatsApp webhook POST with invalid signature");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        filterChain.doFilter(cachedRequest, response);
    }

    private String computeHmacSha256Hex(byte[] body, String secret) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] hmacBytes = mac.doFinal(body);
            StringBuilder hex = new StringBuilder(hmacBytes.length * 2);
            for (byte b : hmacBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to compute webhook signature", e);
        }
    }

    /** Avoids timing attacks that could otherwise leak the correct signature one byte at a time. */
    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }
}
