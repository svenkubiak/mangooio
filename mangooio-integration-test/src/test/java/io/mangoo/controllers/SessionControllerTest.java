package io.mangoo.controllers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.http.cookie.Cookie;
import org.junit.Test;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Key;
import io.mangoo.test.MangooRequest;
import io.mangoo.test.MangooResponse;
import io.mangoo.test.MangooTestInstance;

/**
 * 
 * @author svenkubiak
 *
 */
public class SessionControllerTest {
    @Test
    public void sessionTest() {
        Config config = MangooTestInstance.IO.getInjector().getInstance(Config.class);
        String cookieName = config.getString(Key.COOKIE_NAME);
        
        MangooResponse response = MangooRequest.get("/session").execute();
        List<Cookie> cookies = response.getCookies();
        
        assertNotNull(cookies);
        assertEquals(cookieName, cookies.get(0).getName());
    }
}