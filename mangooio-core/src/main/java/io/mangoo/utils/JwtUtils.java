package io.mangoo.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.PasswordBasedDecrypter;
import com.nimbusds.jose.crypto.PasswordBasedEncrypter;
import com.nimbusds.jwt.EncryptedJWT;
import com.nimbusds.jwt.JWTClaimsSet;
import io.mangoo.constants.NotNull;
import io.mangoo.exceptions.MangooJwtExeption;
import org.apache.fury.util.Preconditions;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.*;

public final class JwtUtils {
    private static final int SALT_LENGTH = 32;
    private static final int ITERATIONS = 200_000;
    private static final Set<String> RESERVED = Set.of("iss","aud","sub","iat","nbf","exp","jti");

    private JwtUtils() {}

    public static String createJwt(String secret,
                               String issuer,
                               String audience,
                               String subject,
                               long ttlSeconds,
                               Map<String, String> claims) throws MangooJwtExeption {
        Objects.requireNonNull(secret, NotNull.SECRET);
        Objects.requireNonNull(issuer, NotNull.ISSUER);
        Objects.requireNonNull(audience, NotNull.AUDIENCE);
        Objects.requireNonNull(subject, NotNull.SUBJECT);
        Preconditions.checkArgument(ttlSeconds > 0, "TTL must be greater than 0.");

        try {
            Instant now = Instant.now();
            JWTClaimsSet.Builder jwtClaimsSetBuilder = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .audience(audience)
                    .subject(subject)
                    .issueTime(Date.from(now))
                    .notBeforeTime(Date.from(now.minusSeconds(30)))
                    .expirationTime(Date.from(now.plusSeconds(ttlSeconds)))
                    .jwtID(CodecUtils.uuidV6());

            if (claims != null && !claims.isEmpty()) {
                for (Map.Entry<String, String> e : claims.entrySet()) {
                    String key = Objects.requireNonNull(e.getKey(), "extra claim key must not be null");
                    if (RESERVED.contains(key)) {
                        throw new IllegalArgumentException("Extra claim '" + key + "' conflicts with a reserved claim");
                    }
                    jwtClaimsSetBuilder.claim(key, e.getValue());
                }
            }

            JWTClaimsSet jwtClaimsSet = jwtClaimsSetBuilder.build();

            JWEHeader jweHeader = new JWEHeader.Builder(
                    JWEAlgorithm.PBES2_HS512_A256KW,
                    EncryptionMethod.A256GCM)
                    .type(JOSEObjectType.JWT)
                    .build();

            PasswordBasedEncrypter encrypter = new PasswordBasedEncrypter(secret, SALT_LENGTH, ITERATIONS);
            encrypter.getJCAContext().setSecureRandom(new SecureRandom());

            EncryptedJWT jwe = new EncryptedJWT(jweHeader, jwtClaimsSet);
            jwe.encrypt(encrypter);

            return jwe.serialize();
        } catch (Exception e) {
            throw new MangooJwtExeption(e);
        }
    }

    public static JWTClaimsSet parseJwt(
            String jwt,
            String secret,
            String issuer,
            String audience,
            long ttlSeconds) throws MangooJwtExeption {
        Objects.requireNonNull(jwt, NotNull.JWT);
        Objects.requireNonNull(secret, NotNull.SECRET);
        Objects.requireNonNull(issuer, NotNull.ISSUER);
        Objects.requireNonNull(audience, NotNull.AUDIENCE);
        Preconditions.checkArgument(ttlSeconds > 0, "TTL must be greater than 0.");

        try {
            EncryptedJWT jwe = EncryptedJWT.parse(jwt);

            JWEHeader h = jwe.getHeader();
            if (!JWEAlgorithm.PBES2_HS512_A256KW.equals(h.getAlgorithm())) {
                throw new JOSEException("Unexpected JWE alg: " + h.getAlgorithm());
            }
            if (!EncryptionMethod.A256GCM.equals(h.getEncryptionMethod())) {
                throw new JOSEException("Unexpected JWE enc: " + h.getEncryptionMethod());
            }

            PasswordBasedDecrypter decrypter = new PasswordBasedDecrypter(secret);
            jwe.decrypt(decrypter);

            JWTClaimsSet claims = jwe.getJWTClaimsSet();
            Instant now = Instant.now();

            Date exp = require(claims.getExpirationTime(), "exp");
            Date iat = require(claims.getIssueTime(),      "iat");
            Date nbf = require(claims.getNotBeforeTime(),  "nbf");

            if (!Objects.equals(issuer, claims.getIssuer())) {
                throw new JOSEException("Issuer mismatch");
            }

            List<String> aud = claims.getAudience();
            if (aud == null || aud.stream().noneMatch(audience::equals)) {
                throw new JOSEException("Audience mismatch");
            }

            long skew = Math.max(0, 30);
            if (exp.toInstant().isBefore(now.minusSeconds(skew))) {
                throw new JOSEException("Token expired");
            }

            if (nbf.toInstant().isAfter(now.plusSeconds(skew))) {
                throw new JOSEException("Token not yet valid");
            }

            if (iat.toInstant().isAfter(now.plusSeconds(skew))) {
                throw new JOSEException("Token issued in the future");
            }

            long lifetime = (exp.getTime() - iat.getTime()) / 1000L;
            if (lifetime > ttlSeconds) {
                throw new JOSEException("Token lifetime exceeds limit");
            }

            return claims;
        } catch (Exception e) {
            throw new MangooJwtExeption(e);
        }
    }

    private static <T> T require(T v, String name) throws JOSEException {
        if (v == null) throw new JOSEException("Missing '" + name + "' claim");
        return v;
    }
}
