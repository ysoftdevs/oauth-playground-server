package com.ysoft.geecon.error;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

import java.util.Arrays;

@ApplicationScoped
class ExceptionMappers {
    @Inject
    ResourceInfo resourceInfo;

    @ServerExceptionMapper
    public Response exception(OAuthException exception) {
        Object entity = producesJson() ? exception.getResponse() : Templates.error(exception.getResponse());
        return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
    }

    private boolean producesJson() {
        Produces annotation = resourceInfo.getResourceMethod().getAnnotation(Produces.class);
        if (annotation == null) {
            return false;
        }
        String[] produces = annotation.value();
        return Arrays.asList(produces).contains(MediaType.APPLICATION_JSON);
    }


    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance error(ErrorResponse response);
    }
}