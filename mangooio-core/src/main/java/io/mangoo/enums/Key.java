package io.mangoo.enums;

/**
 * Key strings for reading configuration properties
 *
 * @author svenkubiak
 *
 */
public enum Key {
    APPLICATION_CONFIG("application.config"),
    APPLICATION_MASTERKEY("application.masterkey"),
    APPLICATION_NAME("application.name"),
    APPLICATION_MINIFY_JS("application.minify.js"),
    APPLICATION_MINIFY_CSS("application.minify.css"),
    APPLICATION_SECRET("application.secret"),
    APPLICATION_PORT("application.port"),
    APPLICATION_HOST("application.host"),
    APPLICATION_LANGUAGE("application.language"),
    APPLICATION_MODE("application.mode"),
    APPLICATION_TIMER("application.timer"),
    APPLICATION_PREPROCESS_SASS("application.preprocess.sass"),
    APPLICATION_PREPROCESS_LESS("application.preprocess.less"),
    APPLICATION_ADMIN_ENABLE("application.admin.enable"),
    APPLICATION_ADMIN_USERNAME("application.admin.username"),
    APPLICATION_ADMIN_PASSWORD("application.admin.password"),
    APPLICATION_CONTROLLER("application.controller"),
    APPLICATION_THREADPOOL("application.threadpool"),
    APPLICATION_TEMPLATEENGINE("application.templateengine"),
    APPLICATION_JWT_SIGNKEY("application.jwt.signkey"),
    APPLICATION_JWT_ENCRYPT("application.jwt.encrypt"),
    APPLICATION_JWT_ENCRYPTION_KEY("application.jwt.encryptionkey"),
    SMTP_PORT("smtp.port"),
    SMTP_HOST("smtp.host"),
    SMTP_SSL("smtp.ssl"),
    SMTP_FROM("smtp.from"),
    SMTP_USERNAME("smtp.username"),
    SMTP_PASSWORD("smtp.password"),
    AUTH_COOKIE_NAME("auth.cookie.name"),
    AUTH_REDIRECT("auth.redirect"),
    AUTH_COOKIE_ENCRYPT("auth.cookie.encrypt"),
    AUTH_COOKIE_EXPIRES("auth.cookie.expires"),
    AUTH_COOKIE_REMEMBER_EXPIRES("auth.cookie.remember.expires"),
    AUTH_COOKIE_SECURE("auth.cookie.secure"),
    AUTH_COOKIE_VERSION("auth.cookie.version"),
    COOKIE_NAME("cookie.name"),
    COOKIE_I18N_NAME("cookie.i18n.name"),
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
    CACHE_ADDRESSES("cache.addresses"),
    CACHE_MAX_SIZE("cache.maxsize"),
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
    UNDERTOW_MAX_ENTITY_SIZE("undertow.maxentitysize");

    private final String value;

    Key (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}