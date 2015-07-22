package io.mangoo.enums;

/**
 * Key strings for reading configuration propertie
 *
 * @author svenkubiak
 *
 */
public enum Key {
    APPLICATION_CONFIG("application.config"),
    APPLICATION_NAME("application.name"),
    APPLICATION_MINIFY_JS("application.minify.js"),
    APPLICATION_MINIFY_CSS("application.minify.css"),
    APPLICATION_MINIFY_JSFOLDER("application.minify.jsfolder"),
    APPLICATION_MINIFY_CSSFOLDER("application.minify.cssfolder"),
    APPLICATION_GZIP_JS("application.minify.gzipjs"),
    APPLICATION_GZIP_CSS("application.minify.gzipcss"),
    APPLICATION_SECRET("application.secret"),
    APPLICATION_PORT("application.port"),
    APPLICATION_HOST("application.host"),
    APPLICATION_LANGUAGE("application.language"),
    APPLICATION_MODE("application.mode"),
    AUTH_COOKIE_NAME("auth.cookie.name"),
    AUTH_REDIRECT("auth.redirect"),
    AUTH_COOKIE_ENCRYPT("auth.cookie.encrypt"),
    AUTH_COOKIE_EXPIRES("auth.cookie.expires"),
    SMTP_HOST("smtp.host"),
    SMTP_PORT("smtp.port"),
    SMTP_USERNAME("smtp.username"),
    SMTP_PASSWORD("smtp.password"),
    SMTP_SSL("smtp.ssl"),
    COOKIE_NAME("cookie.name"),
    COOKIE_ENCRYPTION("cookie.encryption"),
    COOKIE_EXPIRES("cookie.expires"),
    ERROR("error"),
    WARNING("warning"),
    SUCCESS("success"),
    FORM_REQUIRED("form.required"),
    FORM_MIN("form.min"),
    FORM_MAX("form.max"),
    FORM_EXACT_MATCH("form.exactMatch"),
    FORM_MATCH("form.match"),
    FORM_EMAIL("form.email"),
    FORM_IPV4("form.ipv4"),
    FORM_IPV6("form.ipv6"),
    FORM_RANGE("form.range"),
    FORM_URL("form.url"),
    VERSION("version");

    private final String value;

    Key (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}