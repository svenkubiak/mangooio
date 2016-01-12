package io.mangoo.enums;

/**
 * Key strings for reading configuration properties
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
    APPLICATION_TIMER("application.timer"),
    APPLICATION_ADMIN_HEALTH("application.admin.health"),
    APPLICATION_ADMIN_CACHE("application.admin.cache"),
    APPLICATION_ADMIN_ROUTES("application.admin.routes"),
    APPLICATION_ADMIN_CONFIG("application.admin.config"),
    APPLICATION_ADMIN_METRICS("application.admin.metrics"),
    APPLICATION_ADMIN_SYSTEM("application.admin.system"),
    APPLICATION_ADMIN_MEMORY("application.admin.memory"),
    APPLICATION_ADMIN_SCHEDULER("application.admin.scheduler"),
    APPLICATION_ADMIN_USERNAME("application.admin.username"),
    APPLICATION_ADMIN_PASSWORD("application.admin.password"),
    APPLICATION_CONTROLLER("application.controller"),
    AUTH_COOKIE_NAME("auth.cookie.name"),
    AUTH_REDIRECT("auth.redirect"),
    AUTH_COOKIE_ENCRYPT("auth.cookie.encrypt"),
    AUTH_COOKIE_EXPIRES("auth.cookie.expires"),
    AUTH_COOKIE_REMEMBER_EXPIRES("auth.cookie.remember.expires"),
    AUTH_COOKIE_SECURE("auth.cookie.secure"),
    AUTH_COOKIE_VERSION("auth.cookie.version"),
    CACHE_TYPE("cache.type"),
    COOKIE_NAME("cookie.name"),
    COOKIE_ENCRYPTION("cookie.encryption"),
    COOKIE_EXPIRES("cookie.expires"),
    COOKIE_SECURE("cookie.secure"),
    COOKIE_VERSION("cookie.version"),
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
    VALIDATION_NUMERIC("validation.numeric"),
    VERSION("version"),
    CACHE_MAX_SIZE("cache.maxsize"),
    CACHE_EVICTION("cache.eviction"),
    CACHE_EXPIRES("cache.expires"),
    CACHE_CLASS("cache.class"),
    OAUTH_TWITTER_KEY("oauth.twitter.key"),
    OAUTH_TWITTER_SECRET("oauth.twitter.secret"),
    OAUTH_TWITTER_CALLBACK("oauth.twitter.callback"),
    OAUTH_GOOGLE_KEY("oauth.google.key"),
    OAUTH_GOOGLE_SECRET("oauth.google.secret"),
    OAUTH_GOOGLE_CALLBACK("oauth.google.callback"),
    OAUTH_FACEBOOK_KEY("oauth.facebook.key"),
    OAUTH_FACEBOOK_SECRET("oauth.facebook.secret"),
    OAUTH_FACEBOOK_CALLBACK("oauth.facebook.callback"),
    SCHEDULER_AUTOSTART("scheduler.autostart"),
    SCHEDULER_PACKAGE("scheduler.package"),
    EXECUTION_THREADPOOL("execution.threadpool");

    private final String value;

    Key (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}