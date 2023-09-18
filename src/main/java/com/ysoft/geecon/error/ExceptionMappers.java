package com.ysoft.geecon.error;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

class ExceptionMappers {
    @ServerExceptionMapper
    @Produces(MediaType.APPLICATION_JSON)
    public Response mapJson(OAuthException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(exception.getResponse()).build();
    }

    @ServerExceptionMapper
    @Produces(MediaType.APPLICATION_JSON)
    public Response mapHtml(OAuthException exception) {
        return Response.status(Response.Status.BAD_REQUEST).entity(Templates.error(exception.getResponse())).build();
    }

    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance error(OAuthException.ErrorResponse response);
    }
}