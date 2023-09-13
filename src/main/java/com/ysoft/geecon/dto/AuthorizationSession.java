package com.ysoft.geecon.dto;

import java.util.List;

public record AuthorizationSession(AuthParams params, OAuthClient client, User user, List<String> acceptedScopes) {
    public AuthorizationSession(AuthParams params, OAuthClient client) {
        this(params, client, null, null);
    }

    public AuthorizationSession withUser(User user) {
        return new AuthorizationSession(params, client, user, acceptedScopes);
    }

    public AuthorizationSession withScopes(List<String> acceptedScopes) {
        return new AuthorizationSession(params, client,  user, acceptedScopes);
    }
}
