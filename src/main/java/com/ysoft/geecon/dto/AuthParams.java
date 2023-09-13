package com.ysoft.geecon.dto;

import org.jboss.resteasy.reactive.RestQuery;

import java.util.Arrays;
import java.util.List;

public class AuthParams {
    public enum ResponseType {
        code
    }

    @RestQuery("login_hint")
    String loginHint;
    @RestQuery("response_type")
    ResponseType responseType;
    @RestQuery("client_id")
    String clientId;
    @RestQuery("redirect_uri")
    String redirectUri;
    @RestQuery("scope")
    String scope;
    @RestQuery("state")
    String state;

    public String getLoginHint() {
        return loginHint;
    }

    public void setLoginHint(String loginHint) {
        this.loginHint = loginHint;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
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
}
