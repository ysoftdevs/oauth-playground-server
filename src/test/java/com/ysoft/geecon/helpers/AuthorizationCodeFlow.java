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

    private Map<String, String> query;

    public AuthorizationCodeFlow(String authUrl, OAuthClient client) {
        this.authUrl = authUrl;
        this.client = client;

        query = new HashMap<>();
        query.put("client_id", client.clientId());
        query.put("redirect_uri", client.redirectUris().get(0));
        query.put("state", state);
    }

    public AuthorizationCodeFlow param(String key, String value) {
        query.put(key, value);
        return this;
    }

    public Result start() throws IOException {
        Document document = Jsoup.connect(authUrl)
                .followRedirects(false)
                .data(query)
                .get();

        return new Result() {
            @Override
            public LoginScreen expectLogin() {
                return new LoginScreen(document);
            }

            @Override
            public Map<String, String> expectErrorRedirect() {
                var response = document.connection().response();

                Map<String, String> query = expectRedirect(response);
                assertThat(query.get("error"), is(notNullValue()));
                assertThat(query.get("error_description"), is(notNullValue()));
                return query;
            }
        };
    }

    public void expectSuccessfulRedirect(Connection.Response response) {
        Map<String, String> query = expectRedirect(response);

        code = query.get("code");
        accessToken = query.get("access_token");
        idToken = query.get("id_token");
    }

    private Map<String, String> expectRedirect(Connection.Response response) {
        assertThat(response.statusCode(), is(303));
        assertThat(response.header("location"), startsWith(client.redirectUris().get(0)));

        URI location = URI.create(Objects.requireNonNull(response.header("location")));
        Map<String, String> query = URLEncodedUtils.parse(location.getQuery(), Charset.defaultCharset())
                .stream().collect(Collectors.toMap(NameValuePair::getName, NameValuePair::getValue));

        assertThat(query.get("state"), is(state));
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

    public AuthorizationCodeFlow pkce(String codeChallenge, String codeVerifier) {
        query.put("code_challenge", codeChallenge);
        query.put("code_challenge_method", "S256");
        this.codeVerifier = codeVerifier;
        return this;
    }

    public AuthorizationCodeFlow scope(String scope) {
        return param("scope", scope);
    }

    public interface Result {
        LoginScreen expectLogin();

        Map<String, String> expectErrorRedirect();
    }
}
