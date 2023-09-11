package com.ysoft.geecon;

import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

@Path("/auth")
public class OAuthResource {
    @Inject
    Template hello;

    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance get(@QueryParam("login_hint") String loginHint) {
        return hello.data("loginHint", loginHint);
    }
//
//    @GET
//    @Produces(MediaType.TEXT_PLAIN)
//    public String hello() {
//        return "Auth";
//    }
}
