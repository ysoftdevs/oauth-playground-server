package com.ysoft.geecon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DeviceResponse(
        @JsonProperty("device_code") String deviceCode,
        @JsonProperty("user_code") String userCode,
        @JsonProperty("verification_uri") String verificationUri,
        @JsonProperty("interval") long interval,
        @JsonProperty("expires_in") long expiresIn
) {
}
