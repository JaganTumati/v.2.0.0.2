package com.restaurant.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // Patterns (not exact origins) so this works both for local dev AND
        // GitHub Codespaces, where the frontend's forwarded URL is a random
        // per-session subdomain like https://<name>-5173.app.github.dev -
        // there's no fixed origin to allowlist ahead of time.
        List<String> patterns = new ArrayList<>(List.of(
                "http://localhost:*",
                "http://127.0.0.1:*",
                "https://*.app.github.dev",
                "https://*.githubpreview.dev"
        ));
        // Escape hatch for a real production dashboard domain.
        String explicitOrigin = System.getenv("DASHBOARD_ORIGIN");
        if (explicitOrigin != null && !explicitOrigin.isBlank()) {
            patterns.add(explicitOrigin);
        }
        config.setAllowedOriginPatterns(patterns);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
