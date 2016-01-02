package io.mangoo.enums;

import io.undertow.util.HttpString;

/**
 * Custom headers which are not part of undertow
 *
 * @author svenkubiak
 *
 */
public enum Header {
    X_XSS_PPROTECTION("X-XSS-Protection"),
    X_CONTENT_TYPE_OPTIONS("X-Content-Type-Options"),
    X_FRAME_OPTIONS("X-Frame-Options"),
    X_RESPONSE_TIME("X-Response-Time");

    private final String value;

    Header (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public HttpString toHttpString() {
        return new HttpString(this.value);
    }
}