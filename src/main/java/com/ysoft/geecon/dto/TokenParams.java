package com.ysoft.geecon.dto;

import jakarta.ws.rs.FormParam;

public class TokenParams {

    @FormParam("grant_type")
    private String grantType;

    @FormParam("client_id")
    private String clientId;

    @FormParam("client_secret")
    private String clientSecret;

    @FormParam("redirect_uri")
    private String redirectUri;

    @FormParam("code")
    private String code;

    @FormParam("code_verifier")
    private String codeVerifier;

    public String getGrantType() {
        return grantType;
    }

    public void setGrantType(String grantType) {
        this.grantType = grantType;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }

    public void setRedirectUri(String redirectUri) {
        this.redirectUri = redirectUri;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeVerifier() {
        return codeVerifier;
    }

    public void setCodeVerifier(String codeVerifier) {
        this.codeVerifier = codeVerifier;
    }
}
