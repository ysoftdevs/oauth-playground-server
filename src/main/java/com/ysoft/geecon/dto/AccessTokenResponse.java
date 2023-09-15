package com.ysoft.geecon.dto;

public record AccessTokenResponse(String tokenType, long expiresIn, String accessToken, String scope, String refreshToken, String idToken) {
}