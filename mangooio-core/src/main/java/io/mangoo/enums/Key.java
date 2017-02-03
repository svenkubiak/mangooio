package io.mangoo.enums;

/**
 * Key strings for reading configuration properties
 *
 * @author svenkubiak
 *
 */
public enum Key {
    APPLICATION_ADMIN_ENABLE("application.admin.enable"),
    APPLICATION_ADMIN_PASSWORD("application.admin.password"),
    APPLICATION_ADMIN_USERNAME("application.admin.username"),
    APPLICATION_CONFIG("application.config"),
    APPLICATION_CONTROLLER("application.controller"),
    APPLICATION_HEADERS_CONTENTSECURITYPOLICY("application.headers.contentsecuritypolicy"),
    APPLICATION_HEADERS_REFERERPOLICY("application.headers.refererpolicy"),
    APPLICATION_HEADERS_SERVER("application.headers.server"),
    APPLICATION_HEADERS_XCONTENTTYPEOPTIONS("application.headers.xcontenttypeoptions"),
    APPLICATION_HEADERS_XFRAMEOPTIONS("application.headers.xframeoptions"),
    APPLICATION_HEADERS_XSSPROTECTION("application.headers.xssprotection"),
    APPLICATION_JWT_ENCRYPT("application.jwt.encrypt"),
    APPLICATION_JWT_ENCRYPTION_KEY("application.jwt.encryptionkey"),
    APPLICATION_JWT_SIGNKEY("application.jwt.signkey"),
    APPLICATION_LANGUAGE("application.language"),
    APPLICATION_LOG("application.log"),
    APPLICATION_MASTERKEY("application.masterkey"),
    APPLICATION_MINIFY_CSS("application.minify.css"),
    APPLICATION_MINIFY_JS("application.minify.js"),
    APPLICATION_MODE("application.mode"),
    APPLICATION_NAME("application.name"),
    APPLICATION_PREPROCESS_LESS("application.preprocess.less"),   
    APPLICATION_PREPROCESS_SASS("application.preprocess.sass"),
    APPLICATION_SECRET("application.secret"),
    APPLICATION_TEMPLATEENGINE("application.templateengine"),
    APPLICATION_THREADPOOL("application.threadpool"),
    AUTH_COOKIE_ENCRYPT("auth.cookie.encrypt"),
    AUTH_COOKIE_EXPIRES("auth.cookie.expires"),
    AUTH_COOKIE_NAME("auth.cookie.name"),
    AUTH_COOKIE_REMEMBER_EXPIRES("auth.cookie.remember.expires"),
    AUTH_COOKIE_SECURE("auth.cookie.secure"),
    AUTH_COOKIE_VERSION("auth.cookie.version"),
    AUTH_LOCK("auth.lock"),
    AUTH_REDIRECT("auth.redirect"),
    CACHE_CLUSTER_ENABLE("cache.cluster.enable"),
    CACHE_CLUSTER_URL("cache.cluster.url"),
    CONNECTOR_AJP_HOST("connector.ajp.host"),
    CONNECTOR_AJP_PORT("connector.ajp.port"),
    CONNECTOR_HTTP_HOST("connector.http.host"),
    CONNECTOR_HTTP_PORT("connector.http.port"),
    COOKIE_ENCRYPTION("cookie.encryption"),
    COOKIE_EXPIRES("cookie.expires"),
    COOKIE_I18N_NAME("cookie.i18n.name"),
    COOKIE_NAME("cookie.name"),
    COOKIE_SECURE("cookie.secure"),
    COOKIE_VERSION("cookie.version"),
    ERROR("error"),
    OAUTH_FACEBOOK_CALLBACK("oauth.facebook.callback"),
    OAUTH_FACEBOOK_KEY("oauth.facebook.key"),
    OAUTH_FACEBOOK_SECRET("oauth.facebook.secret"),
    OAUTH_GOOGLE_CALLBACK("oauth.google.callback"),
    OAUTH_GOOGLE_KEY("oauth.google.key"),
    OAUTH_GOOGLE_SECRET("oauth.google.secret"),
    OAUTH_TWITTER_CALLBACK("oauth.twitter.callback"),
    OAUTH_TWITTER_KEY("oauth.twitter.key"),
    OAUTH_TWITTER_SECRET("oauth.twitter.secret"),
    SCHEDULER_AUTOSTART("scheduler.autostart"),
    SCHEDULER_PACKAGE("scheduler.package"),
    SMTP_FROM("smtp.from"),
    SMTP_HOST("smtp.host"),
    SMTP_PASSWORD("smtp.password"),
    SMTP_PORT("smtp.port"),
    SMTP_SSL("smtp.ssl"),
    SMTP_USERNAME("smtp.username"),
    SUCCESS("success"),
    UNDERTOW_MAX_ENTITY_SIZE("undertow.maxentitysize"),
    VALIDATION_DOMAIN_NAME("validation.domainname"),
    VALIDATION_EMAIL("validation.email"),
    VALIDATION_EXACT_MATCH("validation.exactmatch"),
    VALIDATION_IPV4("validation.ipv4"),
    VALIDATION_IPV6("validation.ipv6"),
    VALIDATION_MATCH("validation.match"),
    VALIDATION_MATCH_VALUES("validation.matchvalues"),
    VALIDATION_MAX("validation.max"),
    VALIDATION_MIN("validation.min"),
    VALIDATION_NUMERIC("validation.numeric"),
    VALIDATION_RANGE("validation.range"),
    VALIDATION_REGEX("validation.regex"),
    VALIDATION_REQUIRED("validation.required"),
    VALIDATION_URL("validation.url"),
    VERSION("version"),
    WARNING("warning");

    private final String value;

    Key (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }
}