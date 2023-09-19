package com.ysoft.geecon.helpers;

import com.ysoft.geecon.dto.AccessTokenResponse;
import com.ysoft.geecon.dto.OAuthClient;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static io.restassured.RestAssured.given;
import static io.restassured.http.ContentType.JSON;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class AuthorizationCodeFlow {
    private final String authUrl;
    private final OAuthClient client;
    private String state = "testStateIsNotRandom";
    private String codeChallenge;
    private String codeVerifier;
    private String code;
    private String accessToken;
    private String idToken;

    public AuthorizationCodeFlow(String authUrl, OAuthClient client) {
        this.authUrl = authUrl;
        this.client = client;
    }

    public LoginScreen start(Map<String, String> additionalData) throws IOException {
        var data = defaultQuery();
        if (additionalData != null) {
            data.putAll(additionalData);
        }

        Document login = Jsoup.connect(authUrl)
                .data(data)
                .get();

        return new LoginScreen(login);
    }

    public Connection.Response startExpectError(Map<String, String> additionalData) throws IOException {
        var data = defaultQuery();
        if (additionalData != null) {
            data.putAll(additionalData);
        }

        return Jsoup.connect(authUrl)
                .followRedirects(false)
                .data(data)
                .get()
                .connection()
                .response();
    }

    private Map<String, String> defaultQuery() {
        var map = new HashMap<String, String>();
        map.put("client_id", client.clientId());
        map.put("redirect_uri", client.redirectUri());
        map.put("state", state);
        if (codeChallenge != null) {
            map.put("code_challenge", codeChallenge);
            map.put("code_challenge_method", "S256");
        }
        return map;
    }

    public void parseAndValidateRedirect(Connection.Response response) {
        assertThat(response.statusCode(), is(303));
        assertThat(response.header("location"), startsWith(client.redirectUri()));

        URI location = URI.create(Objects.requireNonNull(response.header("location")));
        Map<String, String> query = URLEncodedUtils.parse(location.getQuery(), Charset.defaultCharset())
                .stream().collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

        assertThat(query.get("state"), is(state));

        code = query.get("code");
        accessToken = query.get("access_token");
        idToken = query.get("id_token");
    }

    public Map<String, String> parseAndValidateRedirectError(Connection.Response response) {
        assertThat(response.statusCode(), is(303));
        assertThat(response.header("location"), startsWith(client.redirectUri()));

        URI location = URI.create(Objects.requireNonNull(response.header("location")));
        Map<String, String> query = URLEncodedUtils.parse(location.getQuery(), Charset.defaultCharset())
                .stream().collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

        assertThat(query.get("state"), is(state));
        assertThat(query.get("error"), is(notNullValue()));
        assertThat(query.get("error_description"), is(notNullValue()));
        return query;
    }

    public AccessTokenResponse exchangeCode() {
        Map<String, String> tokenForm = new HashMap<>();
        tokenForm.put("grant_type", "authorization_code");
        tokenForm.put("client_id", client.clientId());
        tokenForm.put("redirect_uri", client.redirectUri());
        tokenForm.put("code", code);
        if (codeVerifier != null) {
            tokenForm.put("code_verifier", codeVerifier);
        }

        AccessTokenResponse accessTokenResponse = given()
                .formParams(tokenForm)
                .when()
                .post("/auth/token")
                .then()
                .statusCode(200)
                .contentType(JSON)
                .body("token_type", is("Bearer"))
                .body("expires_in", is(notNullValue()))
                .body("access_token", is(notNullValue()))
                .body("refresh_token", is(notNullValue()))
                .extract().body().as(AccessTokenResponse.class);
        accessToken = accessTokenResponse.accessToken();
        idToken = accessTokenResponse.idToken();
        return accessTokenResponse;
    }

    public String getState() {
        return state;
    }

    public String getCode() {
        return code;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getIdToken() {
        return idToken;
    }

    public void setPkce(String codeChallenge, String codeVerifier) {
        this.codeChallenge = codeChallenge;
        this.codeVerifier = codeVerifier;
    }
}
