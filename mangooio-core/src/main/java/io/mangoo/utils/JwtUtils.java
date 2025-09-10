package io.mangoo.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.PasswordBasedDecrypter;
import com.nimbusds.jose.crypto.PasswordBasedEncrypter;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.mangoo.constants.Required;
import io.mangoo.exceptions.MangooJwtException;
import org.apache.commons.lang3.StringUtils;
import org.apache.fury.util.Preconditions;

import java.security.SecureRandom;
import java.text.ParseException;
import java.time.Instant;
import java.util.*;

public final class JwtUtils {
    private static final String JWT = "JWT";
    private static final int SALT_LENGTH = 32;
    private static final int ITERATIONS = 100_00;
    private static final Set<String> RESERVED = Set.of("iss", "aud", "sub", "iat", "nbf", "exp", "jti");

    private JwtUtils() {
    }

    public static String createJwt(JwtData jwtData) throws MangooJwtException {
        validate(jwtData);
        Arguments.requireNonBlank(jwtData.subject(), Required.SUBJECT);

        try {
            var now = Instant.now();
            var claimsBuilder = new JWTClaimsSet.Builder()
                    .issuer(jwtData.issuer())
                    .audience(jwtData.audience())
                    .subject(jwtData.subject())
                    .issueTime(Date.from(now))
                    .notBeforeTime(Date.from(now.minusSeconds(30)))
                    .expirationTime(Date.from(now.plusSeconds(jwtData.ttlSeconds())))
                    .jwtID(StringUtils.isNotBlank(jwtData.jwtID()) ? jwtData.jwtID() : CommonUtils.randomString(32));

            if (jwtData.claims() != null && !jwtData.claims().isEmpty()) {
                for (Map.Entry<String, String> entry : jwtData.claims().entrySet()) {
                    String key = Objects.requireNonNull(entry.getKey(), "extra claim key must not be null");
                    if (RESERVED.contains(key)) {
                        throw new MangooJwtException("Extra claim '" + key + "' conflicts with a reserved claim");
                    }
                    claimsBuilder.claim(key, entry.getValue());
                }
            }

            JWTClaimsSet claimsSet = claimsBuilder.build();

            // Step 1: Sign JWT using KEY
            var jwsHeader = new JWSHeader.Builder(JWSAlgorithm.HS512)
                    .type(JOSEObjectType.JWT)
                    .build();

            var signedJWT = new SignedJWT(jwsHeader, claimsSet);
            var signer = new MACSigner(jwtData.key());
            signedJWT.sign(signer);

            // Step 2: Encrypt with SECRET
            var jweHeader = new JWEHeader.Builder(
                    JWEAlgorithm.PBES2_HS512_A256KW,
                    EncryptionMethod.A256GCM)
                    .compressionAlgorithm(CompressionAlgorithm.DEF)
                    .contentType(JWT)
                    .build();

            var jweObject = new JWEObject(jweHeader, new Payload(signedJWT.serialize()));

            var encrypter = new PasswordBasedEncrypter(jwtData.secret(), SALT_LENGTH, ITERATIONS);
            encrypter.getJCAContext().setSecureRandom(new SecureRandom());
            jweObject.encrypt(encrypter);

            return jweObject.serialize();
        } catch (JOSEException e) {
            throw new MangooJwtException(e);
        }
    }

    public static JWTClaimsSet parseJwt(String jwt, JwtData jwtData) throws MangooJwtException {
        Arguments.requireNonBlank(jwt, Required.JWT);
        validate(jwtData);

        try {
            // Step 1: Parse encrypted JWE
            var jweObject = JWEObject.parse(jwt);

            if (!JWEAlgorithm.PBES2_HS512_A256KW.equals(jweObject.getHeader().getAlgorithm())) {
                throw new JOSEException("Unexpected JWE algorithm: " + jweObject.getHeader().getAlgorithm());
            }
            if (!EncryptionMethod.A256GCM.equals(jweObject.getHeader().getEncryptionMethod())) {
                throw new JOSEException("Unexpected JWE encryption method: " + jweObject.getHeader().getEncryptionMethod());
            }

            // Step 2: Decrypt using SECRET
            var decrypter = new PasswordBasedDecrypter(jwtData.secret());
            jweObject.decrypt(decrypter);

            // Step 3: Extract and parse Signed JWT
            var signedJWT = SignedJWT.parse(jweObject.getPayload().toString());

            if (!JWSAlgorithm.HS512.equals(signedJWT.getHeader().getAlgorithm())) {
                throw new JOSEException("Unexpected JWS algorithm: " + signedJWT.getHeader().getAlgorithm());
            }

            // Step 4: Verify signature using KEY
            var verifier = new MACVerifier(jwtData.key());
            if (!signedJWT.verify(verifier)) {
                throw new JOSEException("JWT signature verification failed");
            }

            // Validate claims
            var claims = signedJWT.getJWTClaimsSet();
            var now = Instant.now();

            var exp = Objects.requireNonNull(claims.getExpirationTime(), "exp is required");
            var iat = Objects.requireNonNull(claims.getIssueTime(), "iat is required");
            var nbf = Objects.requireNonNull(claims.getNotBeforeTime(), "nbf is required");

            if (!Objects.equals(jwtData.issuer(), claims.getIssuer())) {
                throw new JOSEException("Issuer mismatch");
            }

            List<String> aud = claims.getAudience();
            if (aud == null || aud.stream().noneMatch(jwtData.audience()::equals)) {
                throw new JOSEException("Audience mismatch");
            }

            long skew = 30; // allowable clock skew

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
            if (lifetime > jwtData.ttlSeconds()) {
                throw new JOSEException("Token lifetime exceeds limit");
            }

            return claims;
        } catch (JOSEException | ParseException e) {
            throw new MangooJwtException(e);
        }
    }

    public static String extractSubject(String jwt, byte[] secret) throws MangooJwtException {
        Arguments.requireNonBlank(jwt, Required.JWT);
        Objects.requireNonNull(secret, Required.SECRET);
        Preconditions.checkArgument(secret.length > 0, Required.SECRET);

        try {
            // Step 1: Parse encrypted JWE
            var jweObject = JWEObject.parse(jwt);

            if (!JWEAlgorithm.PBES2_HS512_A256KW.equals(jweObject.getHeader().getAlgorithm())) {
                throw new JOSEException("Unexpected JWE algorithm: " + jweObject.getHeader().getAlgorithm());
            }
            if (!EncryptionMethod.A256GCM.equals(jweObject.getHeader().getEncryptionMethod())) {
                throw new JOSEException("Unexpected JWE encryption method: " + jweObject.getHeader().getEncryptionMethod());
            }

            // Step 2: Decrypt using SECRET (necessary to access payload)
            var decrypter = new PasswordBasedDecrypter(secret);
            jweObject.decrypt(decrypter);

            // Step 3: Extract and parse Signed JWT without verifying signature
            var signedJWT = SignedJWT.parse(jweObject.getPayload().toString());

            // Step 4: Extract claims WITHOUT signature verification
            JWTClaimsSet claims = signedJWT.getJWTClaimsSet();

            return claims.getSubject();
        } catch (Exception e) {
            throw new MangooJwtException(e);
        }
    }

    public static JWTClaimsSet extractCustomClaims(JWTClaimsSet claims) {
        var builder = new JWTClaimsSet.Builder();
        claims.getClaims().forEach((key, value) -> {
            if (!RESERVED.contains(key)) {
                builder.claim(key, value);
            }
        });
        return builder.build();
    }

    private static void validate(JwtData jwtData) {
        Objects.requireNonNull(jwtData, Required.JWT_DATA);
        Objects.requireNonNull(jwtData.secret(), Required.SECRET); // encryption secret must exist
        Objects.requireNonNull(jwtData.key(), Required.KEY);       // signing key must exist
        Arguments.requireNonBlank(jwtData.issuer(), Required.ISSUER);
        Arguments.requireNonBlank(jwtData.audience(), Required.AUDIENCE);
        Preconditions.checkArgument(jwtData.ttlSeconds() > 0, "TTL must be greater than 0.");
    }

    public static JwtData jwtData() {
        return new JwtData(null, null, null, null, null, 0L, Map.of(), null);
    }

    public record JwtData(
            byte[] secret,        // encryption/decryption
            byte[] key,           // signing/verifying
            String issuer,
            String audience,
            String subject,
            long ttlSeconds,
            Map<String, String> claims,
            String jwtID
    ) {

        public static JwtData create() {
            return new JwtData(null, null, null, null, null, 0L, Map.of(), null);
        }

        public JwtData withSecret(byte[] secret) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims, jwtID);
        }

        public JwtData withKey(byte[] key) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims, jwtID);
        }

        public JwtData withIssuer(String issuer) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims, jwtID);
        }

        public JwtData withAudience(String audience) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims, jwtID);
        }

        public JwtData withSubject(String subject) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims, jwtID);
        }

        public JwtData withTtlSeconds(long ttlSeconds) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims, jwtID);
        }

        public JwtData withClaims(Map<String, String> claims) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims, jwtID);
        }

        public JwtData withJwtID(String jwtID) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims, jwtID);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JwtData other)) return false;
            return ttlSeconds == other.ttlSeconds
                    && Arrays.equals(secret, other.secret)
                    && Arrays.equals(key, other.key)
                    && Objects.equals(issuer, other.issuer)
                    && Objects.equals(audience, other.audience)
                    && Objects.equals(subject, other.subject)
                    && Objects.equals(claims, other.claims)
                    && Objects.equals(jwtID, other.jwtID);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(issuer, audience, subject, ttlSeconds, claims, jwtID);
            result = 31 * result + Arrays.hashCode(secret);
            result = 31 * result + Arrays.hashCode(key);
            return result;
        }

        @Override
        public String toString() {
            return "JwtData[" +
                    "secret=***hidden***" +
                    ", key=***hidden***" +
                    ", issuer=" + issuer +
                    ", audience=" + audience +
                    ", subject=" + subject +
                    ", ttlSeconds=" + ttlSeconds +
                    ", claims=" + claims +
                    ", jwtID=" + jwtID +
                    ']';
        }
    }
}
