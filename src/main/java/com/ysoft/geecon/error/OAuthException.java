package com.ysoft.geecon.error;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OAuthException extends RuntimeException {
    private final ErrorResponse response;

    // https://www.ietf.org/archive/id/draft-ietf-oauth-v2-1-09.html#name-error-response-2
    public OAuthException(ErrorResponse response) {
        super("OAuth error: " + response.error() + " " + response.description());
        this.response = response;
    }

    public OAuthException(String error, String description) {
        this(new ErrorResponse(error, description));
    }

    @Deprecated
    public OAuthException(String message) {
        this(message, message);
    }

    public ErrorResponse getResponse() {
        return response;
    }

    public record ErrorResponse(@JsonProperty("error") String error,
                                @JsonProperty("error_description") String description) {
    }
}
