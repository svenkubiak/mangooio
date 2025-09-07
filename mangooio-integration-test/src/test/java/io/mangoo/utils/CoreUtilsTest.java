package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.cache.Cache;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooJwtException;
import io.mangoo.routing.bindings.Form;
import io.undertow.server.handlers.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static io.mangoo.core.Application.getInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({TestExtension.class})
@TestMethodOrder(MethodOrderer.MethodName.class)
class CoreUtilsTest {
    private static final String MANGOOIO_ADMIN_LOCKED_UNTIL = "mangooio-admin-locked-until";
    private static final String MANGOOIO_ADMIN_LOCK_COUNT = "mangooio-admin-lock-count";
    private Config config;
    private Cache cache;

    @BeforeEach
    void setUp() {
        config = getInstance(Config.class);
        cache = getInstance(Cache.class);
    }

    @Test
    void testGetVersion() {
        //given
        // No setup needed for static method

        //when
        String version = CoreUtils.getVersion();

        //then
        assertThat(version, not(nullValue()));
        assertThat(version, not(emptyString()));
    }

    @Test
    void testGetLanguages() {
        //given
        // No setup needed for static method

        //when
        Set<String> languages = CoreUtils.getLanguages();

        //then
        assertThat(languages, not(nullValue()));
        assertThat(languages, instanceOf(Set.class));
    }

    @Test
    void testGetRootFolder() {
        //given
        // No setup needed for static method

        //when
        String rootFolder = CoreUtils.getRootFolder();

        //then
        assertThat(rootFolder, not(nullValue()));
    }

    @Test
    void testIsValidAuthenticationWithValidCredentials() {
        //given
        Form form = new Form();
        form.addValue("username", config.getApplicationAdminUsername());
        form.addValue("password", config.getApplicationAdminPassword());

        //when
        boolean isValid = CoreUtils.isValidAuthentication(form);

        //then
        assertThat(isValid, is(true));
    }

    @Test
    void testIsValidAuthenticationWithInvalidUsername() {
        //given
        Form form = new Form();
        form.addValue("username", "invalid");
        form.addValue("password", config.getApplicationAdminPassword());

        //when
        boolean isValid = CoreUtils.isValidAuthentication(form);

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
        boolean isValid = CoreUtils.isValidAuthentication(form);

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
        boolean isValid = CoreUtils.isValidAuthentication(form);

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
        boolean isValid = CoreUtils.isValidAuthentication(form);

        //then
        assertThat(isValid, is(false));
    }

    @Test
    void testGetAdminCookieWithoutTwoFactor() throws MangooJwtException {
        //given
        // No setup needed for static method

        //when
        Cookie cookie = CoreUtils.getAdminCookie(false);

        //then
        assertThat(cookie, not(nullValue()));
        assertThat(cookie.getName(), equalTo(CoreUtils.getAdminCookieName()));
        assertThat(cookie.isHttpOnly(), is(true));
        assertThat(cookie.getPath(), equalTo("/"));
        assertThat(cookie.getSameSiteMode(), equalTo("Strict"));
        assertThat(cookie.getValue(), not(nullValue()));
        assertThat(cookie.getValue(), not(emptyString()));
    }

    @Test
    void testGetAdminCookieWithTwoFactor() throws MangooJwtException {
        //given
        // No setup needed for static method

        //when
        Cookie cookie = CoreUtils.getAdminCookie(true);

        //then
        assertThat(cookie, not(nullValue()));
        assertThat(cookie.getName(), equalTo(CoreUtils.getAdminCookieName()));
        assertThat(cookie.isHttpOnly(), is(true));
        assertThat(cookie.getPath(), equalTo("/"));
        assertThat(cookie.getSameSiteMode(), equalTo("Strict"));
        assertThat(cookie.getValue(), not(nullValue()));
        assertThat(cookie.getValue(), not(emptyString()));
    }

    @Test
    void testGetAdminCookieName() {
        //given
        // No setup needed for static method

        //when
        String cookieName = CoreUtils.getAdminCookieName();

        //then
        assertThat(cookieName, not(nullValue()));
        assertThat(cookieName, not(emptyString()));
    }

    @Test
    void testInvalidAuthentication() {
        //given
        CoreUtils.resetLockCounter();

        //when
        CoreUtils.invalidAuthentication();

        //then
        AtomicInteger counter = cache.get(MANGOOIO_ADMIN_LOCK_COUNT);
        assertThat(counter, not(nullValue()));
        assertThat(counter.intValue(), equalTo(1));
    }

    @Test
    void testInvalidAuthenticationMaxRetries() {
        //given
        CoreUtils.resetLockCounter();

        //when
        for (int i = 0; i < 10; i++) {
            CoreUtils.invalidAuthentication();
        }

        //then
        assertThat(CoreUtils.isNotLocked(), is(false));
        LocalDateTime lockedUntil = cache.get(MANGOOIO_ADMIN_LOCKED_UNTIL);
        assertThat(lockedUntil, not(nullValue()));
        assertThat(lockedUntil.isAfter(LocalDateTime.now()), is(true));
    }

    @Test
    void testIsNotLockedWhenNotLocked() {
        //given
        cache.remove(MANGOOIO_ADMIN_LOCKED_UNTIL);

        //when
        boolean notLocked = CoreUtils.isNotLocked();

        //then
        assertThat(notLocked, is(true));
    }

    @Test
    void testIsNotLockedWhenLocked() {
        //given
        LocalDateTime futureTime = LocalDateTime.now().plusMinutes(30);
        cache.put(MANGOOIO_ADMIN_LOCKED_UNTIL, futureTime);

        //when
        boolean notLocked = CoreUtils.isNotLocked();

        //then
        assertThat(notLocked, is(false));
    }

    @Test
    void testIsNotLockedWhenLockExpired() {
        //given
        LocalDateTime pastTime = LocalDateTime.now().minusMinutes(30);
        cache.put(MANGOOIO_ADMIN_LOCKED_UNTIL, pastTime);

        //when
        boolean notLocked = CoreUtils.isNotLocked();

        //then
        assertThat(notLocked, is(true));
    }

    @Test
    void testResetLockCounter() {
        //given
        cache.put(MANGOOIO_ADMIN_LOCK_COUNT, new AtomicInteger(5));

        //when
        CoreUtils.resetLockCounter();

        //then
        AtomicInteger counter = cache.get(MANGOOIO_ADMIN_LOCK_COUNT);
        assertThat(counter, not(nullValue()));
        assertThat(counter.intValue(), equalTo(0));
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
        CoreUtils.mergeMaps(baseMap, overrideMap);

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
        CoreUtils.mergeMaps(baseMap, overrideMap);

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
        CoreUtils.mergeMaps(baseMap, overrideMap);

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
                () -> CoreUtils.mergeMaps(baseMap, null));
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
        Map<String, String> flattened = CoreUtils.flattenMap(map);

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
        Map<String, String> flattened = CoreUtils.flattenMap(map);

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
        Map<String, String> flattened = CoreUtils.flattenMap(map);

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
        Map<String, String> flattened = CoreUtils.flattenMap(map);

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
        Map<String, String> flattened = CoreUtils.flattenMap(map);

        //then
        assertThat(flattened, not(nullValue()));
        assertThat(flattened.isEmpty(), is(true));
    }

    @Test
    void testFlattenMapWithNullMap() {
        //given
        // No setup needed for null input

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> CoreUtils.flattenMap(null));
        assertThat(exception.getMessage(), containsString("map can not be null or blank"));
    }

    @Test
    void testFlattenMapHelperWithSimpleValues() {
        //given
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", "value2");
        
        Map<String, String> flatMap = new HashMap<>();

        //when
        CoreUtils.flattenMapHelper(map, "", flatMap);

        //then
        assertThat(flatMap.get("key1"), equalTo("value1"));
        assertThat(flatMap.get("key2"), equalTo("value2"));
        assertThat(flatMap.size(), equalTo(2));
    }

    @Test
    void testFlattenMapHelperWithPrefix() {
        //given
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        
        Map<String, String> flatMap = new HashMap<>();

        //when
        CoreUtils.flattenMapHelper(map, "prefix", flatMap);

        //then
        assertThat(flatMap.get("prefix.key1"), equalTo("value1"));
        assertThat(flatMap.size(), equalTo(1));
    }

    @Test
    void testFlattenMapHelperWithNestedMaps() {
        //given
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> nested = new HashMap<>();
        nested.put("nested1", "value1");
        map.put("parent", nested);
        
        Map<String, String> flatMap = new HashMap<>();

        //when
        CoreUtils.flattenMapHelper(map, "", flatMap);

        //then
        assertThat(flatMap.get("parent.nested1"), equalTo("value1"));
        assertThat(flatMap.size(), equalTo(1));
    }

    @Test
    void testFlattenMapHelperWithNullValues() {
        //given
        Map<String, Object> map = new HashMap<>();
        map.put("key1", "value1");
        map.put("key2", null);
        
        Map<String, String> flatMap = new HashMap<>();

        //when
        CoreUtils.flattenMapHelper(map, "", flatMap);

        //then
        assertThat(flatMap.get("key1"), equalTo("value1"));
        assertThat(flatMap.get("key2"), equalTo(""));
        assertThat(flatMap.size(), equalTo(2));
    }

    @Test
    void testFlattenMapHelperWithEmptyMap() {
        //given
        Map<String, Object> map = new HashMap<>();
        
        Map<String, String> flatMap = new HashMap<>();

        //when
        CoreUtils.flattenMapHelper(map, "", flatMap);

        //then
        assertThat(flatMap.isEmpty(), is(true));
    }

    @Test
    void testFlattenMapHelperWithNullMap() {
        //given
        Map<String, String> flatMap = new HashMap<>();

        //when & then
        NullPointerException exception = assertThrows(NullPointerException.class,
                () -> CoreUtils.flattenMapHelper(null, "", flatMap));
        assertThat(exception.getMessage(), containsString("map can not be null or blank"));
    }
}
