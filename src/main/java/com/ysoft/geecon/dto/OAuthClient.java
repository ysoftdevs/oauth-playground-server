package com.ysoft.geecon.dto;

import java.util.List;
import java.util.Objects;

public record OAuthClient(String clientId, String description, String clientSecret, List<String> redirectUris) {
    public boolean validateRedirectUri(String redirectUri) {
        return this.redirectUris != null && this.redirectUris.contains(redirectUri);
    }

    public boolean validateSecret(String clientSecret) {
        return Objects.equals(clientSecret, this.clientSecret);
    }
}
