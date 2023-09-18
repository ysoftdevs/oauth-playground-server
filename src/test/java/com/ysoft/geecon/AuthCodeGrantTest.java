package com.ysoft.geecon;

import com.ysoft.geecon.dto.OAuthClient;
import com.ysoft.geecon.dto.User;
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
        AuthorizationCodeFlow flow = new AuthorizationCodeFlow(authUrl, CLIENT);
        LoginScreen loginScreen = flow.start(Map.of("scope", "scope1 scope2"));

        ConsentScreen consentScreen = loginScreen.submitCorrect("bob", "password");
        assertThat(consentScreen.getScopes(), is(List.of("scope1", "scope2")));

        Document submit = consentScreen.submit();
        flow.parseAndValidateRedirect(submit.connection().response());

        assertThat(flow.getCode(), is(notNullValue()));
        assertThat(flow.getToken(), is(nullValue()));
        flow.exchangeCode();

        assertThat(flow.getToken(), is(notNullValue()));
    }

}