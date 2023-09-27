package com.ysoft.geecon.dto;

import com.ysoft.geecon.repo.SecureRandomStrings;
import io.smallrye.jwt.build.Jwt;
import io.smallrye.jwt.build.JwtClaimsBuilder;
import org.eclipse.microprofile.jwt.Claims;

import java.util.List;
import java.util.Objects;

public record AuthorizationSession(String sessionId,
                                   AuthParams params,
                                   OAuthClient client,
                                   User user,
                                   List<String> acceptedScopes,
                                   AccessTokenResponse tokens) {
    public AuthorizationSession(AuthParams params, OAuthClient client) {
        this(SecureRandomStrings.alphanumeric(50), params, client, null, null, null);
    }

    public AuthorizationSession withUser(User user) {
        return new AuthorizationSession(sessionId, params, client, user, acceptedScopes, tokens);
    }

    public AuthorizationSession withScopes(List<String> acceptedScopes) {
        return new AuthorizationSession(sessionId, params, client, user, acceptedScopes, tokens);
    }

    public AuthorizationSession withGeneratedTokens() {
        var tokens = new AccessTokenResponse("Bearer",
                expiresIn(),
                SecureRandomStrings.alphanumeric(50),
                scope(),
                SecureRandomStrings.alphanumeric(50),
                acceptedScopes.contains("openid") ? idToken() : null
        );
        return new AuthorizationSession(sessionId, params, client, user, acceptedScopes, tokens);
    }

    private int expiresIn() {
        return 8400;
    }

    public String scope() {
        return acceptedScopes == null ? null : String.join(" ", acceptedScopes);
    }

    private String idToken() {
        JwtClaimsBuilder jwt = Jwt.claims()
                .issuer("https://sso.oauth-playground.online")
                .issuedAt(System.currentTimeMillis() / 1000)
                .expiresAt(System.currentTimeMillis() / 1000 + expiresIn())
                .subject(user().id())
                .audience(client().clientId())
                .preferredUserName(user().login());

        if (params().nonce != null)
            jwt.claim(Claims.nonce, params().nonce);

        return jwt.sign();
    }

    public boolean validateCodeChallenge(String codeVerifier) {
        if (params.codeChallengeMethod == null) {
            return true;
        }
        if (codeVerifier == null) {
            return false;
        }
        return Pkce.validate(params.codeChallengeMethod, params.codeChallenge, codeVerifier);
    }

    public boolean validateRedirectUri(String redirectUri) {
        return Objects.equals(params.redirectUri, redirectUri);
    }
}
