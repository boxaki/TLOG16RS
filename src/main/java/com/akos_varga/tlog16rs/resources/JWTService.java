package com.akos_varga.tlog16rs.resources;

import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.lang.JoseException;
// import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author Akos Varga
 */
// @Slf4j
public class JWTService {

    RsaJsonWebKey rsaJsonWebKey;

    public JWTService() throws JoseException {
        rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        rsaJsonWebKey.setKeyId("k1");

    }

    public String createJWT(String name) throws JoseException {

        JwtClaims claims = new JwtClaims();
        claims.setExpirationTimeMinutesInTheFuture(5);
        claims.setClaim("user", name);

        JsonWebSignature jws = new JsonWebSignature();
        jws.setPayload(claims.toJson());
        jws.setKey(rsaJsonWebKey.getPrivateKey());
        jws.setKeyIdHeaderValue(rsaJsonWebKey.getKeyId());
        jws.setAlgorithmHeaderValue(AlgorithmIdentifiers.RSA_USING_SHA256);
        String jwt = jws.getCompactSerialization();

        return jwt;
    }

    public String getUsernameIfValidJwt(String jwt) throws InvalidJwtException { //getUsernameifvalidjwt
        JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                .setRequireExpirationTime()
                .setVerificationKey(rsaJsonWebKey.getKey())
                .setJwsAlgorithmConstraints(
                        new AlgorithmConstraints(AlgorithmConstraints.ConstraintType.WHITELIST,
                                AlgorithmIdentifiers.RSA_USING_SHA256))
                .build();

        JwtClaims claim = jwtConsumer.processToClaims(jwt);

        return (String) claim.getClaimValue("user");

    }

}
