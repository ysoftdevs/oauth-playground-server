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
    private String code;
    private String token;
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

    private Map<String, String> defaultQuery() {
        var map = new HashMap<String, String>();
        map.put("client_id", client.clientId());
        map.put("redirect_uri", client.redirectUri());
        map.put("state", state);
        return map;
    }

    public void parseAndValidateRedirect(Connection.Response response) {
        assertThat(response.statusCode(), is(303));
        assertThat(response.header("location"), startsWith(client.redirectUri()));

        URI location = URI.create(Objects.requireNonNull(response.header("location")));
        Map<String, String> query = URLEncodedUtils.parse(location.getQuery(), Charset.defaultCharset())
                .stream().collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

        assertThat(query.get("state"), is(state));
        assertThat(query.get("code"), is(notNullValue()));

        code = query.get("code");
        token = query.get("token");
        idToken = query.get("id_token");
    }

    public AccessTokenResponse exchangeCode() {
        AccessTokenResponse accessTokenResponse = given()
                .formParam("grant_type", "authorization_code")
                .formParam("client_id", client.clientId())
                .formParam("redirect_uri", client.redirectUri())
                .formParam("code", code)
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
        token = accessTokenResponse.accessToken();
        idToken = accessTokenResponse.idToken();
        return accessTokenResponse;
    }

    public String getState() {
        return state;
    }

    public String getCode() {
        return code;
    }

    public String getToken() {
        return token;
    }

    public String getIdToken() {
        return idToken;
    }
}
