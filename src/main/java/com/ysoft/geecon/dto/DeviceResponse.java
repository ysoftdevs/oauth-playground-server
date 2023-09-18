package com.ysoft.geecon.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.net.URI;

public record DeviceResponse(
        @JsonProperty("device_code") String deviceCode,
        @JsonProperty("user_code") String userCode,
        @JsonProperty("verification_uri") URI verificationUri,
        @JsonProperty("interval") long interval,
        @JsonProperty("expires_in") long expiresIn
) {
}
