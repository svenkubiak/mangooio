package io.mangoo.controllers;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.interfaces.TokenBlacklist;
import io.mangoo.test.http.TestBrowser;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.mangoo.utils.CommonUtils;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.HttpCookie;
import java.time.Instant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class TokenBlacklistTest {
    private static final String AUTHENTICATION_COOKIE = "test-authentication";
    private static final String AUTHENTICATION_REQUIRED = "/authenticationrequired";

    @Test
    void testLoggedOutCookieIsRejected() {
        //given
        TestBrowser browser = TestBrowser.open();
        HttpCookie cookie = login(browser, "/dologin");

        //when
        browser.to("/logout")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(requestWith(cookie).getStatusCode(), equalTo(StatusCodes.FOUND));
    }

    @Test
    void testRevokedSubjectCookieIsRejected() {
        //given
        String subject = CommonUtils.uuidV4();
        HttpCookie cookie = login(TestBrowser.open(), "/dologin/" + subject);
        assertThat(requestWith(cookie).getStatusCode(), equalTo(StatusCodes.OK));

        //when
        // iat has second precision, revoke everything issued up to one second from now
        Application.getInstance(TokenBlacklist.class).revokeSubject(subject, Instant.now().plusSeconds(1));

        //then
        assertThat(requestWith(cookie).getStatusCode(), equalTo(StatusCodes.FOUND));
    }

    @Test
    void testOtherSubjectIsNotAffected() {
        //given
        HttpCookie cookie = login(TestBrowser.open(), "/dologin/" + CommonUtils.uuidV4());

        //when
        Application.getInstance(TokenBlacklist.class).revokeSubject(CommonUtils.uuidV4(), Instant.now().plusSeconds(1));

        //then
        assertThat(requestWith(cookie).getStatusCode(), equalTo(StatusCodes.OK));
    }

    @Test
    void testLogoutWithoutValidCookieDoesNotFail() {
        //given
        HttpCookie invalid = new HttpCookie(AUTHENTICATION_COOKIE, "invalid");

        //when
        TestResponse withoutCookie = TestRequest.get("/logout").execute();
        TestResponse withInvalidCookie = TestRequest.get("/logout").withCookie(invalid).execute();

        //then
        assertThat(withoutCookie.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(withInvalidCookie.getStatusCode(), equalTo(StatusCodes.OK));
    }

    private static HttpCookie login(TestBrowser browser, String url) {
        TestResponse response = browser.to(url)
                .withHTTPMethod(Methods.POST.toString())
                .withDisabledRedirects()
                .execute();

        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        HttpCookie cookie = response.getCookie(AUTHENTICATION_COOKIE);
        assertThat(cookie, not(nullValue()));

        return cookie;
    }

    private static TestResponse requestWith(HttpCookie cookie) {
        return TestRequest.get(AUTHENTICATION_REQUIRED)
                .withCookie(cookie)
                .withDisabledRedirects()
                .execute();
    }
}
