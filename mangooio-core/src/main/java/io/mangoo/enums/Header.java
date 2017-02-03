package io.mangoo.enums;

import io.undertow.util.HttpString;

/**
 * Custom headers which are not part of undertow
 *
 * @author svenkubiak
 *
 */
public enum Header {
    CONTENT_SECURITY_POLICY("Content-Security-Policy"),
    REFERER_POLICY("Referrer-Policy"),
    X_CONTENT_TYPE_OPTIONS("X-Content-Type-Options"),
    X_FRAME_OPTIONS("X-Frame-Options"),
    X_RESPONSE_TIME("X-Response-Time"),
    X_XSS_PPROTECTION("X-XSS-Protection");

    private final String value;

    Header (String value) {
        this.value = value;
    }

    public HttpString toHttpString() {
        return new HttpString(this.value);
    }

    @Override
    public String toString() {
        return this.value;
    }
}