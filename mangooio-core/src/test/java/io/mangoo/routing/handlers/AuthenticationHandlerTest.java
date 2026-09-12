package io.mangoo.routing.handlers;

import io.undertow.server.HttpServerExchange;
import org.junit.jupiter.api.Test;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AuthenticationHandlerTest {

    @Test
    void testOriginContainsRequestUri() {
        //given
        var exchange = exchange("/admin/secret", "");

        //when
        var origin = AuthenticationHandler.origin(exchange);

        //then
        assertEquals("%2Fadmin%2Fsecret", origin);
        assertEquals("/admin/secret", decode(origin));
    }

    @Test
    void testOriginContainsQueryString() {
        //given
        var exchange = exchange("/admin/secret", "a=1&b=2");

        //when
        var origin = AuthenticationHandler.origin(exchange);

        //then
        assertEquals("/admin/secret?a=1&b=2", decode(origin));
    }

    @Test
    void testOriginWithoutQueryStringHasNoQuestionMark() {
        //given
        var exchange = exchange("/admin/secret", "");

        //when
        var origin = AuthenticationHandler.origin(exchange);

        //then
        assertEquals("/admin/secret", decode(origin));
    }

    @Test
    void testOriginDoesNotInjectAdditionalParameters() {
        //given an ampersand is a legal sub-delimiter inside a URI path
        var exchange = exchange("/admin&next=https://evil.com", "");

        //when
        var origin = AuthenticationHandler.origin(exchange);

        //then it must not survive as a parameter separator in the redirect
        assertFalse(origin.contains("&"), "origin must not contain a raw ampersand: " + origin);
        assertEquals("/admin&next=https://evil.com", decode(origin));
    }

    @Test
    void testOriginIsNotProtocolRelative() {
        //given
        Map<String, String> uris = Map.of(
                "//evil.com/path", "/evil.com/path",
                "///evil.com/path", "/evil.com/path",
                "////evil.com/path", "/evil.com/path",
                "/admin/secret", "/admin/secret");

        uris.forEach((requestUri, expected) -> {
            //when
            var origin = AuthenticationHandler.origin(exchange(requestUri, ""));

            //then
            assertEquals(expected, decode(origin), "unexpected origin for " + requestUri);
            assertFalse(decode(origin).startsWith("//"), "origin must not be protocol-relative: " + origin);
        });
    }

    @Test
    void testOriginKeepsPercentEncodingOfRequestUri() {
        //given the request URI is handed over undecoded by Undertow
        var exchange = exchange("/admin%0d%0aX-Injected:%20yes", "");

        //when
        var origin = AuthenticationHandler.origin(exchange);

        //then a single decode must not yield control characters
        assertEquals("/admin%0d%0aX-Injected:%20yes", decode(origin));
    }

    @Test
    void testOriginWithEmptyRequestUri() {
        //given
        var exchange = exchange("", "");

        //when
        var origin = AuthenticationHandler.origin(exchange);

        //then
        assertEquals("/", decode(origin));
    }

    private static HttpServerExchange exchange(String requestUri, String queryString) {
        var exchange = new HttpServerExchange(null);
        exchange.setRequestURI(requestUri);
        exchange.setQueryString(queryString);

        return exchange;
    }

    private static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }
}
