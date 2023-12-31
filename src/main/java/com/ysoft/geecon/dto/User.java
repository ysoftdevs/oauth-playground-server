package com.ysoft.geecon.dto;

import com.ysoft.geecon.repo.SecureRandomStrings;
import com.ysoft.geecon.webauthn.WebAuthnCredential;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record User(String id, String login, String password, List<WebAuthnCredential> credentials) {
    public User(String login, String password) {
        this(SecureRandomStrings.alphanumeric(5), login, password, List.of());
    }

    public User(String login, WebAuthnCredential credential) {
        this(SecureRandomStrings.alphanumeric(5), login, null, List.of(credential));
    }

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
            existing.get().counter = webAuthnCredential.counter;
            newCredentials = credentials;
        } else {
            newCredentials = new ArrayList<>(credentials);
            newCredentials.add(webAuthnCredential);
        }

        return new User(id, login, password, newCredentials);
    }
}
