package com.ysoft.geecon.error;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorResponse(@JsonProperty("error") Error error,
                            @JsonProperty("error_description") String description) {
    public enum Error {
        invalid_request, unauthorized_client, unsupported_response_type, unsupported_grant_type,
        access_denied, invalid_scope, server_error, temporarily_unavailable, authorization_pending
    }
}
