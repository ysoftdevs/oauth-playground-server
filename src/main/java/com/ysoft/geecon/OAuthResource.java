package com.ysoft.geecon;

import com.ysoft.geecon.dto.*;
import com.ysoft.geecon.error.OAuthException;
import com.ysoft.geecon.repo.ClientsRepo;
import com.ysoft.geecon.repo.SecureRandomStrings;
import com.ysoft.geecon.repo.SessionsRepo;
import com.ysoft.geecon.repo.UsersRepo;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.runtime.util.StringUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Path("/auth")
public class OAuthResource {

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Object post(AuthParams params,
                       @FormParam("sessionId") String sessionId,
                       @FormParam("username") String username,
                       @FormParam("password") String password,
                       @FormParam("scope") List<String> scopes) {


        sessionsRepo.getSession(sessionId).orElseThrow(() -> new OAuthException("Invalid session"));
        var user = validateUser(username, password);
        if (user == null) {
            return Templates.login(username, sessionId, "invalid_credentials");
        } else {
            AuthorizationSession session = sessionsRepo.assignUser(sessionId, user);
            return Templates.consents(session.user(), session.client(), session.params().getScopes(), sessionId, "");
        }
    }

    @Inject
    ClientsRepo clientsRepo;
    @Inject
    UsersRepo usersRepo;
    @Inject
    SessionsRepo sessionsRepo;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(AuthParams params) {
        var client = validateClient(params);
        String sessionId = sessionsRepo.newAuthorizationSession(params, client);
        return Templates.login(params.getLoginHint(), sessionId, "");
    }

    @POST
    @Path("consent")
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Response postConsent(
            @FormParam("sessionId") String sessionId,
            @FormParam("scope") List<String> scopes) {

        sessionsRepo.getSession(sessionId).orElseThrow(() -> new OAuthException("Invalid session"));
        var session = sessionsRepo.authorizeSession(sessionId, scopes);

        String redirectUri = session.params().getRedirectUri();
        if (StringUtils.isNotBlank(redirectUri)) {
            var responseTypes = session.params().getResponseTypes();

            UriBuilder uri = UriBuilder.fromUri(redirectUri)
                    .fragment("")
                    .queryParam("state", session.params().getState());

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
                "http://verificationuri/device-login",
                10,
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
            default -> throw new OAuthException("Unsupported grant type");
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
        validateClient(params);
        var session = sessionsRepo.redeemAuthorizationCode(params.getCode())
                .orElseThrow(() -> new OAuthException("Invalid code"));
        if (!session.validateCodeChallenge(params.getCodeVerifier())) {
            throw new OAuthException("Invalid code verifier");
        }

        String idToken = null;

        return new AccessTokenResponse("Bearer",
                8400,
                SecureRandomStrings.alphanumeric(50),
                session.scope(),
                SecureRandomStrings.alphanumeric(50),
                idToken
        );
    }

    private User validateUser(String username, String password) {
        return usersRepo.getUser(username)
                .filter(u -> u.validatePassword(password)).orElse(null);

    }

    private OAuthClient validateClient(AuthParams params) {
        var client = clientsRepo.getClient(params.getClientId())
                .orElseThrow(() -> new RuntimeException("Not a valid client"));
        if (!client.validateRedirectUri(params.getRedirectUri())) {
            throw new RuntimeException("Invalid redirect URI");
        }
        if (StringUtil.isNullOrEmpty(params.getState())) {
            throw new RuntimeException("Invalid state");
        }
        return client;
    }

    private OAuthClient validateClient(TokenParams params) {
        var client = clientsRepo.getClient(params.getClientId())
                .orElseThrow(() -> new RuntimeException("Not a valid client"));
        if (!client.validateRedirectUri(params.getRedirectUri())) {
            throw new RuntimeException("Invalid redirect URI");
        }
        if (!client.validateSecret(params.getClientSecret())) {
            throw new RuntimeException("Invalid secret");
        }
        return client;
    }

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance login(String loginHint, String sessionId, String error);

        public static native TemplateInstance loginSuccess();

        public static native TemplateInstance consents(User user, OAuthClient client, List<String> scopes, String sessionId, String error);

        public static native TemplateInstance deviceLogin(String error);
    }
}






