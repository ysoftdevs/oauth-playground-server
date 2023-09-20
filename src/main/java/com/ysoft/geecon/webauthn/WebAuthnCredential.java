package com.ysoft.geecon.webauthn;

import io.vertx.ext.auth.webauthn.Authenticator;
import io.vertx.ext.auth.webauthn.PublicKeyCredential;

import java.util.ArrayList;
import java.util.List;


public class WebAuthnCredential {

    /**
     * The username linked to this authenticator
     */
    public String userName;

    /**
     * The type of key (must be "public-key")
     */
    public String type = "public-key";

    /**
     * The non user identifiable id for the authenticator
     */
    public String credID;

    /**
     * The public key associated with this authenticator
     */
    public String publicKey;

    /**
     * The signature counter of the authenticator to prevent replay attacks
     */
    public long counter;

    public String aaguid;

    /**
     * The Authenticator attestation certificates object, a JSON like:
     * <pre>{@code
     *   {
     *     "alg": "string",
     *     "x5c": [
     *       "base64"
     *     ]
     *   }
     * }</pre>
     */
    /**
     * The algorithm used for the public credential
     */
    public PublicKeyCredential alg;

    /**
     * The list of X509 certificates encoded as base64url.
     */
    public List<String> x5c = new ArrayList<>();

    public String fmt;

    public WebAuthnCredential() {
    }

    public WebAuthnCredential(Authenticator authenticator) {
        aaguid = authenticator.getAaguid();
        if (authenticator.getAttestationCertificates() != null)
            alg = authenticator.getAttestationCertificates().getAlg();
        counter = authenticator.getCounter();
        credID = authenticator.getCredID();
        fmt = authenticator.getFmt();
        publicKey = authenticator.getPublicKey();
        type = authenticator.getType();
        userName = authenticator.getUserName();

        if (authenticator.getAttestationCertificates() != null
                && authenticator.getAttestationCertificates().getX5c() != null) {
            this.x5c.addAll(authenticator.getAttestationCertificates().getX5c());
        }
//        this.user = user;
//        user.webAuthnCredential = this;
    }

//    public static Uni<List<WebAuthnCredential>> findByUserName(String userName) {
//        return list("userName", userName);
//    }
//
//    public static Uni<List<WebAuthnCredential>> findByCredID(String credID) {
//        return list("credID", credID);
//    }
//
//    public <T> Uni<T> fetch(T association) {
//        return getSession().flatMap(session -> session.fetch(association));
//    }
}