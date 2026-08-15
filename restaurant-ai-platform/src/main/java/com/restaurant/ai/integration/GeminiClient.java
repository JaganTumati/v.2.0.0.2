package com.restaurant.ai.integration;

import com.restaurant.ai.ai.gemini.GeminiGenerateRequest;
import com.restaurant.ai.ai.gemini.GeminiGenerateResponse;
import com.restaurant.ai.config.GeminiProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Adapter around the Gemini REST API (generateContent). Kept intentionally
 * thin and swappable: nothing outside this class and the ai.gemini wire-format
 * package knows Gemini's specific request/response shape, so switching
 * providers again later only touches this seam.
 */
@Component
public class GeminiClient {

    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final WebClient webClient;
    private final GeminiProperties geminiProperties;

    public GeminiClient(WebClient.Builder webClientBuilder, GeminiProperties geminiProperties) {
        this.geminiProperties = geminiProperties;
        this.webClient = webClientBuilder
                .baseUrl(geminiProperties.getBaseUrl())
                .build();
    }

    @CircuitBreaker(name = "geminiApi")
    @Retry(name = "geminiApi")
    @TimeLimiter(name = "geminiApi")
    public CompletableFuture<GeminiGenerateResponse> generateContent(GeminiGenerateRequest request) {
        String path = "/" + geminiProperties.getModel() + ":generateContent";

        return webClient.post()
                .uri(path)
                .header("x-goog-api-key", geminiProperties.getApiKey())
                .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .onStatus(status -> status.value() == 429, resp -> {
                    log.warn("Gemini API rate limit hit");
                    return resp.createException();
                })
                .bodyToMono(GeminiGenerateResponse.class)
                .timeout(Duration.ofSeconds(20))
                .toFuture();
    }
}
