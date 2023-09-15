package com.ysoft.geecon.error;

public class OAuthException extends RuntimeException {
    // https://www.ietf.org/archive/id/draft-ietf-oauth-v2-1-09.html#name-error-response-2
    public OAuthException(String message) {
        super(message);
    }
}
