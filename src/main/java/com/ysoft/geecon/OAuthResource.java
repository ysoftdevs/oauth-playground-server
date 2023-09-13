package com.ysoft.geecon;

import com.ysoft.geecon.dto.OAuthClient;
import com.ysoft.geecon.repo.ClientsRepo;
import com.ysoft.geecon.repo.UsersRepo;
import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import io.quarkus.runtime.util.StringUtil;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.jboss.resteasy.reactive.RestQuery;

import java.net.URI;
import java.util.List;

@Path("/auth")
public class OAuthResource {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance login(String loginHint, String error);

        public static native TemplateInstance consents(List<String> scopes, String error);
    }

    @Inject
    ClientsRepo clientsRepo;
    @Inject
    UsersRepo usersRepo;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(AuthParams params) {
        validateClient(params);

        return Templates.login(params.loginHint, "");
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public Object post(AuthParams params,
                       @FormParam("username") String username,
                       @FormParam("password") String password) {
        validateClient(params);
        var user = usersRepo.getUser(username);
        if (user.isEmpty()) {
            return Templates.login(username, "invalid_credentials");
        }
        if (!user.get().validatePassword(password)) {
            return Templates.login(username, "invalid_credentials");
        }

        return Response.seeOther(UriBuilder.fromUri(params.redirectUri)
                .queryParam("code", "randomCode")
                .queryParam("state", params.state)
                .build())
                .build();
    }

    private OAuthClient validateClient(AuthParams params) {
        var client = clientsRepo.getClient(params.clientId)
                .orElseThrow(() -> new RuntimeException("Not a valid client"));
        if (!client.validateRedirectUri(params.redirectUri)) {
            throw new RuntimeException("Invalid redirect URI");
        }
        if (StringUtil.isNullOrEmpty(params.state)) {
            throw new RuntimeException("Invalid state");
        }
        return client;
    }

    public static class AuthParams {
        public enum ResponseType {
            code
        }

        @RestQuery("login_hint")
        String loginHint;
        @RestQuery("response_type")
        ResponseType responseType;
        @RestQuery("client_id")
        String clientId;
        @RestQuery("redirect_uri")
        String redirectUri;
        @RestQuery("scope")
        String scope;
        @RestQuery("state")
        String state;

        public String getLoginHint() {
            return loginHint;
        }

        public void setLoginHint(String loginHint) {
            this.loginHint = loginHint;
        }

        public ResponseType getResponseType() {
            return responseType;
        }

        public void setResponseType(ResponseType responseType) {
            this.responseType = responseType;
        }

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public String getRedirectUri() {
            return redirectUri;
        }

        public void setRedirectUri(String redirectUri) {
            this.redirectUri = redirectUri;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scope) {
            this.scope = scope;
        }

        public String getState() {
            return state;
        }

        public void setState(String state) {
            this.state = state;
        }
    }

}






