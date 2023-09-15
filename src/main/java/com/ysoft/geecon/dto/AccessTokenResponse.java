package com.ysoft.geecon.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record AccessTokenResponse(@JsonProperty("token_type") String tokenType,
                                  @JsonProperty("expires_in") long expiresIn,
                                  @JsonProperty("access_token") String accessToken,
                                  String scope,
                                  @JsonProperty("refresh_token") String refreshToken,
                                  @JsonProperty("id_token") String idToken) {
}