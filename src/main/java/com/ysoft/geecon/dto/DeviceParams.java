package com.ysoft.geecon.dto;

import jakarta.ws.rs.FormParam;

public final class DeviceParams {
    @FormParam("client_id")
    String clientId;

    public String getClientId() {
        return clientId;
    }

    public AuthParams toAuthParams() {
        var params = new AuthParams();
        params.setClientId(clientId);
        return params;
    }
}
