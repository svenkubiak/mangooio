package io.mangoo.controllers;

import io.mangoo.TestExtension;
import io.mangoo.TestUtils;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class SessionControllerTest {
    
    @Test
    void testSessionCookie() {
        //when
        Config config = Application.getInstance(Config.class);
        TestResponse response = TestRequest.get("/session").execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getCookie(config.getSessionCookieName()), not(nullValue()));
    }

    @Test
    void testSessionCookieIsABrowserSessionCookieByDefault() {
        //when
        TestResponse response = TestRequest.get("/session").execute();

        //then
        assertThat(sessionCookieHeader(response), not(containsStringIgnoringCase("Expires=")));
    }

    @Test
    void testSessionCookieExpiresIfConfigured() throws Exception {
        //given
        try (var _ = TestUtils.withConfigValue("session.cookie.expires", "true")) {
            //when
            TestResponse response = TestRequest.get("/session").execute();

            //then
            assertThat(sessionCookieHeader(response), containsStringIgnoringCase("Expires="));
        }
    }

    private static String sessionCookieHeader(TestResponse response) {
        String name = Application.getInstance(Config.class).getSessionCookieName();
        return response.getHttpResponse().headers().allValues("Set-Cookie").stream()
                .filter(header -> header.startsWith(name + "="))
                .findFirst()
                .orElseThrow();
    }
}
