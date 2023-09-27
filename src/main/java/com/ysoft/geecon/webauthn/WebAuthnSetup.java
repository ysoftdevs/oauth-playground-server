package com.ysoft.geecon.webauthn;

import com.ysoft.geecon.dto.User;
import com.ysoft.geecon.error.OAuthApiException;
import com.ysoft.geecon.repo.UsersRepo;
import io.quarkus.runtime.StartupEvent;
import io.quarkus.security.webauthn.WebAuthnUserProvider;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.Json;
import io.vertx.ext.auth.webauthn.AttestationCertificates;
import io.vertx.ext.auth.webauthn.Authenticator;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class WebAuthnSetup implements WebAuthnUserProvider {
    @Inject
    UsersRepo usersRepo;

    private static List<Authenticator> toAuthenticators(List<WebAuthnCredential> dbs) {
        return dbs.stream().map(WebAuthnSetup::toAuthenticator).toList();
    }

    private static Uni<List<Authenticator>> loadCredentials(Optional<User> user) {
        var authenticators = user.map(u -> toAuthenticators(u.credentials())).filter(l -> !l.isEmpty());
        return Uni.createFrom().item(authenticators.orElse(List.of()));
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

    public void init(@Observes StartupEvent e, Router router) {
        router.route("/q/webauthn/*").failureHandler((RoutingContext context) -> {
            if (context.failure() instanceof OAuthApiException exception) {
                context.response()
                        .setStatusMessage("Forbidden")
                        .setStatusCode(403)
                        .end(Json.encodePrettily(exception.getResponse().getEntity()));
            } else {
                context.response()
                        .setStatusMessage("Internal Error")
                        .setStatusCode(500)
                        .end(context.failure().getMessage());
            }
        });
    }

    @Override
    public Uni<List<Authenticator>> findWebAuthnCredentialsByUserName(String userName) {
        return loadCredentials(usersRepo.getUser(userName));
    }

    @Override
    public Uni<List<Authenticator>> findWebAuthnCredentialsByCredID(String credID) {
        return loadCredentials(usersRepo.findByCredID(credID));
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
            usersRepo.register(new User(authenticator.getUserName(), credential));
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