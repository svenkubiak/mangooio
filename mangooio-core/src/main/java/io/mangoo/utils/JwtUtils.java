package io.mangoo.utils;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.crypto.PasswordBasedDecrypter;
import com.nimbusds.jose.crypto.PasswordBasedEncrypter;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.mangoo.constants.NotNull;
import io.mangoo.exceptions.MangooJwtException;
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
        Objects.requireNonNull(jwtData.subject(), NotNull.SUBJECT);

        try {
            var now = Instant.now();
            var claimsBuilder = new JWTClaimsSet.Builder()
                    .issuer(jwtData.issuer())
                    .audience(jwtData.audience())
                    .subject(jwtData.subject())
                    .issueTime(Date.from(now))
                    .notBeforeTime(Date.from(now.minusSeconds(30)))
                    .expirationTime(Date.from(now.plusSeconds(jwtData.ttlSeconds())))
                    .jwtID(CommonUtils.uuidV6());

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

            // Step 1: Sign JWT
            var jwsHeader = new JWSHeader.Builder(JWSAlgorithm.HS512)
                    .type(JOSEObjectType.JWT)
                    .build();

            var signedJWT = new SignedJWT(jwsHeader, claimsSet);
            var signer = new MACSigner(jwtData.secret());
            signedJWT.sign(signer);

            // Step 2: Encrypt the signed JWT compact serialization string (nested JWT)
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
        Objects.requireNonNull(jwt, NotNull.JWT);
        validate(jwtData);

        try {
            // Step 1: Parse the encrypted JWT (JWE)
            var jweObject = JWEObject.parse(jwt);

            var jweHeader = jweObject.getHeader();
            if (!JWEAlgorithm.PBES2_HS512_A256KW.equals(jweHeader.getAlgorithm())) {
                throw new JOSEException("Unexpected JWE algorithm: " + jweHeader.getAlgorithm());
            }
            if (!EncryptionMethod.A256GCM.equals(jweHeader.getEncryptionMethod())) {
                throw new JOSEException("Unexpected JWE encryption method: " + jweHeader.getEncryptionMethod());
            }

            // Decrypt JWE
            var decrypter = new PasswordBasedDecrypter(jwtData.secret());
            jweObject.decrypt(decrypter);

            // Step 2: Extract the nested signed JWT compact serialization string
            var signedJwtString = jweObject.getPayload().toString();

            // Step 3: Parse the signed JWT
            var signedJWT = SignedJWT.parse(signedJwtString);

            var jwsHeader = signedJWT.getHeader();
            if (!JWSAlgorithm.HS512.equals(jwsHeader.getAlgorithm())) {
                throw new JOSEException("Unexpected JWS algorithm: " + jwsHeader.getAlgorithm());
            }

            // Verify signature
            var verifier = new MACVerifier(jwtData.secret());
            if (!signedJWT.verify(verifier)) {
                throw new JOSEException("JWT signature verification failed");
            }

            // Validate claims
            var claims = signedJWT.getJWTClaimsSet();

            var now = Instant.now();
            var exp = Objects.requireNonNull(claims.getExpirationTime(), "exp claim is required");
            var iat = Objects.requireNonNull(claims.getIssueTime(), "iat claim is required");
            var nbf = Objects.requireNonNull(claims.getNotBeforeTime(), "nbf claim is required");

            if (!Objects.equals(jwtData.issuer(), claims.getIssuer())) {
                throw new JOSEException("Issuer mismatch");
            }

            List<String> aud = claims.getAudience();
            if (aud == null || aud.stream().noneMatch(jwtData.audience()::equals)) {
                throw new JOSEException("Audience mismatch");
            }

            long skew = Math.max(0, 30);  // 30 seconds clock skew

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
        Objects.requireNonNull(jwtData, NotNull.JWT_DATA);
        Objects.requireNonNull(jwtData.key(), NotNull.KEY);
        Objects.requireNonNull(jwtData.secret(), NotNull.SECRET);
        Objects.requireNonNull(jwtData.issuer(), NotNull.ISSUER);
        Objects.requireNonNull(jwtData.audience(), NotNull.AUDIENCE);
        Preconditions.checkArgument(jwtData.ttlSeconds() > 0, "TTL must be greater than 0.");
    }
    public static JwtData jwtData() {
        return new JwtData(null, null, null, null, null, 0L, Map.of());
    }

    public record JwtData(
            byte[] secret,
            String key,
            String issuer,
            String audience,
            String subject,
            long ttlSeconds,
            Map<String, String> claims
        ) {

        public static JwtData create() {
            return new JwtData(null, null, null, null, null, 0L, Map.of());
        }

        public JwtData withSecret(byte[] secret) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims);
        }

        public JwtData withKey(String key) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims);
        }

        public JwtData withIssuer(String issuer) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims);
        }

        public JwtData withAudience(String audience) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims);
        }

        public JwtData withSubject(String subject) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims);
        }

        public JwtData withTtlSeconds(long ttlSeconds) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims);
        }

        public JwtData withClaims(Map<String, String> claims) {
            return new JwtData(secret, key, issuer, audience, subject, ttlSeconds, claims);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof JwtData other)) return false;
            return ttlSeconds == other.ttlSeconds
                    && Arrays.equals(secret, other.secret)
                    && Objects.equals(key, other.key)
                    && Objects.equals(issuer, other.issuer)
                    && Objects.equals(audience, other.audience)
                    && Objects.equals(subject, other.subject)
                    && Objects.equals(claims, other.claims);
        }

        @Override
        public int hashCode() {
            int result = Objects.hash(key, issuer, audience, subject, ttlSeconds, claims);
            result = 31 * result + Arrays.hashCode(secret);
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
                    ']';
        }
    }
}
