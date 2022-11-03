package io.mangoo.enums;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.undertow.util.HttpString;

public enum Header {
    ACCEPT_LANGUAGE("Accept-Language", new HttpString("Accept-Language")),
    CONTENT_DISPOSITION("Content-Disposition", new HttpString("Content-Disposition")),
    CONTENT_SECURITY_POLICY("Content-Security-Policy", new HttpString("Content-Security-Policy")),
    CONTENT_TYPE("Content-Type", new HttpString("Content-Type")),
    COOKIE("Cookie", new HttpString("Cookie")),
    FEATURE_POLICY("Feature-Policy", new HttpString("Feature-Policy")),
    LOCATION("Location", new HttpString("Location")),
    REFERER_POLICY("Referrer-Policy", new HttpString("Referrer-Policy")),
    SERVER("Server", new HttpString("Server")),
    X_CONTENT_TYPE_OPTIONS("X-Content-Type-Options", new HttpString("X-Content-Type-Options")),
    X_FORWARDED_FOR("X-Forwarded-For", new HttpString("X-Forwarded-For")),
    X_FRAME_OPTIONS("X-Frame-Options", new HttpString("X-Frame-Options")),
    X_XSS_PROTECTION("X-XSS-Protection", new HttpString("X-XSS-Protection"));

    private final HttpString httpString;
    private final String value;

    @SuppressFBWarnings(value = "squid:UnusedPrivateMethod")
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