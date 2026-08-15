package com.restaurant.ai.security;

import com.restaurant.ai.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey key;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // HS256 needs >= 256 bits; JWT_SECRET must be a long random string (see .env.example).
        this.key = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String issueAccessToken(AppUserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("userId", principal.getUserId().toString())
                .claim("restaurantId", principal.getRestaurantId() == null ? null : principal.getRestaurantId().toString())
                .claim("role", principal.getRoleName())
                .claim("type", "access")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.getAccessTokenTtlMinutes(), ChronoUnit.MINUTES)))
                .signWith(key)
                .compact();
    }

    public String issueRefreshToken(AppUserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(principal.getUsername())
                .claim("userId", principal.getUserId().toString())
                .claim("type", "refresh")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(jwtProperties.getRefreshTokenTtlDays(), ChronoUnit.DAYS)))
                .signWith(key)
                .compact();
    }

    /** Throws a JwtException subtype (unchecked) if the token is invalid/expired/tampered - callers treat any exception as "unauthenticated". */
    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractUserId(Claims claims) {
        return UUID.fromString(claims.get("userId", String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return "refresh".equals(claims.get("type", String.class));
    }
}
