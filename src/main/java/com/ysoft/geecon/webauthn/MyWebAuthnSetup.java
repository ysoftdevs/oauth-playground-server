package com.ysoft.geecon.webauthn;

import com.ysoft.geecon.dto.User;
import com.ysoft.geecon.repo.UsersRepo;
import io.quarkus.security.webauthn.WebAuthnUserProvider;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.auth.webauthn.AttestationCertificates;
import io.vertx.ext.auth.webauthn.Authenticator;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class MyWebAuthnSetup implements WebAuthnUserProvider {
    @Inject
    UsersRepo usersRepo;

    private static List<Authenticator> toAuthenticators(List<WebAuthnCredential> dbs) {
        return dbs.stream().map(MyWebAuthnSetup::toAuthenticator).toList();
    }

    private static Authenticator toAuthenticator(WebAuthnCredential credential) {
        Authenticator ret = new Authenticator();
        ret.setAaguid(credential.aaguid);
        AttestationCertificates attestationCertificates = new AttestationCertificates();
        attestationCertificates.setAlg(credential.alg);
        attestationCertificates.setX5c(credential.x5c);
        ret.setAttestationCertificates(attestationCertificates);
        ret.setCounter(credential.counter);
        ret.setCredID(credential.credID);
        ret.setFmt(credential.fmt);
        ret.setPublicKey(credential.publicKey);
        ret.setType(credential.type);
        ret.setUserName(credential.userName);
        return ret;
    }

    @Override
    public Uni<List<Authenticator>> findWebAuthnCredentialsByUserName(String userName) {
        return Uni.createFrom().item(usersRepo.getUser(userName)
                .map((User dbs) -> toAuthenticators(dbs.credentials()))
                .orElse(List.of())
        );
    }

    @Override
    public Uni<List<Authenticator>> findWebAuthnCredentialsByCredID(String credID) {
        return Uni.createFrom().item(usersRepo.findByCredID(credID)
                .map((User dbs) -> toAuthenticators(dbs.credentials()))
                .orElse(List.of())
        );
    }

    @Override
    public Uni<Void> updateOrStoreWebAuthnCredentials(Authenticator authenticator) {
        WebAuthnCredential credential1 = new WebAuthnCredential(authenticator);

        var existingUser = usersRepo.getUser(authenticator.getUserName());
        var existingCredential = existingUser.stream().flatMap(u -> u.credentials().stream())
                .filter(c -> authenticator.getCredID().equals(c.credID)).findAny();

        if (existingUser.isPresent() && existingCredential.isPresent()) {
            // returning user and credential -> update counter
            usersRepo.register(existingUser.get().withAddedCredential(existingCredential.get()));
            return Uni.createFrom().nullItem();
        } else if (existingUser.isEmpty()) {
            // new user -> register
            usersRepo.register(new User(authenticator.getUserName(), null, List.of(credential1)));
            return Uni.createFrom().nullItem();
        } else {
            // returning (or duplicate) user with new credential -> reject,
            // as we do not provide a means to register additional credentials yet
            return Uni.createFrom().failure(new Throwable("Duplicate user"));
        }
    }
}