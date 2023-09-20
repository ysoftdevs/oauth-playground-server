package com.ysoft.geecon.dto;

import com.ysoft.geecon.webauthn.WebAuthnCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record User(String login, String password, List<WebAuthnCredential> credentials) {
    public boolean validatePassword(String password) {
        return this.password != null && this.password.equals(password);
    }

    public User withAddedCredential(WebAuthnCredential webAuthnCredential) {
        Optional<WebAuthnCredential> existing = credentials.stream()
                .filter(credential -> credential.credID.equals(webAuthnCredential.credID))
                .findAny();

        List<WebAuthnCredential> newCredentials;
        if (existing.isPresent()) {
            // TODO need to decide if immutable or not
            existing.get().counter++;
            newCredentials = credentials;
        } else {
            newCredentials = new ArrayList<>(credentials);
            newCredentials.add(webAuthnCredential);
        }

        return new User(login, password, newCredentials);
    }
}
