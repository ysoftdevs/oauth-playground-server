package com.ysoft.geecon.webauthn;

import com.ysoft.geecon.repo.UsersRepo;
import io.quarkus.security.webauthn.WebAuthnLoginResponse;
import io.quarkus.security.webauthn.WebAuthnRegisterResponse;
import io.quarkus.security.webauthn.WebAuthnSecurity;
import io.vertx.ext.auth.webauthn.Authenticator;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import java.net.URI;

@Path("webauthn")
public class LoginResource {

    @Inject
    WebAuthnSecurity webAuthnSecurity;
    @Inject
    UsersRepo usersRepo;

    @Path("/login")
    @POST
    public Response login(@BeanParam WebAuthnLoginResponse webAuthnResponse,
                          RoutingContext ctx) {
        // Input validation
        if (!webAuthnResponse.isSet()
                || !webAuthnResponse.isValid()) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        Authenticator authenticator = this.webAuthnSecurity.login(webAuthnResponse, ctx)
                .await().indefinitely();

        return Response.seeOther(URI.create("/")).build();
    }

    @Path("/register")
    @POST
    public Response register(@BeanParam WebAuthnRegisterResponse webAuthnResponse,
                             RoutingContext ctx) {
        // Input validation
        if (!webAuthnResponse.isSet() || !webAuthnResponse.isValid()) {
            return Response.status(Status.BAD_REQUEST).build();
        }

        Authenticator authenticator = this.webAuthnSecurity.register(webAuthnResponse, ctx)
                .await().indefinitely();

        return Response.seeOther(URI.create("/")).build();
    }
}