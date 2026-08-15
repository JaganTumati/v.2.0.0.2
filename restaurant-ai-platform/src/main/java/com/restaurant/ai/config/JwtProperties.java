package com.restaurant.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    private String secret;
    private int accessTokenTtlMinutes;
    private int refreshTokenTtlDays;

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public int getAccessTokenTtlMinutes() {
        return accessTokenTtlMinutes;
    }

    public void setAccessTokenTtlMinutes(int accessTokenTtlMinutes) {
        this.accessTokenTtlMinutes = accessTokenTtlMinutes;
    }

    public int getRefreshTokenTtlDays() {
        return refreshTokenTtlDays;
    }

    public void setRefreshTokenTtlDays(int refreshTokenTtlDays) {
        this.refreshTokenTtlDays = refreshTokenTtlDays;
    }
}
