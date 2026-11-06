package io.mangoo.utils;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.nimbusds.jwt.JWTClaimsSet;
import io.mangoo.TestExtension;
import io.mangoo.TestUtils;
import io.mangoo.constants.ClaimKey;
import io.mangoo.constants.Key;
import io.mangoo.constants.Required;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooJwtException;
import io.mangoo.interfaces.persistence.Datastore;
import io.mangoo.routing.bindings.Form;
import io.mangoo.utils.internal.MangooUtils;
import io.undertow.server.handlers.Cookie;
import org.bson.Document;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static io.mangoo.core.Application.getInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({TestExtension.class})
@TestMethodOrder(MethodOrderer.MethodName.class)
class CoreUtilsTest {
    /** Cleartext of the Argon2id hash stored as application.admin.password. */
    private static final String ADMIN_PASSWORD = "admin";
    private Config config;

    @BeforeEach
    void setUp() {
        config = getInstance(Config.class);
        resetAdmin();
    }

    // A locked admin step would make the admin controller tests fail, which run in the same JVM
    @AfterEach
    void resetAdmin() {
        MangooUtils.resetAdminAttempts(MangooUtils.ADMIN_PASSWORD);
        MangooUtils.resetAdminAttempts(MangooUtils.ADMIN_SECOND_FACTOR);
    }

    @Test
    void testGetVersion() {
        //given

        //when
        String version = MangooUtils.getVersion();

        //then
        assertThat(version, not(nullValue()));
        assertThat(version, not(emptyString()));
    }

    @Test
    void testIsValidAuthenticationWithValidCredentials() {
        //given
        Form form = new Form();
        form.addValue("username", config.getApplicationAdminUsername());
        form.addValue("password", ADMIN_PASSWORD);

        //when
        boolean isValid = MangooUtils.isValidAuthentication(form);

        //then
        assertThat(isValid, is(true));
    }

    @Test
    void testIsValidAuthenticationWithInvalidUsername() {
        //given
        Form form = new Form();
        form.addValue("username", "invalid");
        form.addValue("password", ADMIN_PASSWORD);

        //when
        boolean isValid = MangooUtils.isValidAuthentication(form);

        //then
        assertThat(isValid, is(false));
    }

    @Test
    void testIsValidAuthenticationWithInvalidPassword() {
        //given
        Form form = new Form();
        form.addValue("username", config.getApplicationAdminUsername());
        form.addValue("password", "invalid");

        //when
        boolean isValid = MangooUtils.isValidAuthentication(form);

        //then
        assertThat(isValid, is(false));
    }

    @Test
    void testIsValidAuthenticationWithEmptyCredentials() {
        //given
        Form form = new Form();
        form.addValue("username", "");
        form.addValue("password", "");

        //when
        boolean isValid = MangooUtils.isValidAuthentication(form);

        //then
        assertThat(isValid, is(false));
    }

    @Test
    void testIsValidAuthenticationWithNullCredentials() {
        //given
        Form form = new Form();
        form.addValue("username", null);
        form.addValue("password", null);

        //when
        boolean isValid = MangooUtils.isValidAuthentication(form);

        //then
        assertThat(isValid, is(false));
    }

    @Test
    void testGetAdminCookieWithoutTwoFactor() throws Exception {
        //given
        try (var _ = TestUtils.withConfigValue(Key.APPLICATION_ADMIN_SECRET, TotpUtils.createSecret())) {
            //when
            Cookie cookie = MangooUtils.getAdminCookie(false);

            //then
            assertThat(cookie.getName(), equalTo(MangooUtils.getAdminCookieName()));
            assertThat(cookie.isHttpOnly(), is(true));
            assertThat(cookie.getPath(), equalTo("/"));
            assertThat(cookie.getSameSiteMode(), equalTo("Strict"));

            JWTClaimsSet claims = parseAdminCookie(cookie);
            assertThat(MangooUtils.isTwoFactorPending(claims), is(false));
            assertThat(lifetime(claims), equalTo(1800L));
        }
    }

    @Test
    void testGetAdminCookieWithTwoFactor() throws Exception {
        //given
        try (var _ = TestUtils.withConfigValue(Key.APPLICATION_ADMIN_SECRET, TotpUtils.createSecret())) {
            //when
            Cookie cookie = MangooUtils.getAdminCookie(true);

            //then
            JWTClaimsSet claims = parseAdminCookie(cookie);
            assertThat(MangooUtils.isTwoFactorPending(claims), is(true));
            assertThat(lifetime(claims), equalTo(120L));
        }
    }

    @Test
    void testGetAdminCookieWithTwoFactorWithoutAdminSecret() throws Exception {
        //when
        Cookie cookie = MangooUtils.getAdminCookie(true);

        //then
        JWTClaimsSet claims = parseAdminCookie(cookie);
        assertThat(MangooUtils.isTwoFactorPending(claims), is(false));
        assertThat(lifetime(claims), equalTo(1800L));
    }

    @Test
    void testGetAdminCookieName() {
        //given

        //when
        String cookieName = MangooUtils.getAdminCookieName();

        //then
        assertThat(cookieName, not(nullValue()));
        assertThat(cookieName, not(emptyString()));
    }

    @Test
    void testAdminAttemptMaxRetries() {
        //when
        for (int i = 0; i < 10; i++) {
            assertThat(MangooUtils.acquireAdminAttempt(MangooUtils.ADMIN_PASSWORD), is(true));
        }

        //then the attempt that used up the budget was granted and set the lock
        assertThat(MangooUtils.isAdminLocked(MangooUtils.ADMIN_PASSWORD), is(true));
        assertThat(MangooUtils.acquireAdminAttempt(MangooUtils.ADMIN_PASSWORD), is(false));
        Date lockedUntil = adminAttempts().getDate("lockedUntil");
        assertThat(lockedUntil.toInstant().isAfter(Instant.now().plus(Duration.ofMinutes(59))), is(true));
    }

    @Test
    void testResetAdminAttempts() {
        //given
        for (int i = 0; i < 5; i++) {
            MangooUtils.acquireAdminAttempt(MangooUtils.ADMIN_PASSWORD);
        }

        //when
        MangooUtils.resetAdminAttempts(MangooUtils.ADMIN_PASSWORD);

        //then
        assertThat(adminAttempts(), nullValue());
    }

    @Test
    void testPreAuthenticationIsConsumedOnce() {
        //given
        JWTClaimsSet claims = preAuthClaims(CommonUtils.uuidV6());

        //when
        boolean first = MangooUtils.consumePreAuthentication(claims);
        boolean replay = MangooUtils.consumePreAuthentication(claims);

        //then
        assertThat(first, is(true));
        assertThat(replay, is(false));
    }

    private static Document adminAttempts() {
        return attemptsCollection().find(Filters.eq("_id", MangooUtils.ADMIN_PASSWORD)).first();
    }

    private static MongoCollection<Document> attemptsCollection() {
        return getInstance(Datastore.class).getMongoDatabase().getCollection("mangooio_attempts");
    }

    private static JWTClaimsSet preAuthClaims(String jwtId) {
        return new JWTClaimsSet.Builder()
                .jwtID(jwtId)
                .claim(ClaimKey.TWO_FACTOR, "true")
                .expirationTime(Date.from(Instant.now().plusSeconds(120)))
                .build();
    }

    @Test
    void testPreAuthenticationWithoutPendingSecondFactorIsRefused() {
        //given
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .jwtID(CommonUtils.uuidV6())
                .expirationTime(Date.from(Instant.now().plusSeconds(120)))
                .build();

        //then
        assertThat(MangooUtils.consumePreAuthentication(claims), is(false));
    }

    @Test
    void testPreAuthenticationClaimOutlivesTheCookieByTheClockSkew() {
        //given
        String jwtId = CommonUtils.uuidV6();
        JWTClaimsSet claims = preAuthClaims(jwtId);

        //when
        MangooUtils.consumePreAuthentication(claims);

        //then
        Document claim = getInstance(Datastore.class).getMongoDatabase().getCollection("mangooio_claims")
                .find(Filters.eq("_id", "admin-pre-auth:" + jwtId)).first();
        assertThat(claim.getDate("expireAt").toInstant(), equalTo(claims.getExpirationTime().toInstant().plusSeconds(30)));
    }

    @Test
    void testMergeMapsWithSimpleValues() {
        //given
        Map<String, Object> baseMap = new HashMap<>();
        baseMap.put("key1", "value1");
        baseMap.put("key2", "value2");
        
        Map<String, Object> overrideMap = new HashMap<>();
        overrideMap.put("key2", "newValue2");
        overrideMap.put("key3", "value3");

        //when
        MangooUtils.mergeMaps(baseMap, overrideMap);

        //then
        assertThat(baseMap.get("key1"), equalTo("value1"));
        assertThat(baseMap.get("key2"), equalTo("newValue2"));
        assertThat(baseMap.get("key3"), equalTo("value3"));
        assertThat(baseMap.size(), equalTo(3));
    }

    @Test
    void testMergeMapsWithNestedMaps() {
        //given
        Map<String, Object> baseMap = new HashMap<>();
        Map<String, Object> nestedBase = new HashMap<>();
        nestedBase.put("nested1", "value1");
        nestedBase.put("nested2", "value2");
        baseMap.put("nested", nestedBase);
        
        Map<String, Object> overrideMap = new HashMap<>();
        Map<String, Object> nestedOverride = new HashMap<>();
        nestedOverride.put("nested2", "newValue2");
        nestedOverride.put("nested3", "value3");
        overrideMap.put("nested", nestedOverride);

        //when
        MangooUtils.mergeMaps(baseMap, overrideMap);

        //then
        @SuppressWarnings("unchecked")
        Map<String, Object> resultNested = (Map<String, Object>) baseMap.get("nested");
        assertThat(resultNested.get("nested1"), equalTo("value1"));
        assertThat(resultNested.get("nested2"), equalTo("newValue2"));
        assertThat(resultNested.get("nested3"), equalTo("value3"));
        assertThat(resultNested.size(), equalTo(3));
    }

    @Test
    void testMergeMapsWithEmptyOverride() {
        //given
        Map<String, Object> baseMap = new HashMap<>();
        baseMap.put("key1", "value1");
        
        Map<String, Object> overrideMap = new HashMap<>();

        //when
        MangooUtils.mergeMaps(baseMap, overrideMap);

        //then
        assertThat(baseMap.get("key1"), equalTo("value1"));
        assertThat(baseMap.size(), equalTo(1));
    }

    @Test
    void testMergeMapsWithNullOverride() {
        //given
        Map<String, Object> baseMap = new HashMap<>();
        baseMap.put("key1", "value1");

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> MangooUtils.mergeMaps(baseMap, null));
        assertThat(exception.getMessage(), containsString("map can not be null or blank"));
    }

    @Test
    void testFlattenMapWithSimpleValues() {
        //given
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        map.put("key3", 123);

        //when
        Map<String, String> flattened = MangooUtils.flattenMap(map);

        //then
        assertThat(flattened.get("key1"), equalTo("value1"));
        assertThat(flattened.get("key2"), equalTo("value2"));
        assertThat(flattened.get("key3"), equalTo("123"));
        assertThat(flattened.size(), equalTo(3));
    }

    @Test
    void testFlattenMapWithNestedMaps() {
        //given
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> nested = new HashMap<>();
        nested.put("nested1", "value1");
        nested.put("nested2", "value2");
        map.put("parent", nested);
        map.put("simple", "value");

        //when
        Map<String, String> flattened = MangooUtils.flattenMap(map);

        //then
        assertThat(flattened.get("parent.nested1"), equalTo("value1"));
        assertThat(flattened.get("parent.nested2"), equalTo("value2"));
        assertThat(flattened.get("simple"), equalTo("value"));
        assertThat(flattened.size(), equalTo(3));
    }

    @Test
    void testFlattenMapWithDeeplyNestedMaps() {
        //given
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> level1 = new HashMap<>();
        Map<String, Object> level2 = new HashMap<>();
        level2.put("deep", "value");
        level1.put("level2", level2);
        map.put("level1", level1);

        //when
        Map<String, String> flattened = MangooUtils.flattenMap(map);

        //then
        assertThat(flattened.get("level1.level2.deep"), equalTo("value"));
        assertThat(flattened.size(), equalTo(1));
    }

    @Test
    void testFlattenMapWithNullValues() {
        //given
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", null);

        //when
        Map<String, String> flattened = MangooUtils.flattenMap(map);

        //then
        assertThat(flattened.get("key1"), equalTo("value1"));
        assertThat(flattened.get("key2"), equalTo(""));
        assertThat(flattened.size(), equalTo(2));
    }

    @Test
    void testFlattenMapWithEmptyMap() {
        //given
        Map<String, Object> map = new HashMap<>();

        //when
        Map<String, String> flattened = MangooUtils.flattenMap(map);

        //then
        assertThat(flattened, not(nullValue()));
        assertThat(flattened.isEmpty(), is(true));
    }

    @Test
    void testFlattenMapWithNullMap() {
        //given

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> MangooUtils.flattenMap(null));
        assertThat(exception.getMessage(), containsString("map can not be null or blank"));
    }

    @Test
    void testFlattenMapHelperWithPrefix() {
        //given
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        
        Map<String, String> flatMap = new HashMap<>();

        //when
        MangooUtils.flattenMapHelper(map, "prefix", flatMap);

        //then
        assertThat(flatMap.get("prefix.key1"), equalTo("value1"));
        assertThat(flatMap.size(), equalTo(1));
    }

    @Test
    void testFlattenMapHelperWithNullMap() {
        //given
        Map<String, String> flatMap = new HashMap<>();

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> MangooUtils.flattenMapHelper(null, "", flatMap));
        assertThat(exception.getMessage(), containsString("map can not be null or blank"));
    }

    @Test
    void testFlattenMapHelperWithNullPrefix() {
        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> MangooUtils.flattenMapHelper(Map.of("key", "value"), null, new HashMap<>()));
        assertThat(exception.getMessage(), equalTo(Required.PREFIX));
    }

    @Test
    void testFlattenMapHelperWithNullFlatMap() {
        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> MangooUtils.flattenMapHelper(Map.of("key", "value"), "", null));
        assertThat(exception.getMessage(), equalTo(Required.FLAT_MAP));
    }

    private JWTClaimsSet parseAdminCookie(Cookie cookie) throws MangooJwtException {
        var jwtData = JwtUtils.JwtData.create()
                .withKey(config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))
                .withSecret(config.getApplicationSecret().getBytes(StandardCharsets.UTF_8))
                .withIssuer(config.getApplicationName())
                .withAudience(MangooUtils.getAdminCookieName())
                .withTtlSeconds(1800);

        return JwtUtils.parseJwt(cookie.getValue(), jwtData);
    }

    private static long lifetime(JWTClaimsSet claims) {
        return Duration.between(claims.getIssueTime().toInstant(), claims.getExpirationTime().toInstant()).toSeconds();
    }
}
