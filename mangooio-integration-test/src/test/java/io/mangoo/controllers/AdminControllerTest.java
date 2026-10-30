package io.mangoo.controllers;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.mangoo.Csrf;
import io.mangoo.TestExtension;
import io.mangoo.TestUtils;
import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheImpl;
import io.mangoo.cache.CacheProvider;
import io.mangoo.constants.CacheName;
import io.mangoo.constants.Const;
import io.mangoo.constants.Key;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.models.Metrics;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.mangoo.utils.TotpUtils;
import io.mangoo.utils.internal.MangooUtils;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.reflect.Proxy;
import java.net.HttpCookie;
import java.time.Duration;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class AdminControllerTest {
    private static final String TEXT_HTML = "text/html; charset=UTF-8";
    private static final String EVICTIONS = "Evictions";
    private static final String CACHE = "cache";
    private static final String CONTROL_PANEL = "Dashboard";
    /** Cleartext of the Argon2id hash stored as application.admin.password. */
    private static final String ADMIN_PASSWORD = "admin";

    @Test
    void testLoginWithoutCsrf() {
        Multimap<String, String> parameters = ArrayListMultimap.create();
        parameters.put("username", Application.getInstance(Config.class).getApplicationAdminUsername());
        parameters.put("password", ADMIN_PASSWORD);

        TestResponse response = TestRequest.post("/@admin/authenticate")
                .withForm(parameters)
                .execute();
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
        assertThat(response.getContent(), not(containsString(CONTROL_PANEL)));
    }

    @Test
    void testLoginWithCsrf() {
        Csrf csrf = TestUtils.getCsrf();
        Multimap<String, String> parameters = ArrayListMultimap.create();
        parameters.put("username", Application.getInstance(Config.class).getApplicationAdminUsername());
        parameters.put("password", ADMIN_PASSWORD);
        parameters.put(Const.CSRF_TOKEN, csrf.token());

        TestResponse response = TestRequest.post("/@admin/authenticate")
                .withCookie(csrf.cookie())
                .withForm(parameters)
                .execute();
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString(CONTROL_PANEL));
    }

    @Test
    void testDashboardUnauthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
    }
    
    @Test
    void testCacheAuthorized() {
        //given
        TestResponse response = login().to("/@admin/cache")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(CACHE));
        assertThat(response.getContent(), containsString(EVICTIONS));
    }

    @Test
    void testCacheWithForeignCacheImplementation() {
        //given
        String name = "foreign-cache";
        Cache foreign = (Cache) Proxy.newProxyInstance(Cache.class.getClassLoader(), new Class<?>[]{Cache.class}, (proxy, method, args) -> null);
        Application.getInstance(CacheProvider.class).addCache(name, foreign);

        try {
            //when
            TestResponse response = login().to("/@admin/cache")
                    .withHTTPMethod(Methods.GET.toString())
                    .execute();

            //then
            assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
            assertThat(response.getContent(), containsString(CacheName.APPLICATION));
        } finally {
            Application.getInstance(CacheProvider.class).getCaches().remove(name);
        }
    }

    @Test
    void testCacheUnauthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/cache")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
    }
    
    @Test
    void testDashboardAuthorized() {
        //given
        TestResponse response = login().to("/@admin")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(CONTROL_PANEL));
    }

    @Test
    void testDashboardShowsSystemAndStatusCodes() {
        //given
        Application.getInstance(Metrics.class).addStatusCode(StatusCodes.NOT_FOUND);

        //when
        TestResponse response = login().to("/@admin")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString("Heap memory"));
        assertThat(response.getContent(), containsString(Runtime.version().toString()));
        assertThat(response.getContent(), containsString("Responses by status"));
        assertThat(response.getContent(), containsString("4xx"));
    }

    @Test
    void testDashboardShowsEventsAndFeatures() {
        //when
        TestResponse response = login().to("/@admin")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString("Open connections"));
        assertThat(response.getContent(), containsString("Failed events"));
        assertThat(response.getContent(), containsString("subscribers.FailingEvent"));
        assertThat(response.getContent(), containsString("Admin two factor"));
    }

    @Test
    void testDashboardListsThisInstance() {
        //when
        TestResponse response = login().to("/@admin")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString("Instances ("));
        assertThat(response.getContent(), containsString(">" + Application.getInstanceId() + "</code> <span class=\"badge badge-success\">This instance</span>"));
    }

    @Test
    void testRoutesAuthorized() {
        //when
        TestResponse response = login().to("/@admin/routes")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString("ApplicationController.index"));
        assertThat(response.getContent(), containsString("/anyof"));
        assertThat(response.getContent(), containsString("/sse/client"));
        assertThat(response.getContent(), not(containsString("AdminController")));
    }

    @Test
    void testRoutesUnauthorized() {
        //when
        TestResponse response = TestRequest.get("/@admin/routes")
                .withDisabledRedirects()
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
    }

    @Test
    void testClearCache() {
        //given
        String name = "admin-clear-test";
        Cache cache = new CacheImpl(Caffeine.newBuilder().recordStats().build());
        cache.put("key", "value");
        Application.getInstance(CacheProvider.class).addCache(name, cache);

        try {
            //when
            TestResponse response = adminPost("/@admin/cache/clear", Map.of("name", name), true);

            //then
            assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
            assertThat(response.getHeader("Location"), equalTo("/@admin/cache"));
            await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(cache.get("key"), nullValue()));
        } finally {
            Application.getInstance(CacheProvider.class).getCaches().remove(name);
        }
    }

    @Test
    void testClearCacheWithoutCsrf() {
        //given
        String name = "admin-clear-csrf-test";
        Cache cache = new CacheImpl(Caffeine.newBuilder().recordStats().build());
        cache.put("key", "value");
        Application.getInstance(CacheProvider.class).addCache(name, cache);

        try {
            //when
            TestResponse response = adminPost("/@admin/cache/clear", Map.of("name", name), false);

            //then
            assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
            assertThat(cache.get("key"), equalTo("value"));
        } finally {
            Application.getInstance(CacheProvider.class).getCaches().remove(name);
        }
    }

    @Test
    void testClearUnknownCache() {
        //when
        TestResponse response = adminPost("/@admin/cache/clear", Map.of("name", "does-not-exist"), true);

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/cache"));
    }

    @Test
    void testClearCacheUnauthorized() {
        //given
        Csrf csrf = TestUtils.getCsrf();
        Multimap<String, String> parameters = ArrayListMultimap.create();
        parameters.put("name", CacheName.APPLICATION);
        parameters.put(Const.CSRF_TOKEN, csrf.token());

        //when
        TestResponse response = TestRequest.post("/@admin/cache/clear")
                .withCookie(csrf.cookie())
                .withForm(parameters)
                .withDisabledRedirects()
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
    }

    @Test
    void testResetMetrics() {
        //given
        int statusCode = 599;
        Application.getInstance(Metrics.class).addStatusCode(statusCode);

        //when
        TestResponse response = adminPost("/@admin/metrics/reset", Map.of(), true);

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin"));
        await().atMost(Duration.ofSeconds(5)).untilAsserted(() -> assertThat(Application.getInstance(Metrics.class).getResponseMetrics(), not(hasKey(statusCode))));
    }

    @Test
    void testResetMetricsWithoutCsrf() {
        //when
        TestResponse response = adminPost("/@admin/metrics/reset", Map.of(), false);

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
    }

    @Test
    void testSecurityTwoFactorAuthorized() {
        //given
        TestResponse response = login().to("/@admin/security")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString("Two Factor Authentication"));
    }
    
    @Test
    void testSecurityUnauthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/security")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
    }

    @Test
    void testTwoFactorUnauthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/twofactor")
                .withDisabledRedirects()
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
    }

    @Test
    void testVerifyWithoutFirstFactor() {
        //given
        Csrf csrf = TestUtils.getCsrf();
        Multimap<String, String> parameters = ArrayListMultimap.create();
        parameters.put("code", "123456");
        parameters.put(Const.CSRF_TOKEN, csrf.token());

        TestResponse response = TestRequest.post("/@admin/verify")
                .withCookie(csrf.cookie())
                .withForm(parameters)
                .withDisabledRedirects()
                .execute();

        //then the second factor alone must never yield an admin session
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
    }

    @Test
    void testLogin() {
        //given
        TestResponse response = TestRequest.get("/@admin/login").execute();
        
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString("login"));
    }
    
    @Test
    void testLoginWithSecondFactor() throws Exception {
        //given
        String secret = TotpUtils.createSecret();
        try (var _ = TestUtils.withConfigValue(Key.APPLICATION_ADMIN_SECRET, secret)) {
            //when
            TestResponse authenticate = authenticate();
            HttpCookie preAuthentication = authenticate.getCookie(MangooUtils.getAdminCookieName());

            //then
            assertThat(authenticate.getHeader("Location"), equalTo("/@admin"));
            TestResponse dashboard = TestRequest.get("/@admin")
                    .withCookie(preAuthentication)
                    .withDisabledRedirects()
                    .execute();
            assertThat(dashboard.getStatusCode(), equalTo(StatusCodes.FOUND));
            assertThat(dashboard.getHeader("Location"), equalTo("/@admin/twofactor"));

            //when
            TestResponse verify = verify(preAuthentication, TotpUtils.getTotp(secret));
            HttpCookie admin = verify.getCookie(MangooUtils.getAdminCookieName());

            //then
            assertThat(verify.getHeader("Location"), equalTo("/@admin"));
            dashboard = TestRequest.get("/@admin")
                    .withCookie(admin)
                    .withDisabledRedirects()
                    .execute();
            assertThat(dashboard.getStatusCode(), equalTo(StatusCodes.OK));
            assertThat(dashboard.getContent(), containsString(CONTROL_PANEL));

            //when the pre-authentication cookie is replayed
            TestResponse replay = verify(preAuthentication, TotpUtils.getTotp(secret));

            //then
            assertThat(replay.getHeader("Location"), not(equalTo("/@admin")));
        }
    }

    @Test
    void testPasswordLoginDoesNotResetSecondFactorAttempts() throws Exception {
        //given
        String secret = TotpUtils.createSecret();
        try (var _ = TestUtils.withConfigValue(Key.APPLICATION_ADMIN_SECRET, secret)) {
            HttpCookie preAuthentication = authenticate().getCookie(MangooUtils.getAdminCookieName());
            for (var i = 0; i < 9; i++) {
                verify(preAuthentication, invalidTotp(secret));
            }

            //when
            preAuthentication = authenticate().getCookie(MangooUtils.getAdminCookieName());
            assertThat(MangooUtils.isAdminLocked(MangooUtils.ADMIN_SECOND_FACTOR), equalTo(false));
            TestResponse response = verify(preAuthentication, invalidTotp(secret));

            //then
            assertThat(response.getHeader("Location"), equalTo("/@admin/twofactor"));
            assertThat(MangooUtils.isAdminLocked(MangooUtils.ADMIN_SECOND_FACTOR), equalTo(true));
        } finally {
            MangooUtils.resetAdminAttempts(MangooUtils.ADMIN_PASSWORD);
            MangooUtils.resetAdminAttempts(MangooUtils.ADMIN_SECOND_FACTOR);
        }
    }

    @Test
    void testSecurityProposesABase32Secret() {
        //when
        TestResponse response = login().to("/@admin/security")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        Matcher matcher = Pattern.compile("id=\"secret\">([^<]*)</textarea>").matcher(response.getContent());
        assertThat(matcher.find(), equalTo(true));
        assertThat(matcher.group(1), matchesPattern("[A-Z2-7]{64}"));
    }

    private TestResponse authenticate() {
        Csrf csrf = TestUtils.getCsrf();
        Multimap<String, String> parameters = ArrayListMultimap.create();
        parameters.put("username", Application.getInstance(Config.class).getApplicationAdminUsername());
        parameters.put("password", ADMIN_PASSWORD);
        parameters.put(Const.CSRF_TOKEN, csrf.token());

        return TestRequest.post("/@admin/authenticate")
                .withCookie(csrf.cookie())
                .withForm(parameters)
                .withDisabledRedirects()
                .execute();
    }

    private TestResponse verify(HttpCookie preAuthentication, String code) {
        Csrf csrf = TestUtils.getCsrf();
        Multimap<String, String> parameters = ArrayListMultimap.create();
        parameters.put("code", code);
        parameters.put(Const.CSRF_TOKEN, csrf.token());

        return TestRequest.post("/@admin/verify")
                .withCookie(csrf.cookie())
                .withCookie(preAuthentication)
                .withForm(parameters)
                .withDisabledRedirects()
                .execute();
    }

    private TestResponse adminPost(String uri, Map<String, String> values, boolean withCsrf) {
        HttpCookie admin = authenticate().getCookie(MangooUtils.getAdminCookieName());
        Csrf csrf = TestUtils.getCsrf();
        Multimap<String, String> parameters = ArrayListMultimap.create();
        values.forEach(parameters::put);
        if (withCsrf) {
            parameters.put(Const.CSRF_TOKEN, csrf.token());
        }

        return TestRequest.post(uri)
                .withCookie(csrf.cookie())
                .withCookie(admin)
                .withForm(parameters)
                .withDisabledRedirects()
                .execute();
    }

    private static String invalidTotp(String secret) {
        return "000000".equals(TotpUtils.getTotp(secret)) ? "111111" : "000000";
    }

    private TestResponse login() {
        Csrf csrf = TestUtils.getCsrf();
        Multimap<String, String> parameters = ArrayListMultimap.create();
        parameters.put("username", Application.getInstance(Config.class).getApplicationAdminUsername());
        parameters.put("password", ADMIN_PASSWORD);
        parameters.put(Const.CSRF_TOKEN, csrf.token());

        return TestRequest.post("/@admin/authenticate")
                .withCookie(csrf.cookie())
                .withForm(parameters)
                .execute();
    }
}