package com.ysoft.geecon;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

import java.util.List;

@Path("/auth")
public class OAuthResource {

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance login(String loginHint, String error);
        public static native TemplateInstance consents(List<String> scopes, String error);
    }



    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("login_hint") String loginHint) {
        return Templates.login(loginHint, "");
    }

    @POST
    @Produces(MediaType.TEXT_HTML)
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public TemplateInstance post(@FormParam("username") String username,
                                 @FormParam("password") String password ) {
        if ("Password1".equals(password)) {
            return Templates.consents(List.of("scope1"), "");
        } else {
            return Templates.login(username, "invalid_credentials");
        }
    }
}
