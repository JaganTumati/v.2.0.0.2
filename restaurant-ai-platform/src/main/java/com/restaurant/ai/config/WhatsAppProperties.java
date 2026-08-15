package com.restaurant.ai.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.whatsapp")
public class WhatsAppProperties {

    private boolean mock;
    private String accessToken;
    private String phoneNumberId;
    private String verifyToken;
    private String appSecret;
    private String graphApiBaseUrl;

    public boolean isMock() {
        return mock;
    }

    public void setMock(boolean mock) {
        this.mock = mock;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getPhoneNumberId() {
        return phoneNumberId;
    }

    public void setPhoneNumberId(String phoneNumberId) {
        this.phoneNumberId = phoneNumberId;
    }

    public String getVerifyToken() {
        return verifyToken;
    }

    public void setVerifyToken(String verifyToken) {
        this.verifyToken = verifyToken;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public String getGraphApiBaseUrl() {
        return graphApiBaseUrl;
    }

    public void setGraphApiBaseUrl(String graphApiBaseUrl) {
        this.graphApiBaseUrl = graphApiBaseUrl;
    }
}
