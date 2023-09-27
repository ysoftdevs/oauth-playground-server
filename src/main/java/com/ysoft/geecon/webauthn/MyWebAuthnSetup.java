package com.ysoft.geecon.webauthn;

import com.ysoft.geecon.dto.User;
import com.ysoft.geecon.repo.UsersRepo;
import io.quarkus.security.webauthn.WebAuthnUserProvider;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.auth.webauthn.AttestationCertificates;
import io.vertx.ext.auth.webauthn.Authenticator;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

import java.util.List;

@ApplicationScoped
public class MyWebAuthnSetup implements WebAuthnUserProvider {
    public static final String AUTHORIZED_USER = MyWebAuthnSetup.class.getPackageName() + "#AUTHORIZED_USER";
    @Inject
    UsersRepo usersRepo;

    @Inject
    RoutingContext routingContext;

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
        WebAuthnCredential credential = new WebAuthnCredential(authenticator);

        var existingUser = usersRepo.getUser(authenticator.getUserName());
        var existingCredential = existingUser.stream().flatMap(u -> u.credentials().stream())
                .filter(c -> authenticator.getCredID().equals(c.credID)).findAny();

        if (existingUser.isPresent() && existingCredential.isPresent()) {
            // returning user and credential -> update counter
            usersRepo.register(existingUser.get().withAddedCredential(credential));
            return Uni.createFrom().nullItem();
        } else if (existingUser.isEmpty()) {
            // new user -> register
            usersRepo.register(new User(authenticator.getUserName(), null, List.of(credential)));
            return Uni.createFrom().nullItem();
        } else {
            // in production, we should not add a new credentials to an existing user
            // unless we have another means of verifying their identity
            // return Uni.createFrom().failure(new Throwable("Duplicate user: " + authenticator.getUserName()));

            // But, for this demo, this is exactly what we are doing.
            // Just let anyone register a credential in anyone's name
            usersRepo.register(existingUser.get().withAddedCredential(credential));
            return Uni.createFrom().nullItem();
        }
    }
}