package com.ysoft.geecon.dto;

public record OAuthClient(String clientId, String clientSecret, String redirectUri) {
    public boolean validateRedirectUri(String redirectUri) {
        return this.redirectUri != null && this.redirectUri.equals(redirectUri);
    }
}
