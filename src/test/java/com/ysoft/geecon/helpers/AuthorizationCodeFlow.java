package com.ysoft.geecon.helpers;

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

    public TokenEndpointCall exchangeCode() {
        return new TokenEndpointCall(client).authorizationCode(code, codeVerifier);
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
