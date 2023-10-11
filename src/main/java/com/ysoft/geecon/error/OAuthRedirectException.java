package com.ysoft.geecon.error;

import com.ysoft.geecon.dto.AuthParams;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import org.apache.commons.lang3.StringUtils;

public class OAuthRedirectException extends OAuthApiException {
    private final AuthParams authParams;

    public OAuthRedirectException(AuthParams authParams, ErrorResponse.Error error, String description) {
        super(error, description);
        this.authParams = authParams;
    }

    public AuthParams getAuthParams() {
        return authParams;
    }

    public Response getResponse() {
        UriBuilder uri = UriBuilder.fromUri(authParams.getRedirectUri())
                .fragment("")
                .queryParam("error", response.error())
                .queryParam("error_description", response.description());
        if (StringUtils.isNotBlank(authParams.getState())) {
            uri.queryParam("state", authParams.getState());
        }
        return Response.seeOther(uri.build()).build();
    }
}
