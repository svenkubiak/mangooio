package io.mangoo.utils.cookie;

import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;

/**
 * 
 * @author svenkubiak
 *
 */
public final class CookieUtils {

    /**
     * Retrieves the value of a cookie with a given name from a HttpServerExchange
     * 
     * @param exchange The exchange containing the cookie
     * @param cookieName The name of the cookie
     * 
     * @return The value of the cookie or null if none found
     */
    public static String getCookieValue(HttpServerExchange exchange, String cookieName) {
        String value = null;
        final Cookie cookie = exchange.getRequestCookies().get(cookieName);
        if (cookie != null) {
            value = cookie.getValue();
        }

        return value;
    }
}
