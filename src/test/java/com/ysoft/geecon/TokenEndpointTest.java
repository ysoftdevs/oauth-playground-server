package com.ysoft.geecon;

import com.ysoft.geecon.dto.OAuthClient;
import com.ysoft.geecon.error.ErrorResponse;
import com.ysoft.geecon.helpers.TokenEndpointCall;
import com.ysoft.geecon.repo.ClientsRepo;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@QuarkusTest
public class TokenEndpointTest {
    public static final OAuthClient CLIENT = new OAuthClient("deviceclient", "", null, null);
    @Inject
    ClientsRepo clientsRepo;

    @BeforeEach
    void beforeAll() {
        clientsRepo.register(CLIENT);
    }

    @Test
    public void invalidGrant() {
        ErrorResponse errorResponse = new TokenEndpointCall(CLIENT).grantType("invalid").expectError(400);
        assertThat(errorResponse.error(), is(ErrorResponse.Error.unsupported_grant_type));
        assertThat(errorResponse.description(), is(notNullValue()));
    }
}