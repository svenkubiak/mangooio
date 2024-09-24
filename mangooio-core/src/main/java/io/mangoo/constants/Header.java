package io.mangoo.constants;

import io.undertow.util.HttpString;

public final class Header {
    public static final HttpString ACCEPT_LANGUAGE = new HttpString("Accept-Language");
    public static final HttpString CONTENT_DISPOSITION = new HttpString("Content-Disposition");
    public static final HttpString CONTENT_SECURITY_POLICY = new HttpString("Content-Security-Policy");
    public static final HttpString CONTENT_TYPE = new HttpString("Content-Type");
    public static final HttpString COOKIE = new HttpString("Cookie");
    public static final HttpString FEATURE_POLICY = new HttpString("Feature-Policy");
    public static final HttpString LOCATION = new HttpString("Location");
    public static final HttpString REFERER_POLICY = new HttpString("Referrer-Policy");
    public static final HttpString SERVER = new HttpString("Server");
    public static final HttpString X_CONTENT_TYPE_OPTIONS = new HttpString("X-Content-Type-Options");
    public static final HttpString X_FRAME_OPTIONS = new HttpString("X-Frame-Options");
    public static final HttpString X_XSS_PROTECTION = new HttpString("X-XSS-Protection");
    public static final HttpString PERMISSIONS_POLICY = new HttpString("Permissions-Policy");

    private Header() {
    }
}
