package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.utils.http.WebRequest;
import io.mangoo.utils.http.WebResponse;
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
        final Config config = Application.getInstance(Config.class);
        final WebResponse response = WebRequest.get("/session").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getCookie(config.getSessionCookieName()).getName(), equalTo(config.getSessionCookieName()));
    }
}