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
    APPLICATION_ADMINISTRATION("application.administration"),
    AUTH_COOKIE_NAME("auth.cookie.name"),
    AUTH_REDIRECT("auth.redirect"),
    AUTH_COOKIE_ENCRYPT("auth.cookie.encrypt"),
    AUTH_COOKIE_EXPIRES("auth.cookie.expires"),
    AUTH_COOKIE_SECURE("auth.cookie.secure"),
    CACHE_TYPE("cache.class"),
    SMTP_HOST("smtp.host"),
    SMTP_PORT("smtp.port"),
    SMTP_USERNAME("smtp.username"),
    SMTP_PASSWORD("smtp.password"),
    SMTP_SSL("smtp.ssl"),
    COOKIE_NAME("cookie.name"),
    COOKIE_ENCRYPTION("cookie.encryption"),
    COOKIE_EXPIRES("cookie.expires"),
    COOKIE_SECURE("cookie.secure"),
    ERROR("error"),
    WARNING("warning"),
    SUCCESS("success"),
    VALIDATION_REQUIRED("validation.required"),
    VALIDATION_MIN("validation.min"),
    VALIDATION_MAX("validation.max"),
    VALIDATION_EXACT_MATCH("validation.exactMatch"),
    VALIDATION_MATCH("validation.match"),
    VALIDATION_EMAIL("validation.email"),
    VALIDATION_IPV4("validation.ipv4"),
    VALIDATION_IPV6("validation.ipv6"),
    VALIDATION_RANGE("validation.range"),
    VALIDATION_URL("validation.url"),
    VALIDATION_REGEX("validation.regex"),
    VERSION("version"),
    CACHE_MAX_SIZE("cache.maxsize"),
    CACHE_EXPIRES("cache.expires");

    private final String value;

    Key (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}