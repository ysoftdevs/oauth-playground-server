package com.ysoft.geecon.error;

import io.quarkus.qute.CheckedTemplate;
import io.quarkus.qute.TemplateInstance;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.server.ServerExceptionMapper;

@ApplicationScoped
class ExceptionMappers {

    @ServerExceptionMapper
    public Response exception(OAuthException exception) {
        return exception.getResponse();
    }


    @CheckedTemplate
    public static class Templates {
        public static native TemplateInstance error(ErrorResponse response);
    }
}