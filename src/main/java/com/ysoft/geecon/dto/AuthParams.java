package com.ysoft.geecon.dto;

import org.jboss.resteasy.reactive.RestQuery;

import java.util.Arrays;
import java.util.List;

public class AuthParams {
    public List<ResponseType> getResponseTypes() {
        return responseType == null ? List.of() : Arrays.stream(responseType.split(" "))
                .map(ResponseType::valueOf)
                .toList();
    }

    @RestQuery("login_hint")
    String loginHint;
    @RestQuery("response_type")
    String responseType;
    @RestQuery("client_id")
    String clientId;
    @RestQuery("redirect_uri")
    String redirectUri;
    @RestQuery("scope")
    String scope;
    @RestQuery("state")
    String state;
    @RestQuery("code_challenge_method")
    String codeChallengeMethod;
    @RestQuery("code_challenge")
    String codeChallenge;
    @RestQuery("nonce")
    String nonce;

    public String getLoginHint() {
        return loginHint;
    }

    public void setLoginHint(String loginHint) {
        this.loginHint = loginHint;
    }

    public String getResponseType() {
        return responseType;
    }

    public enum ResponseType {
        code, token, id_token
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getScope() {
        return scope;
    }

    public List<String> getScopes() {
        return scope == null ? List.of() : Arrays.stream(scope.split(" ")).toList();
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCodeChallengeMethod() {
        return codeChallengeMethod;
    }

    public void setCodeChallengeMethod(String codeChallengeMethod) {
        this.codeChallengeMethod = codeChallengeMethod;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }

    public void setCodeChallenge(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }
}
