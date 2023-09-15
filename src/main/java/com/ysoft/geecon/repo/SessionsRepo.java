package com.ysoft.geecon.repo;

import com.ysoft.geecon.dto.AuthParams;
import com.ysoft.geecon.dto.AuthorizationSession;
import com.ysoft.geecon.dto.OAuthClient;
import com.ysoft.geecon.dto.User;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.*;

@ApplicationScoped
public class SessionsRepo {
    private final Map<String, AuthorizationSession> authorizationSessions = new HashMap<>();
    private final Map<String, String> sessionsByAuthorizationCode = new HashMap<>();

    public Optional<AuthorizationSession> getSession(String sessionId) {
        return Optional.ofNullable(authorizationSessions.get(sessionId));
    }

    public String newAuthorizationSession(AuthParams params, OAuthClient client) {
        var id = SecureRandomStrings.alphanumeric(10);
        authorizationSessions.put(id, new AuthorizationSession(params, client));
        return id;
    }

    public AuthorizationSession authorizeSession(String sessionId, List<String> acceptedScopes) {
        return Objects.requireNonNull(authorizationSessions.computeIfPresent(sessionId,
                (id, s) -> s.withScopes(acceptedScopes).withGeneratedTokens()));
    }


    public AuthorizationSession assignUser(String sessionId, User user) {
        return Objects.requireNonNull(authorizationSessions.computeIfPresent(sessionId, (id, session) -> session.withUser(user)));
    }

    public String generateAuthorizationCode(String sessionId) {
        var authCode = SecureRandomStrings.alphanumeric(10);
        sessionsByAuthorizationCode.put(authCode, sessionId);
        return authCode;
    }

    public Optional<AuthorizationSession> redeemAuthorizationCode(String authorizationCode) {
        var sessionId = Optional.ofNullable(sessionsByAuthorizationCode.get(authorizationCode));
        sessionId.ifPresent(_id -> sessionsByAuthorizationCode.remove(authorizationCode));
        return sessionId.map(authorizationSessions::get);
    }
}
