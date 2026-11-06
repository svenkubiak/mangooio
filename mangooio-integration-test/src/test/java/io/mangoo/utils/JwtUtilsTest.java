package io.mangoo.utils;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObjectType;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWEEncrypter;
import com.nimbusds.jose.JWEHeader;
import com.nimbusds.jose.JWEObject;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.KeyLengthException;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.AESEncrypter;
import com.nimbusds.jose.crypto.DirectEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.mangoo.constants.Default;
import io.mangoo.exceptions.MangooJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
class JwtUtilsTest {
    private static final byte[] SECRET = CommonUtils.randomString(64).getBytes(StandardCharsets.UTF_8);
    private static final byte[] KEY = CommonUtils.randomString(64).getBytes(StandardCharsets.UTF_8);
    private static final String ISSUER = "test-issuer";
    private static final String AUDIENCE = "test-audience";
    private static final String SUBJECT = "test-subject";
    private static final long TTL_SECONDS = 3600L; // 1 hour

    private JwtUtils.JwtData validJwtData;

    @BeforeEach
    void setUp() {
        validJwtData = JwtUtils.JwtData.create()
                .withSecret(SECRET)
                .withKey(KEY)
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withSubject(SUBJECT)
                .withTtlSeconds(TTL_SECONDS);
    }

    @Test
    void testCreateJwtWithNullSubject() {
        //given
        JwtUtils.JwtData jwtData = validJwtData.withSubject(null);

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JwtUtils.createJwt(jwtData));
        assertThat(exception.getMessage(), containsString("subject can not be null"));
    }

    @Test
    void testCreateJwtWithNullJwtData() {
        //given
        JwtUtils.JwtData jwtData = null;

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> JwtUtils.createJwt(jwtData));
        assertThat(exception.getMessage(), containsString("jwtData can not be null"));
    }

    @Test
    void testCreateJwtWithNullSecret() {
        //given
        JwtUtils.JwtData jwtData = validJwtData.withSecret(null);

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> JwtUtils.createJwt(jwtData));
        assertThat(exception.getMessage(), containsString("secret can not be null"));
    }

    @Test
    void testCreateJwtWithNullKey() {
        //given
        JwtUtils.JwtData jwtData = validJwtData.withKey(null);

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> JwtUtils.createJwt(jwtData));
        assertThat(exception.getMessage(), containsString("key can not be null"));
    }

    @Test
    void testCreateJwtWithNullIssuer() {
        //given
        JwtUtils.JwtData jwtData = validJwtData.withIssuer(null);

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JwtUtils.createJwt(jwtData));
        assertThat(exception.getMessage(), containsString("issuer can not be null"));
    }

    @Test
    void testCreateJwtWithNullAudience() {
        //given
        JwtUtils.JwtData jwtData = validJwtData.withAudience(null);

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JwtUtils.createJwt(jwtData));
        assertThat(exception.getMessage(), containsString("audience can not be null"));
    }

    @Test
    void testCreateJwtWithZeroTtl() {
        //given
        JwtUtils.JwtData jwtData = validJwtData.withTtlSeconds(0L);

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JwtUtils.createJwt(jwtData));
        assertThat(exception.getMessage(), containsString("TTL must be greater than 0"));
    }

    @Test
    void testCreateJwtWithNegativeTtl() {
        //given
        JwtUtils.JwtData jwtData = validJwtData.withTtlSeconds(-1L);

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JwtUtils.createJwt(jwtData));
        assertThat(exception.getMessage(), containsString("TTL must be greater than 0"));
    }

    @Test
    void testCreateJwtWithReservedClaimKey() {
        //given
        Map<String, String> customClaims = Map.of(
                "iss", "invalid-issuer", // reserved claim
                "role", "admin"
        );
        JwtUtils.JwtData jwtData = validJwtData.withClaims(customClaims);

        //when & then
        MangooJwtException exception = assertThrows(MangooJwtException.class,
                () -> JwtUtils.createJwt(jwtData));
        assertThat(exception.getMessage(), containsString("Extra claim 'iss' conflicts with a reserved claim"));
    }

    @Test
    void testParseJwtWithValidData() throws MangooJwtException {
        //given
        String jwt = JwtUtils.createJwt(validJwtData);

        //when
        JWTClaimsSet claims = JwtUtils.parseJwt(jwt, validJwtData);

        //then
        assertThat(claims, not(nullValue()));
        assertThat(claims.getSubject(), equalTo(SUBJECT));
        assertThat(claims.getIssuer(), equalTo(ISSUER));
        assertThat(claims.getAudience(), hasItem(AUDIENCE));
        assertThat(claims.getExpirationTime(), not(nullValue()));
        assertThat(claims.getIssueTime(), not(nullValue()));
        assertThat(claims.getNotBeforeTime(), not(nullValue()));
        assertThat(claims.getJWTID(), not(nullValue()));
    }

    @Test
    void testParseJwtWithCustomClaims() throws MangooJwtException {
        //given
        Map<String, String> customClaims = Map.of(
                "role", "admin",
                "department", "IT"
        );
        JwtUtils.JwtData jwtData = validJwtData.withClaims(customClaims);
        String jwt = JwtUtils.createJwt(jwtData);

        //when
        JWTClaimsSet claims = JwtUtils.parseJwt(jwt, jwtData);

        //then
        assertThat(claims, not(nullValue()));
        assertThat(claims.getClaim("role"), equalTo("admin"));
        assertThat(claims.getClaim("department"), equalTo("IT"));
    }

    @Test
    void testParseJwtWithNullJwt() {
        //given
        String jwt = null;

        //when & then
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> JwtUtils.parseJwt(jwt, validJwtData));
        assertThat(exception.getMessage(), containsString("jwt can not be null"));
    }

    @Test
    void testParseJwtWithNullJwtData() {
        //given
        String jwt = "invalid.jwt.token";

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> JwtUtils.parseJwt(jwt, null));
        assertThat(exception.getMessage(), containsString("jwtData can not be null"));
    }

    @Test
    void testParseJwtWithInvalidFormat() {
        //given
        String jwt = "invalid.jwt.token";

        //when & then
        assertThrows(MangooJwtException.class,
                () -> JwtUtils.parseJwt(jwt, validJwtData));
    }

    @Test
    void testParseJwtWithEmptyString() {
        //given
        String jwt = "";

        //when & then
        assertThrows(IllegalArgumentException.class,
                () -> JwtUtils.parseJwt(jwt, validJwtData));
    }

    @Test
    void testParseJwtWithWrongSecret() throws MangooJwtException {
        //given
        String jwt = JwtUtils.createJwt(validJwtData);
        JwtUtils.JwtData wrongJwtData = validJwtData.withSecret(CommonUtils.randomString(64).getBytes(StandardCharsets.UTF_8));

        //when
        MangooJwtException exception = assertThrows(MangooJwtException.class,
                () -> JwtUtils.parseJwt(jwt, wrongJwtData));

        //then
        assertThat(exception.getCause(), not(instanceOf(KeyLengthException.class)));
    }

    @Test
    void testParseJwtWithWrongKey() throws MangooJwtException {
        //given
        String jwt = JwtUtils.createJwt(validJwtData);
        JwtUtils.JwtData wrongJwtData = validJwtData.withKey(CommonUtils.randomString(64).getBytes(StandardCharsets.UTF_8));

        //then
        assertRejected(jwt, wrongJwtData, "JWT signature verification failed");
    }

    @Test
    void testParseJwtWithWrongIssuer() throws MangooJwtException {
        //given
        String jwt = JwtUtils.createJwt(validJwtData);

        //then
        assertRejected(jwt, validJwtData.withIssuer("wrong-issuer"), "Issuer mismatch");
    }

    @Test
    void testParseJwtWithWrongAudience() throws MangooJwtException {
        //given
        String jwt = JwtUtils.createJwt(validJwtData);

        //then
        assertRejected(jwt, validJwtData.withAudience("wrong-audience"), "Audience mismatch");
    }

    @Test
    void testParseForgedJwtWithValidClaims() throws Exception {
        //given
        Instant now = Instant.now();
        String jwt = forge(claims(now, now, now.plusSeconds(60)));

        //when
        JWTClaimsSet claims = JwtUtils.parseJwt(jwt, validJwtData);

        //then
        assertThat(claims.getSubject(), equalTo(SUBJECT));
    }

    @Test
    void testParseJwtWithExpiredToken() throws Exception {
        //given
        Instant now = Instant.now();
        String jwt = forge(claims(now.minusSeconds(600), now.minusSeconds(600), now.minusSeconds(Default.JWT_CLOCK_SKEW + 10)));

        //then
        assertRejected(jwt, validJwtData, "Token expired");
    }

    @Test
    void testParseJwtExpiredWithinClockSkew() throws Exception {
        //given
        Instant now = Instant.now();
        String jwt = forge(claims(now.minusSeconds(600), now.minusSeconds(600), now.minusSeconds(Default.JWT_CLOCK_SKEW - 10)));

        //when
        JWTClaimsSet claims = JwtUtils.parseJwt(jwt, validJwtData);

        //then
        assertThat(claims.getSubject(), equalTo(SUBJECT));
    }

    @Test
    void testParseJwtNotYetValid() throws Exception {
        //given
        Instant now = Instant.now();
        String jwt = forge(claims(now, now.plusSeconds(Default.JWT_CLOCK_SKEW + 60), now.plusSeconds(600)));

        //then
        assertRejected(jwt, validJwtData, "Token not yet valid");
    }

    @Test
    void testParseJwtIssuedInTheFuture() throws Exception {
        //given
        Instant now = Instant.now();
        String jwt = forge(claims(now.plusSeconds(Default.JWT_CLOCK_SKEW + 60), now, now.plusSeconds(600)));

        //then
        assertRejected(jwt, validJwtData, "Token issued in the future");
    }

    @Test
    void testParseJwtWithLifetimeAboveTtl() throws Exception {
        //given
        Instant now = Instant.now();
        String jwt = forge(claims(now, now, now.plusSeconds(TTL_SECONDS + 1)));

        //then
        assertRejected(jwt, validJwtData, "Token lifetime exceeds limit");
    }

    @Test
    void testParseJwtWithoutExpirationTime() throws Exception {
        //given
        Instant now = Instant.now();
        String jwt = forge(claims(now, now, null));

        //then
        assertRejected(jwt, validJwtData, "Missing claim: exp");
    }

    @Test
    void testParseJwtWithoutIssueTime() throws Exception {
        //given
        Instant now = Instant.now();
        String jwt = forge(claims(null, now, now.plusSeconds(60)));

        //then
        assertRejected(jwt, validJwtData, "Missing claim: iat");
    }

    @Test
    void testParseJwtWithoutNotBeforeTime() throws Exception {
        //given
        Instant now = Instant.now();
        String jwt = forge(claims(now, null, now.plusSeconds(60)));

        //then
        assertRejected(jwt, validJwtData, "Missing claim: nbf");
    }

    @Test
    void testParseJwtWithUnexpectedSignatureAlgorithm() throws Exception {
        //given
        Instant now = Instant.now();
        var signedJwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS256).type(JOSEObjectType.JWT).build(), claims(now, now, now.plusSeconds(60)));
        signedJwt.sign(new MACSigner(KEY));
        String jwt = encrypt(signedJwt.serialize(), JWEAlgorithm.DIR, EncryptionMethod.A256CBC_HS512, new DirectEncrypter(SECRET));

        //then
        assertRejected(jwt, validJwtData, "Unexpected JWS algorithm");
    }

    @Test
    void testParseJwtWithUnexpectedEncryptionMethod() throws Exception {
        //given
        Instant now = Instant.now();
        String jwt = encrypt(sign(claims(now, now, now.plusSeconds(60))), JWEAlgorithm.DIR, EncryptionMethod.A256GCM,
                new DirectEncrypter(Arrays.copyOf(SECRET, 32)));

        //then
        assertRejected(jwt, validJwtData, "Unexpected JWE encryption method");
    }

    @Test
    void testParseJwtWithUnexpectedKeyManagementAlgorithm() throws Exception {
        //given
        Instant now = Instant.now();
        String jwt = encrypt(sign(claims(now, now, now.plusSeconds(60))), JWEAlgorithm.A256KW, EncryptionMethod.A256CBC_HS512,
                new AESEncrypter(Arrays.copyOf(SECRET, 32)));

        //then
        assertRejected(jwt, validJwtData, "Unexpected JWE algorithm");
    }

    @Test
    void testExtractCustomClaims() throws MangooJwtException {
        //given
        Map<String, String> customClaims = Map.of(
                "role", "admin",
                "department", "IT",
                "level", "senior"
        );
        JwtUtils.JwtData jwtData = validJwtData.withClaims(customClaims);
        String jwt = JwtUtils.createJwt(jwtData);
        JWTClaimsSet allClaims = JwtUtils.parseJwt(jwt, jwtData);

        //when
        JWTClaimsSet customClaimsResult = JwtUtils.extractCustomClaims(allClaims);

        //then
        assertThat(customClaimsResult, not(nullValue()));
        assertThat(customClaimsResult.getClaim("role"), equalTo("admin"));
        assertThat(customClaimsResult.getClaim("department"), equalTo("IT"));
        assertThat(customClaimsResult.getClaim("level"), equalTo("senior"));
        assertThat(customClaimsResult.getIssuer(), nullValue());
        assertThat(customClaimsResult.getSubject(), nullValue());
        assertThat(customClaimsResult.getAudience(), equalTo(new ArrayList<>()));
    }

    @Test
    void testExtractCustomClaimsWithNoCustomClaims() throws MangooJwtException {
        //given
        String jwt = JwtUtils.createJwt(validJwtData);
        JWTClaimsSet allClaims = JwtUtils.parseJwt(jwt, validJwtData);

        //when
        JWTClaimsSet customClaimsResult = JwtUtils.extractCustomClaims(allClaims);

        //then
        assertThat(customClaimsResult, not(nullValue()));
        assertThat(customClaimsResult.getClaims().size(), equalTo(0));
    }

    @Test
    void testJwtDataEquality() {
        //given
        JwtUtils.JwtData jwtData1 = copyOfValidJwtData();
        JwtUtils.JwtData jwtData2 = copyOfValidJwtData();
        JwtUtils.JwtData jwtData3 = jwtData1.withSubject("different-subject");
        JwtUtils.JwtData jwtData4 = jwtData1.withSecret(CommonUtils.randomString(64).getBytes(StandardCharsets.UTF_8));

        //when & then
        assertThat(jwtData1.secret(), not(sameInstance(jwtData2.secret())));
        assertThat(jwtData1.equals(jwtData2), equalTo(true));
        assertThat(jwtData1.equals(jwtData3), equalTo(false));
        assertThat(jwtData1.equals(jwtData4), equalTo(false));
        assertThat(jwtData1.equals(null), equalTo(false));
        assertThat(jwtData1.equals("not-a-jwtdata"), equalTo(false));
    }

    @Test
    void testJwtDataHashCode() {
        //given
        JwtUtils.JwtData jwtData1 = copyOfValidJwtData();
        JwtUtils.JwtData jwtData2 = copyOfValidJwtData();
        JwtUtils.JwtData jwtData3 = jwtData1.withSubject("different-subject");

        //when & then
        assertThat(jwtData1.key(), not(sameInstance(jwtData2.key())));
        assertThat(jwtData1.hashCode(), equalTo(jwtData2.hashCode()));
        assertThat(jwtData1.hashCode(), not(equalTo(jwtData3.hashCode())));
    }

    @Test
    void testJwtDataToString() {
        //when
        String toString = validJwtData.toString();

        //then
        assertThat(toString, not(nullValue()));
        assertThat(toString, containsString("JwtData["));
        assertThat(toString, containsString("secret=***hidden***"));
        assertThat(toString, containsString("key=***hidden***"));
        assertThat(toString, containsString("issuer=" + ISSUER));
        assertThat(toString, containsString("audience=" + AUDIENCE));
        assertThat(toString, containsString("subject=" + SUBJECT));
        assertThat(toString, containsString("ttlSeconds=" + TTL_SECONDS));
    }

    // Same content as validJwtData, but with distinct byte[] instances
    private static JwtUtils.JwtData copyOfValidJwtData() {
        return JwtUtils.JwtData.create()
                .withSecret(SECRET.clone())
                .withKey(KEY.clone())
                .withIssuer(ISSUER)
                .withAudience(AUDIENCE)
                .withSubject(SUBJECT)
                .withTtlSeconds(TTL_SECONDS);
    }

    private static void assertRejected(String jwt, JwtUtils.JwtData jwtData, String reason) {
        MangooJwtException exception = assertThrows(MangooJwtException.class, () -> JwtUtils.parseJwt(jwt, jwtData));
        assertThat(exception.getCause().getMessage(), containsString(reason));
    }

    private static JWTClaimsSet claims(Instant issueTime, Instant notBeforeTime, Instant expirationTime) {
        return new JWTClaimsSet.Builder()
                .issuer(ISSUER)
                .audience(AUDIENCE)
                .subject(SUBJECT)
                .issueTime(issueTime == null ? null : Date.from(issueTime))
                .notBeforeTime(notBeforeTime == null ? null : Date.from(notBeforeTime))
                .expirationTime(expirationTime == null ? null : Date.from(expirationTime))
                .jwtID(CommonUtils.randomString(32))
                .build();
    }

    private static String forge(JWTClaimsSet claims) throws JOSEException {
        return encrypt(sign(claims), JWEAlgorithm.DIR, EncryptionMethod.A256CBC_HS512, new DirectEncrypter(SECRET));
    }

    private static String sign(JWTClaimsSet claims) throws JOSEException {
        var signedJwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.HS512).type(JOSEObjectType.JWT).build(), claims);
        signedJwt.sign(new MACSigner(KEY));
        return signedJwt.serialize();
    }

    private static String encrypt(String payload, JWEAlgorithm algorithm, EncryptionMethod method, JWEEncrypter encrypter) throws JOSEException {
        var jweObject = new JWEObject(new JWEHeader.Builder(algorithm, method).contentType("JWT").build(), new Payload(payload));
        jweObject.encrypt(encrypter);
        return jweObject.serialize();
    }
}
