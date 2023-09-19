package com.ysoft.geecon.error;

import jakarta.ws.rs.core.Response;

public class OAuthApiException extends OAuthException {

    public OAuthApiException(ErrorResponse response) {
        super("OAuth error: " + response.error() + " " + response.description(), response);
    }

    public OAuthApiException(ErrorResponse.Error error, String description) {
        this(new ErrorResponse(error, description));
    }

    @Override
    public Response getResponse() {
        return Response.status(Response.Status.BAD_REQUEST).entity(response).build();
    }

}
