package io.mangoo.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

import org.junit.Test;

import io.undertow.server.handlers.Cookie;

/**
 * 
 * @author svenkubiak
 *
 */
public class CookieBuilderTest {

    @Test
    public void testBuilder() {
        LocalDateTime now = LocalDateTime.now();
        Cookie cookie = CookieBuilder.create()
                .name("foo")
                .value("bar")
                .path("/foobar")
                .domain("www.foo.com")
                .maxAge(1223)
                .expires(now)
                .discard(true)
                .secure(true)
                .httpOnly(true)
                .build();
        
        assertNotNull(cookie);
        assertEquals("foo", cookie.getName());
        assertEquals("bar", cookie.getValue());
        assertEquals("/foobar", cookie.getPath());
        assertEquals("www.foo.com", cookie.getDomain());
        assertEquals(Date.from(now.atZone(ZoneId.systemDefault()).toInstant()), cookie.getExpires());
        assertTrue(cookie.getMaxAge() == 1223);
        assertTrue(cookie.isDiscard());
        assertTrue(cookie.isSecure());
        assertTrue(cookie.isHttpOnly());
    }
}