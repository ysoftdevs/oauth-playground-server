package com.ysoft.geecon;

import com.ysoft.geecon.dto.AuthParams;
import com.ysoft.geecon.dto.OAuthClient;
import com.ysoft.geecon.dto.User;
import com.ysoft.geecon.error.OAuthException;
import com.ysoft.geecon.repo.ClientsRepo;
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

import java.util.List;

@Path("/auth")
public class OAuthResource {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance login(String loginHint, String sessionId, String error);

        public static native TemplateInstance consents(User user, OAuthClient client, List<String> scopes, String sessionId, String error);
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
        return Templates.login(params.getLoginHint(),  sessionId, "");
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Object post(AuthParams params,
                       @FormParam("sessionId") String sessionId,
                       @FormParam("username") String username,
                       @FormParam("password") String password,
                       @FormParam("scope") List<String> scopes) {


        var session = sessionsRepo.getSession(sessionId).orElseThrow(() -> new OAuthException("Invalid session"));
        if (session.user() == null) {
            var user = validateUser(username, password);
            if (user == null) {
                return Templates.login(username, sessionId, "invalid_credentials");
            } else {
                session = sessionsRepo.assignUser(sessionId, user);
            }
        }

        if (session.acceptedScopes() == null) {
            if (scopes == null || scopes.isEmpty()) {
                return Templates.consents(session.user(), session.client(), session.params().getScopes(), sessionId, "");
            }
        }

        String authCode = sessionsRepo.finishSession(sessionId, scopes);
        return Response.seeOther(UriBuilder.fromUri(params.getRedirectUri())
                        .queryParam("code", authCode)
                        .queryParam("state", params.getState())
                        .build())
                .build();
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

}






