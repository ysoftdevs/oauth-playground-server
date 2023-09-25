package com.ysoft.geecon.helpers;

import com.ysoft.geecon.dto.AccessTokenResponse;
import com.ysoft.geecon.dto.OAuthClient;
import com.ysoft.geecon.error.ErrorResponse;
import io.restassured.response.ValidatableResponse;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

public class TokenEndpointCall {
    private final OAuthClient client;
    private final Map<String, String> tokenForm;

    public TokenEndpointCall(OAuthClient client) {
        this.client = client;

        tokenForm = new HashMap<>();
        tokenForm.put("client_id", client.clientId());
    }

    public TokenEndpointCall authorizationCode(String code, String codeVerifier) {
        tokenForm.put("grant_type", "authorization_code");
        tokenForm.put("redirect_uri", client.redirectUris().get(0));
        tokenForm.put("code", code);
        if (codeVerifier != null) {
            tokenForm.put("code_verifier", codeVerifier);
        }
        return this;
    }

    public TokenEndpointCall deviceCode(String deviceCode) {
        tokenForm.put("grant_type", "urn:ietf:params:oauth:grant-type:device_code");
        tokenForm.put("device_code", deviceCode);
        return this;
    }

    public TokenEndpointCall grantType(String grantType) {
        tokenForm.put("grant_type", grantType);
        return this;
    }

    public AccessTokenResponse expectTokens() {
        return expect()
                .statusCode(200)
                .body("token_type", is("Bearer"))
                .body("expires_in", is(notNullValue()))
                .body("access_token", is(notNullValue()))
                .body("refresh_token", is(notNullValue()))
                .extract().body().as(AccessTokenResponse.class);
    }

    public ErrorResponse expectError(int status) {
        return expect()
                .statusCode(status)
                .body("error", is(notNullValue()))
                .body("error_description", is(notNullValue()))
                .extract().body().as(ErrorResponse.class);
    }

    private ValidatableResponse expect() {
        return given()
                .formParams(tokenForm)
                .when()
                .post("/auth/token")
                .then()
                .contentType(JSON);
    }
}
