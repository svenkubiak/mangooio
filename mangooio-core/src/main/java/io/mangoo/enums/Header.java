package io.mangoo.enums;

import io.undertow.util.HttpString;

/**
 * Custom headers which are not part of undertow
 *
 * @author svenkubiak
 *
 */
public enum Header {
    ACCEPT_LANGUAGE("Accept-Language", new HttpString("Accept-Language")),
    AUTHORIZATION("Authorization", new HttpString("Authorization")),
    CONTENT_DISPOSITION("Content-Disposition", new HttpString("Content-Disposition")),
    CONTENT_SECURITY_POLICY("Content-Security-Policy", new HttpString("Content-Security-Policy")),
    CONTENT_TYPE("Content-Type", new HttpString("Content-Type")),
    COOKIE("Cookie", new HttpString("Cookie")),
    ETAG("ETag", new HttpString("ETag")),
    IF_NONE_MATCH("If-None-Match", new HttpString("If-None-Match")),
    LOCATION("Location", new HttpString("Location")),
    REFERER_POLICY("Referrer-Policy", new HttpString("Referrer-Policy")),
    SERVER("Server", new HttpString("Server")),
    WWW_AUTHENTICATE("WWW-Authenticate", new HttpString("WWW-Authenticate")),
    X_CONTENT_TYPE_OPTIONS("X-Content-Type-Options", new HttpString("X-Content-Type-Options")),
    X_FORWARDED_FOR("X-Forwarded-For", new HttpString("X-Forwarded-For")),
    X_FRAME_OPTIONS("X-Frame-Options", new HttpString("X-Frame-Options")),
    X_XSS_PPROTECTION("X-XSS-Protection", new HttpString("X-XSS-Protection")),
    X_RATELIMIT("X-RateLimit", new HttpString("X-RateLimit")),
    X_RATELIMIT_REMAINING("X-RateLimit-Remaining", new HttpString("X-RateLimit-Remaining"));

    private final HttpString httpString;
    private final String value;

    Header (String value, HttpString httpString) {
        this.value = value;
        this.httpString = httpString;
    }

    public HttpString toHttpString() {
        return this.httpString;
    }

    @Override
    public String toString() {
        return this.value;
    }
}