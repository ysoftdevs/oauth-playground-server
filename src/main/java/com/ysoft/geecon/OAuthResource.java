package com.ysoft.geecon;

import com.ysoft.geecon.dto.*;
import com.ysoft.geecon.error.ErrorResponse;
import com.ysoft.geecon.error.OAuthApiException;
import com.ysoft.geecon.error.OAuthRedirectException;
import com.ysoft.geecon.error.OAuthUserVisibleException;
import com.ysoft.geecon.repo.ClientsRepo;
import com.ysoft.geecon.repo.SessionsRepo;
import com.ysoft.geecon.repo.UsersRepo;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.security.webauthn.WebAuthnLoginResponse;
import io.quarkus.security.webauthn.WebAuthnRegisterResponse;
import io.quarkus.security.webauthn.WebAuthnSecurity;
import io.smallrye.common.annotation.Blocking;
import io.vertx.ext.auth.webauthn.Authenticator;
import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Path("/auth")
public class OAuthResource {

    @Inject
    ClientsRepo clientsRepo;

    @Inject
    UsersRepo usersRepo;
    @Inject
    SessionsRepo sessionsRepo;
    @Inject
    UriInfo uriInfo;
    @Inject
    WebAuthnSecurity webAuthnSecurity;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(AuthParams params) {
        var client = validateClient(params);
        String sessionId = sessionsRepo.newAuthorizationSession(params, client);
        return Templates.login(params.getLoginHint(), sessionId, "");
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Object post(@FormParam("sessionId") String sessionId,
                       @FormParam("username") String username,
                       @FormParam("password") String password) {


        sessionsRepo.getSession(sessionId).orElseThrow(
                () -> new OAuthUserVisibleException(ErrorResponse.Error.access_denied, "Invalid session"));
        var user = validateUser(username, password);
        if (user == null) {
            return Templates.login(username, sessionId, "invalid_credentials");
        } else {
            AuthorizationSession session = sessionsRepo.assignUser(sessionId, user);
            return Templates.consents(session.user(), session.client(), session.params().getScopes(), sessionId, "");
        }
    }

    @GET
    @Path("passwordless")
    @Produces(MediaType.TEXT_HTML)
    @Blocking
    public TemplateInstance getPasswordless(AuthParams params) {
        var client = validateClient(params);
        String sessionId = sessionsRepo.newAuthorizationSession(params, client);
        return Templates.loginPasswordless(params.getLoginHint(), sessionId, "");
    }

    @POST
    @Path("passwordless")
    @Produces(MediaType.TEXT_HTML)
    @Blocking
    public TemplateInstance postPasswordless(@FormParam("sessionId") String sessionId) {
        AuthorizationSession session = sessionsRepo.getSession(sessionId).orElseThrow(
                () -> new OAuthUserVisibleException(ErrorResponse.Error.access_denied, "Invalid session"));
        return Templates.loginPasswordless(session.params().getLoginHint(), sessionId, "");
    }

    @POST
    @Path("passwordless/register")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Blocking
    public TemplateInstance registerPasswordless(@FormParam("sessionId") String sessionId,
                                                 @BeanParam WebAuthnRegisterResponse webAuthnResponse,
                                                 RoutingContext ctx) {

        sessionsRepo.getSession(sessionId).orElseThrow(
                () -> new OAuthUserVisibleException(ErrorResponse.Error.access_denied, "Invalid session"));
        // Input validation
        if (!webAuthnResponse.isSet() || !webAuthnResponse.isValid()) {
            return Templates.loginPasswordless("", sessionId, "Invalid request");
        }

        Authenticator authenticator = this.webAuthnSecurity.register(webAuthnResponse, ctx)
                .await().indefinitely();

        var user = usersRepo.getUser(authenticator.getUserName()).orElseThrow();
        AuthorizationSession session = sessionsRepo.assignUser(sessionId, user);
        return Templates.consents(session.user(), session.client(), session.params().getScopes(), sessionId, "");
    }

    @POST
    @Path("passwordless/login")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Blocking
    public TemplateInstance loginPasswordless(@FormParam("sessionId") String sessionId,
                                              @BeanParam WebAuthnLoginResponse webAuthnResponse,
                                              RoutingContext ctx) {

        sessionsRepo.getSession(sessionId).orElseThrow(
                () -> new OAuthUserVisibleException(ErrorResponse.Error.access_denied, "Invalid session"));
        // Input validation
        if (!webAuthnResponse.isSet() || !webAuthnResponse.isValid()) {
            return Templates.loginPasswordless("", sessionId, "Invalid request");
        }

        Authenticator authenticator = this.webAuthnSecurity.login(webAuthnResponse, ctx)
                .await().indefinitely();

        var user = usersRepo.getUser(authenticator.getUserName()).orElseThrow();
        AuthorizationSession session = sessionsRepo.assignUser(sessionId, user);
        return Templates.consents(session.user(), session.client(), session.params().getScopes(), sessionId, "");
    }

    @POST
    @Path("consent")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postConsent(
            @FormParam("sessionId") String sessionId,
            @FormParam("scope") List<String> scopes) {

        sessionsRepo.getSession(sessionId).orElseThrow(() -> new OAuthUserVisibleException(ErrorResponse.Error.access_denied, "Invalid session"));
        var session = sessionsRepo.authorizeSession(sessionId, scopes);

        String redirectUri = session.params().getRedirectUri();
        if (StringUtils.isNotBlank(redirectUri)) {
            var responseTypes = session.params().getResponseTypes();

            UriBuilder uri = UriBuilder.fromUri(redirectUri)
                    .fragment("");

            if (StringUtils.isNotBlank(session.params().getState())) {
                uri.queryParam("state", session.params().getState());
            }
            if (responseTypes.contains(AuthParams.ResponseType.code)) {
                uri.queryParam("code", sessionsRepo.generateAuthorizationCode(sessionId));
            }
            if (responseTypes.contains(AuthParams.ResponseType.token)) {
                uri.queryParam("access_token", session.tokens().accessToken());
            }
            if (responseTypes.contains(AuthParams.ResponseType.id_token)) {
                uri.queryParam("id_token", session.tokens().idToken());
            }
            return Response.seeOther(uri.build()).build();
        } else {
            return Response.ok(Templates.loginSuccess()).build();
        }
    }

    @POST
    @Path("/device")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public DeviceResponse device(DeviceParams params) {
        var client = validateClient(params);
        AuthParams authParams = new AuthParams();
        authParams.setClientId(params.getClientId());
        String sessionId = sessionsRepo.newAuthorizationSession(authParams, client);

        return new DeviceResponse(
                sessionsRepo.generateAuthorizationCode(sessionId),
                sessionsRepo.generateUserCode(sessionId),
                uriInfo.getBaseUriBuilder()
                        .path(OAuthResource.class)
                        .path(OAuthResource.class, "enterDeviceCode").build(),
                5,
                180
        );
    }

    @POST
    @Path("/token")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public AccessTokenResponse token(TokenParams params) {
        return switch (params.getGrantType()) {
            case "authorization_code" -> redeemAuthorizationCode(params);
            case "urn:ietf:params:oauth:grant-type:device_code" -> redeemDeviceCode(params);
            default ->
                    throw new OAuthApiException(ErrorResponse.Error.unsupported_grant_type, "Unsupported grant type");
        };
    }

    @GET
    @Path("/device-login")
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance enterDeviceCode() {
        return Templates.deviceLogin("");
    }

    @POST
    @Path("/device-login")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postDeviceCode(@FormParam("code") String code) {
        return sessionsRepo.redeemUserCode(code)
                .map(session -> Response.ok(Templates.login(session.params().getLoginHint(), session.sessionId(), "")))
                .orElse(Response.status(404).entity(Templates.deviceLogin("invalid_code"))).build();
    }

    private OAuthClient validateClient(DeviceParams params) {
        return clientsRepo.getClient(params.getClientId())
                .orElseThrow(() -> new RuntimeException("Not a valid client"));
    }

    private AccessTokenResponse redeemAuthorizationCode(TokenParams params) {
        var session = sessionsRepo.redeemAuthorizationCode(params.getCode())
                .orElseThrow(() -> new OAuthApiException(ErrorResponse.Error.access_denied, "Invalid code"));
        validateClient(params, session);
        if (!session.validateCodeChallenge(params.getCodeVerifier())) {
            throw new OAuthApiException(ErrorResponse.Error.access_denied, "Invalid code verifier");
        }
        return session.tokens();
    }

    private AccessTokenResponse redeemDeviceCode(TokenParams params) {
        var session = sessionsRepo.getByAuthorizationCode(params.getDeviceCode())
                .orElseThrow(() -> new OAuthApiException(ErrorResponse.Error.access_denied, "Invalid device code"));
        validateClient(params, session);
        if (session.tokens() != null) {
            sessionsRepo.redeemAuthorizationCode(params.getDeviceCode());
            return session.tokens();
        } else {
            throw new OAuthApiException(ErrorResponse.Error.authorization_pending, "Authorization pending");
        }
    }

    private User validateUser(String username, String password) {
        return usersRepo.getUser(username)
                .filter(u -> u.validatePassword(password)).orElse(null);

    }

    private OAuthClient validateClient(AuthParams params) {
        var client = clientsRepo.getClient(params.getClientId())
                // must NOT redirect to not validated client
                .orElseThrow(() -> new OAuthUserVisibleException(ErrorResponse.Error.invalid_request, "Not a valid client"));
        if (!client.validateRedirectUri(params.getRedirectUri())) {
            // must NOT redirect to invalid redirect URI
            throw new OAuthUserVisibleException(ErrorResponse.Error.invalid_request, "Invalid redirect URI");
        }
        // state is optional
//        if (StringUtil.isNullOrEmpty(params.getState())) {
//            throw new OAuthRedirectException(params, ErrorResponse.Error.invalid_request, "Missing state");
//        }
        if (!params.validateResponseType()) {
            throw new OAuthRedirectException(params, ErrorResponse.Error.unsupported_response_type,
                    "Unsupported response type");
        }
        return client;
    }

    private OAuthClient validateClient(TokenParams params, AuthorizationSession session) {
        var client = clientsRepo.getClient(params.getClientId())
                .orElseThrow(() -> new OAuthApiException(ErrorResponse.Error.invalid_request, "Not a valid client"));
        if (!session.validateRedirectUri(params.getRedirectUri())) {
            throw new OAuthApiException(ErrorResponse.Error.invalid_request, "Invalid redirect URI");
        }
        if (!client.validateSecret(params.getClientSecret())) {
            throw new OAuthApiException(ErrorResponse.Error.unauthorized_client, "Invalid secret");
        }
        return client;
    }

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance login(String loginHint, String sessionId, String error);

        public static native TemplateInstance loginPasswordless(String loginHint, String sessionId, String error);

        public static native TemplateInstance loginSuccess();

        public static native TemplateInstance consents(User user, OAuthClient client, List<String> scopes, String sessionId, String error);

        public static native TemplateInstance deviceLogin(String error);
    }
}






