package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.configuration.Config;
import io.mangoo.test.Mangoo;
import io.mangoo.test.utils.Request;
import io.mangoo.test.utils.Response;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class SessionControllerTest {
    @Test
    public void testSessionCookie() {
        //when
        final Config config = Mangoo.TEST.getInstance(Config.class);
        final Response response = Request.get("/session").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getCookie(config.getSessionCookieName()).getName(), equalTo(config.getSessionCookieName()));
    }
}