package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.test.utils.Request;
import io.mangoo.test.utils.Response;
import io.mangoo.utils.ConfigUtils;
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
        Response response = Request.get("/session").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getCookie(ConfigUtils.getSessionCookieName()).getName(), equalTo(ConfigUtils.getSessionCookieName()));
    }
}