package com.ysoft.geecon;

import com.ysoft.geecon.dto.AccessTokenResponse;
import com.ysoft.geecon.dto.DeviceResponse;
import com.ysoft.geecon.dto.OAuthClient;
import com.ysoft.geecon.dto.User;
import com.ysoft.geecon.error.ErrorResponse;
import com.ysoft.geecon.helpers.ConsentScreen;
import com.ysoft.geecon.helpers.DeviceAuthorizationGrantFlow;
import com.ysoft.geecon.helpers.DeviceCodeScreen;
import com.ysoft.geecon.helpers.LoginScreen;
import com.ysoft.geecon.repo.ClientsRepo;
import com.ysoft.geecon.repo.UsersRepo;
import io.quarkus.test.common.http.TestHTTPResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.jsoup.HttpStatusException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
public class DeviceAuthGrantTest {

    public static final OAuthClient CLIENT = new OAuthClient("deviceclient", "", null, null);
    @Inject
    ClientsRepo clientsRepo;
    @Inject
    UsersRepo usersRepo;

    @TestHTTPResource("auth/device")
    String deviceUri;

    @TestHTTPResource("auth/device-login")
    URI deviceLoginUri;

    @BeforeEach
    void beforeAll() {
        clientsRepo.register(CLIENT);
        usersRepo.register(new User("bob", "password", List.of()));
    }


    @Test
    public void deviceAuthGrant() throws IOException {
        DeviceAuthorizationGrantFlow flow = new DeviceAuthorizationGrantFlow(deviceUri, CLIENT);
        DeviceResponse deviceResponse = flow.start();

        DeviceCodeScreen deviceCodeScreen = new DeviceCodeScreen(deviceResponse.verificationUri());
        LoginScreen loginScreen = deviceCodeScreen.enterCode(deviceResponse.userCode());

        ConsentScreen consentScreen = loginScreen.submit("bob", "password").expectSuccess();
        consentScreen.submit();

        AccessTokenResponse accessTokenResponse = flow.exchangeDeviceCode().expectTokens();
        assertThat(accessTokenResponse.accessToken(), is(notNullValue()));
    }

    @Test
    public void deviceAuthGrant_invalidUserCode() throws IOException {
        DeviceCodeScreen deviceCodeScreen = new DeviceCodeScreen(deviceLoginUri);

        HttpStatusException exception = assertThrows(HttpStatusException.class, () -> deviceCodeScreen.enterCode("somecode"));
        assertThat(exception.getStatusCode(), is(404));
    }

    @Test
    public void deviceAuthGrant_authorizationPending() {
        DeviceAuthorizationGrantFlow flow = new DeviceAuthorizationGrantFlow(deviceUri, CLIENT);
        flow.start();
        ErrorResponse errorResponse = flow.exchangeDeviceCode().expectError(400);
        assertThat(errorResponse.error(), is(ErrorResponse.Error.authorization_pending));
    }
}