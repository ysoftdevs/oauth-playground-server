package com.ysoft.geecon.error;

public class OAuthException extends RuntimeException {
    private final ErrorResponse response;

    public OAuthException(ErrorResponse response) {
        super("OAuth error: " + response.error() + " " + response.description());
        this.response = response;
    }

    public OAuthException(ErrorResponse.Error error, String description) {
        this(new ErrorResponse(error, description));
    }

    public ErrorResponse getResponse() {
        return response;
    }

}
