package com.ysoft.geecon.dto;

public record AccessTokenResponse(String token, String scope, String idToken, long expiresIn) {
}
