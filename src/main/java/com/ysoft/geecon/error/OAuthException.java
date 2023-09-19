package com.ysoft.geecon.error;

import jakarta.ws.rs.core.Response;

public abstract class OAuthException extends RuntimeException {
    protected final ErrorResponse response;

    public OAuthException(String message, ErrorResponse response) {
        super(message);
        this.response = response;
    }

    public abstract Response getResponse();
}
