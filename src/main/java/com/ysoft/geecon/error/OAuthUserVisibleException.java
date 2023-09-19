package com.ysoft.geecon.error;

import jakarta.ws.rs.core.Response;

public class OAuthUserVisibleException extends OAuthApiException {

    public OAuthUserVisibleException(ErrorResponse.Error error, String description) {
        super(error, description);
    }

    public Response getResponse() {
        Object entity = ExceptionMappers.Templates.error(response);
        return Response.status(Response.Status.BAD_REQUEST).entity(entity).build();
    }
}
