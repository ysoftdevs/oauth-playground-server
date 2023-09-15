package com.ysoft.geecon.dto;

import java.util.Objects;

public record OAuthClient(String clientId, String description, String clientSecret, String redirectUri) {
    public boolean validateRedirectUri(String redirectUri) {
        return this.redirectUri != null && this.redirectUri.equals(redirectUri);
    }

    public boolean validateSecret(String clientSecret) {
        return Objects.equals(clientSecret, this.clientSecret);
    }
}
