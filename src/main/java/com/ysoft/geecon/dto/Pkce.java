package com.ysoft.geecon.dto;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

public class Pkce {
    public static boolean validate(String challengeMethod, String codeChallenge, String codeVerifier) {
        return switch (challengeMethod) {
            case "plain" -> codeVerifier.equals(codeChallenge);
            case "S256" -> codeChallenge.equals(s256(codeVerifier));
            default -> false;
        };
    }

    public static String s256(String codeVerifier) {
        return Base64.encodeBase64URLSafeString(DigestUtils.sha256(codeVerifier));
    }
}
