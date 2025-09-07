package io.mangoo.utils;

import com.nimbusds.jwt.JWTClaimsSet;
import io.mangoo.exceptions.MangooJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.util.ArrayList;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Execution(ExecutionMode.CONCURRENT)
class JwtUtilsTest {
    private static final byte[] SECRET = CommonUtils.randomString(64).getBytes();
    private static final String KEY = CommonUtils.randomString(64);
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
    void testCreateJwtWithValidData() throws MangooJwtException {
        //given
        JwtUtils.JwtData jwtData = validJwtData;

        //when
        String jwt = JwtUtils.createJwt(jwtData);

        //then
        assertThat(jwt, not(nullValue()));
        assertThat(jwt, not(emptyString()));
        assertThat(jwt, containsString("."));
    }

    @Test
    void testCreateJwtWithCustomClaims() throws MangooJwtException {
        //given
        Map<String, String> customClaims = Map.of(
                "role", "admin",
                "department", "IT"
        );
        JwtUtils.JwtData jwtData = validJwtData.withClaims(customClaims);

        //when
        String jwt = JwtUtils.createJwt(jwtData);

        //then
        assertThat(jwt, not(nullValue()));
        assertThat(jwt, not(emptyString()));
    }

    @Test
    void testCreateJwtWithNullSubject() {
        //given
        JwtUtils.JwtData jwtData = validJwtData.withSubject(null);

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
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
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> JwtUtils.createJwt(jwtData));
        assertThat(exception.getMessage(), containsString("issuer can not be null"));
    }

    @Test
    void testCreateJwtWithNullAudience() {
        //given
        JwtUtils.JwtData jwtData = validJwtData.withAudience(null);

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
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
        NullPointerException exception = assertThrows(NullPointerException.class,
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
        assertThrows(MangooJwtException.class,
                () -> JwtUtils.parseJwt(jwt, validJwtData));
    }

    @Test
    void testParseJwtWithWrongSecret() throws MangooJwtException {
        //given
        String jwt = JwtUtils.createJwt(validJwtData);
        JwtUtils.JwtData wrongJwtData = validJwtData.withSecret("wrong-secret".getBytes());

        //when & then
        assertThrows(MangooJwtException.class,
                () -> JwtUtils.parseJwt(jwt, wrongJwtData));
    }

    @Test
    void testParseJwtWithWrongIssuer() throws MangooJwtException {
        //given
        String jwt = JwtUtils.createJwt(validJwtData);
        JwtUtils.JwtData wrongJwtData = validJwtData.withIssuer("wrong-issuer");

        //when & then
        assertThrows(MangooJwtException.class,
                () -> JwtUtils.parseJwt(jwt, wrongJwtData));
    }

    @Test
    void testParseJwtWithWrongAudience() throws MangooJwtException {
        //given
        String jwt = JwtUtils.createJwt(validJwtData);
        JwtUtils.JwtData wrongJwtData = validJwtData.withAudience("wrong-audience");

        //when & then
        assertThrows(MangooJwtException.class,
                () -> JwtUtils.parseJwt(jwt, wrongJwtData));
    }

    @Test
    void testParseJwtWithExpiredToken() throws MangooJwtException {
        //given
        JwtUtils.JwtData expiredJwtData = validJwtData.withTtlSeconds(1L); // 1 second TTL
        String jwt = JwtUtils.createJwt(expiredJwtData);

        // Wait for token to expire
        try {
            Thread.sleep(32000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        //when & then
        assertThrows(MangooJwtException.class,
                () -> JwtUtils.parseJwt(jwt, expiredJwtData));
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
    void testExtractCustomClaimsWithNullClaims() {
        //given
        JWTClaimsSet claims = null;

        //when & then
        assertThrows(NullPointerException.class,
                () -> JwtUtils.extractCustomClaims(claims));
    }

    @Test
    void testJwtDataCreate() {
        //when
        JwtUtils.JwtData jwtData = JwtUtils.JwtData.create();

        //then
        assertThat(jwtData, not(nullValue()));
        assertThat(jwtData.secret(), nullValue());
        assertThat(jwtData.key(), nullValue());
        assertThat(jwtData.issuer(), nullValue());
        assertThat(jwtData.audience(), nullValue());
        assertThat(jwtData.subject(), nullValue());
        assertThat(jwtData.ttlSeconds(), equalTo(0L));
        assertThat(jwtData.claims(), equalTo(Map.of()));
    }

    @Test
    void testJwtDataJwtDataMethod() {
        //when
        JwtUtils.JwtData jwtData = JwtUtils.jwtData();

        //then
        assertThat(jwtData, not(nullValue()));
        assertThat(jwtData.secret(), nullValue());
        assertThat(jwtData.key(), nullValue());
        assertThat(jwtData.issuer(), nullValue());
        assertThat(jwtData.audience(), nullValue());
        assertThat(jwtData.subject(), nullValue());
        assertThat(jwtData.ttlSeconds(), equalTo(0L));
        assertThat(jwtData.claims(), equalTo(Map.of()));
    }

    @Test
    void testJwtDataWithSecret() {
        //given
        byte[] newSecret = "new-secret".getBytes();

        //when
        JwtUtils.JwtData newJwtData = validJwtData.withSecret(newSecret);

        //then
        assertThat(newJwtData.secret(), equalTo(newSecret));
        assertThat(newJwtData.key(), equalTo(validJwtData.key()));
        assertThat(newJwtData.issuer(), equalTo(validJwtData.issuer()));
        assertThat(newJwtData.audience(), equalTo(validJwtData.audience()));
        assertThat(newJwtData.subject(), equalTo(validJwtData.subject()));
        assertThat(newJwtData.ttlSeconds(), equalTo(validJwtData.ttlSeconds()));
        assertThat(newJwtData.claims(), equalTo(validJwtData.claims()));
    }

    @Test
    void testJwtDataWithKey() {
        //given
        String newKey = "new-key";

        //when
        JwtUtils.JwtData newJwtData = validJwtData.withKey(newKey);

        //then
        assertThat(newJwtData.key(), equalTo(newKey));
        assertThat(newJwtData.secret(), equalTo(validJwtData.secret()));
        assertThat(newJwtData.issuer(), equalTo(validJwtData.issuer()));
        assertThat(newJwtData.audience(), equalTo(validJwtData.audience()));
        assertThat(newJwtData.subject(), equalTo(validJwtData.subject()));
        assertThat(newJwtData.ttlSeconds(), equalTo(validJwtData.ttlSeconds()));
        assertThat(newJwtData.claims(), equalTo(validJwtData.claims()));
    }

    @Test
    void testJwtDataWithIssuer() {
        //given
        String newIssuer = "new-issuer";

        //when
        JwtUtils.JwtData newJwtData = validJwtData.withIssuer(newIssuer);

        //then
        assertThat(newJwtData.issuer(), equalTo(newIssuer));
        assertThat(newJwtData.secret(), equalTo(validJwtData.secret()));
        assertThat(newJwtData.key(), equalTo(validJwtData.key()));
        assertThat(newJwtData.audience(), equalTo(validJwtData.audience()));
        assertThat(newJwtData.subject(), equalTo(validJwtData.subject()));
        assertThat(newJwtData.ttlSeconds(), equalTo(validJwtData.ttlSeconds()));
        assertThat(newJwtData.claims(), equalTo(validJwtData.claims()));
    }

    @Test
    void testJwtDataWithAudience() {
        //given
        String newAudience = "new-audience";

        //when
        JwtUtils.JwtData newJwtData = validJwtData.withAudience(newAudience);

        //then
        assertThat(newJwtData.audience(), equalTo(newAudience));
        assertThat(newJwtData.secret(), equalTo(validJwtData.secret()));
        assertThat(newJwtData.key(), equalTo(validJwtData.key()));
        assertThat(newJwtData.issuer(), equalTo(validJwtData.issuer()));
        assertThat(newJwtData.subject(), equalTo(validJwtData.subject()));
        assertThat(newJwtData.ttlSeconds(), equalTo(validJwtData.ttlSeconds()));
        assertThat(newJwtData.claims(), equalTo(validJwtData.claims()));
    }

    @Test
    void testJwtDataWithSubject() {
        //given
        String newSubject = "new-subject";

        //when
        JwtUtils.JwtData newJwtData = validJwtData.withSubject(newSubject);

        //then
        assertThat(newJwtData.subject(), equalTo(newSubject));
        assertThat(newJwtData.secret(), equalTo(validJwtData.secret()));
        assertThat(newJwtData.key(), equalTo(validJwtData.key()));
        assertThat(newJwtData.issuer(), equalTo(validJwtData.issuer()));
        assertThat(newJwtData.audience(), equalTo(validJwtData.audience()));
        assertThat(newJwtData.ttlSeconds(), equalTo(validJwtData.ttlSeconds()));
        assertThat(newJwtData.claims(), equalTo(validJwtData.claims()));
    }

    @Test
    void testJwtDataWithTtlSeconds() {
        //given
        long newTtlSeconds = 7200L;

        //when
        JwtUtils.JwtData newJwtData = validJwtData.withTtlSeconds(newTtlSeconds);

        //then
        assertThat(newJwtData.ttlSeconds(), equalTo(newTtlSeconds));
        assertThat(newJwtData.secret(), equalTo(validJwtData.secret()));
        assertThat(newJwtData.key(), equalTo(validJwtData.key()));
        assertThat(newJwtData.issuer(), equalTo(validJwtData.issuer()));
        assertThat(newJwtData.audience(), equalTo(validJwtData.audience()));
        assertThat(newJwtData.subject(), equalTo(validJwtData.subject()));
        assertThat(newJwtData.claims(), equalTo(validJwtData.claims()));
    }

    @Test
    void testJwtDataWithClaims() {
        //given
        Map<String, String> newClaims = Map.of("role", "user", "level", "basic");

        //when
        JwtUtils.JwtData newJwtData = validJwtData.withClaims(newClaims);

        //then
        assertThat(newJwtData.claims(), equalTo(newClaims));
        assertThat(newJwtData.secret(), equalTo(validJwtData.secret()));
        assertThat(newJwtData.key(), equalTo(validJwtData.key()));
        assertThat(newJwtData.issuer(), equalTo(validJwtData.issuer()));
        assertThat(newJwtData.audience(), equalTo(validJwtData.audience()));
        assertThat(newJwtData.subject(), equalTo(validJwtData.subject()));
        assertThat(newJwtData.ttlSeconds(), equalTo(validJwtData.ttlSeconds()));
    }

    @Test
    void testJwtDataEquality() {
        //given
        JwtUtils.JwtData jwtData1 = validJwtData;
        JwtUtils.JwtData jwtData2 = validJwtData;
        JwtUtils.JwtData jwtData3 = validJwtData.withSubject("different-subject");

        //when & then
        assertThat(jwtData1.equals(jwtData2), equalTo(true));
        assertThat(jwtData1.equals(jwtData3), equalTo(false));
        assertThat(jwtData1.equals(null), equalTo(false));
        assertThat(jwtData1.equals("not-a-jwtdata"), equalTo(false));
    }

    @Test
    void testJwtDataHashCode() {
        //given
        JwtUtils.JwtData jwtData1 = validJwtData;
        JwtUtils.JwtData jwtData2 = validJwtData;
        JwtUtils.JwtData jwtData3 = validJwtData.withSubject("different-subject");

        //when & then
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

    @Test
    void testCreateAndParseJwtEndToEnd() throws MangooJwtException {
        //given
        Map<String, String> customClaims = Map.of(
                "role", "admin",
                "department", "IT",
                "permissions", "read,write,delete"
        );
        JwtUtils.JwtData jwtData = validJwtData.withClaims(customClaims);

        //when
        String jwt = JwtUtils.createJwt(jwtData);
        JWTClaimsSet parsedClaims = JwtUtils.parseJwt(jwt, jwtData);
        JWTClaimsSet customClaimsResult = JwtUtils.extractCustomClaims(parsedClaims);

        //then
        assertThat(jwt, not(nullValue()));
        assertThat(parsedClaims, not(nullValue()));
        assertThat(parsedClaims.getSubject(), equalTo(SUBJECT));
        assertThat(parsedClaims.getIssuer(), equalTo(ISSUER));
        assertThat(parsedClaims.getAudience(), hasItem(AUDIENCE));

        assertThat(customClaimsResult.getClaim("role"), equalTo("admin"));
        assertThat(customClaimsResult.getClaim("department"), equalTo("IT"));
        assertThat(customClaimsResult.getClaim("permissions"), equalTo("read,write,delete"));
    }

    @Test
    void testMultipleJwtOperations() throws MangooJwtException {
        //given
        JwtUtils.JwtData jwtData1 = validJwtData.withSubject("user1").withClaims(Map.of("role", "user"));
        JwtUtils.JwtData jwtData2 = validJwtData.withSubject("user2").withClaims(Map.of("role", "admin"));

        //when
        String jwt1 = JwtUtils.createJwt(jwtData1);
        String jwt2 = JwtUtils.createJwt(jwtData2);

        JWTClaimsSet claims1 = JwtUtils.parseJwt(jwt1, jwtData1);
        JWTClaimsSet claims2 = JwtUtils.parseJwt(jwt2, jwtData2);

        //then
        assertThat(jwt1, not(equalTo(jwt2)));
        assertThat(claims1.getSubject(), equalTo("user1"));
        assertThat(claims2.getSubject(), equalTo("user2"));
        assertThat(claims1.getClaim("role"), equalTo("user"));
        assertThat(claims2.getClaim("role"), equalTo("admin"));
    }
}
