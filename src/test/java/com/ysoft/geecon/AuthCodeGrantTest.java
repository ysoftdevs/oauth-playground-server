package com.ysoft.geecon;

import com.ysoft.geecon.dto.AccessTokenResponse;
import com.ysoft.geecon.dto.OAuthClient;
import com.ysoft.geecon.dto.User;
import com.ysoft.geecon.error.ErrorResponse;
import com.ysoft.geecon.helpers.AuthorizationCodeFlow;
import com.ysoft.geecon.helpers.ConsentScreen;
import com.ysoft.geecon.helpers.LoginScreen;
import com.ysoft.geecon.repo.ClientsRepo;
import com.ysoft.geecon.repo.UsersRepo;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

@QuarkusTest
public class AuthCodeGrantTest {
    public static final OAuthClient CLIENT = new OAuthClient("myclient", "", null, "https://myserver:8888/success");
    @Inject
    ClientsRepo clientsRepo;
    @Inject
    UsersRepo usersRepo;

    @TestHTTPResource("auth")
    String authUrl;

    @BeforeEach
    void beforeAll() {
        clientsRepo.register(CLIENT);
        usersRepo.register(new User("bob", "password"));
    }

    @Test
    public void authCodeGrant() throws IOException {
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow(authUrl, CLIENT)
                .scope("scope1 scope2");
        LoginScreen loginScreen = flow.start().expectLogin();

        ConsentScreen consentScreen = loginScreen.submit("bob", "password").expectSuccess();
        assertThat(consentScreen.getScopes(), is(List.of("scope1", "scope2")));

        Document submit = consentScreen.submit();
        flow.expectSuccessfulRedirect(submit.connection().response());

        assertThat(flow.getCode(), is(notNullValue()));
        assertThat(flow.getAccessToken(), is(nullValue()));
        AccessTokenResponse accessTokenResponse = flow.exchangeCode().expectTokens();

        assertThat(accessTokenResponse.accessToken(), is(notNullValue()));
    }

    @Test
    public void badCredentials() throws IOException {
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow(authUrl, CLIENT)
                .scope("scope1 scope2");
        LoginScreen loginScreen = flow.start().expectLogin();

        loginScreen.submit("x", "bbb").expectError("Invalid credentials")
                .submit("bob", "bbb").expectError("Invalid credentials")
                .submit("bob", "password").expectSuccess();
    }

    @Test
    public void authCodeGrant_invalidResponseType() throws IOException {
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow(authUrl, CLIENT)
                .param("response_type", "");
        Map<String, String> query = flow.start().expectErrorRedirect();
        assertThat(query.get("error"), is(ErrorResponse.Error.unsupported_response_type.name()));
    }

    @Test
    public void implicitGrant() throws IOException {
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow(authUrl, CLIENT)
                .param("response_type", "token")
                .scope("scope1 scope2");
        LoginScreen loginScreen = flow.start().expectLogin();

        ConsentScreen consentScreen = loginScreen.submit("bob", "password").expectSuccess();
        assertThat(consentScreen.getScopes(), is(List.of("scope1", "scope2")));

        Document submit = consentScreen.submit();
        flow.expectSuccessfulRedirect(submit.connection().response());

        assertThat(flow.getAccessToken(), is(notNullValue()));
    }

    @Test
    public void authCodeGrantWithPkce() throws IOException {
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow(authUrl, CLIENT)
                .pkce("PnRLncOTibrwxaBmBYm4QC89u0m4mz518sk1WFKjxnc", "bbb")
                .scope("scope1 scope2");

        LoginScreen loginScreen = flow.start().expectLogin();

        ConsentScreen consentScreen = loginScreen.submit("bob", "password").expectSuccess();
        assertThat(consentScreen.getScopes(), is(List.of("scope1", "scope2")));

        Document submit = consentScreen.submit();
        flow.expectSuccessfulRedirect(submit.connection().response());

        assertThat(flow.getCode(), is(notNullValue()));
        assertThat(flow.getAccessToken(), is(nullValue()));
        AccessTokenResponse accessTokenResponse = flow.exchangeCode().expectTokens();

        assertThat(accessTokenResponse.accessToken(), is(notNullValue()));
    }

    @Test
    public void authCodeGrantWithPkce_wrong() throws IOException {
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow(authUrl, CLIENT)
                .pkce("badChallenge", "bbb")
                .scope("scope1 scope2");

        LoginScreen loginScreen = flow.start().expectLogin();

        ConsentScreen consentScreen = loginScreen.submit("bob", "password").expectSuccess();
        assertThat(consentScreen.getScopes(), is(List.of("scope1", "scope2")));

        Document submit = consentScreen.submit();
        flow.expectSuccessfulRedirect(submit.connection().response());

        assertThat(flow.getCode(), is(notNullValue()));
        assertThat(flow.getAccessToken(), is(nullValue()));
        ErrorResponse errorResponse = flow.exchangeCode().expectError(400);

        assertThat(errorResponse.error(), is(ErrorResponse.Error.access_denied));
    }
}