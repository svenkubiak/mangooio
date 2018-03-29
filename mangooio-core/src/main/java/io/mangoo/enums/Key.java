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
    
    @Deprecated /** Use Jvm.APPLICATION_CONFIG instead **/
    APPLICATION_CONFIG("application.config"),
    APPLICATION_CONTROLLER("application.controller"),
    APPLICATION_HEADERS_CONTENTSECURITYPOLICY("application.headers.contentsecuritypolicy"),
    APPLICATION_HEADERS_REFERERPOLICY("application.headers.refererpolicy"),
    APPLICATION_HEADERS_SERVER("application.headers.server"),
    APPLICATION_HEADERS_XCONTENTTYPEOPTIONS("application.headers.xcontenttypeoptions"),
    APPLICATION_HEADERS_XFRAMEOPTIONS("application.headers.xframeoptions"),
    APPLICATION_HEADERS_XSSPROTECTION("application.headers.xssprotection"),
    
    @Deprecated /** Will be removed in 5.0.0 without replacement **/
    APPLICATION_JWT_ENCRYPT("application.jwt.encrypt"),
    
    @Deprecated /** Will be removed in 5.0.0 without replacement **/
    APPLICATION_JWT_ENCRYPTION_KEY("application.jwt.encryptionkey"),
    
    @Deprecated /** Will be removed in 5.0.0 without replacement **/
    APPLICATION_JWT_SIGNKEY("application.jwt.signkey"),
    
    APPLICATION_LANGUAGE("application.language"),
    
    @Deprecated /** Use Jvm.APPLICATION_LOG instead **/
    APPLICATION_LOG("application.log"),

    @Deprecated /** Use APPLICATION_MASTERKEY_FILE instead **/
    APPLICATION_MASTERKEY("application.masterkey"),
    APPLICATION_MASTERKEY_FILE("application.masterkeyfile"),
    APPLICATION_MINIFY_CSS("application.minify.css"),
    APPLICATION_MINIFY_JS("application.minify.js"),
    
    @Deprecated /** Use Jvm.APPLICATION_MODE instead **/
    APPLICATION_MODE("application.mode"),
    APPLICATION_NAME("application.name"),
    APPLICATION_PREPROCESS_LESS("application.preprocess.less"),   
    APPLICATION_PREPROCESS_SASS("application.preprocess.sass"),
    APPLICATION_SECRET("application.secret"),
    APPLICATION_TEMPLATEENGINE("application.templateengine"),
    APPLICATION_THREADPOOL("application.threadpool"),
    
    @Deprecated /** Use AUTHENTICATION_COOKIE_ENCRYPT instead **/
    AUTH_COOKIE_ENCRYPT("auth.cookie.encrypt"),
    
    @Deprecated /** Use AUTHENTICATION_COOKIE_EXPIRES instead **/
    AUTH_COOKIE_EXPIRES("auth.cookie.expires"),
    
    @Deprecated /** Use AUTHENTICATION_COOKIE_NAME instead **/
    AUTH_COOKIE_NAME("auth.cookie.name"),
    
    @Deprecated /** Use AUTHENTICATION_COOKIE_REMEMBER_EXPIRES instead **/
    AUTH_COOKIE_REMEMBER_EXPIRES("auth.cookie.remember.expires"),
    
    @Deprecated /** Use AUTHENTICATION_COOKIE_SECURE instead **/
    AUTH_COOKIE_SECURE("auth.cookie.secure"),
    
    @Deprecated /** Use AUTHENTICATION_COOKIE_VERSION instead **/
    AUTH_COOKIE_VERSION("auth.cookie.version"),
    
    @Deprecated /** Use AUTHENTICATION_LOCK instead **/
    AUTH_LOCK("auth.lock"),
    
    @Deprecated /** Use AUTHENTICATION_REDIRECT instead **/
    AUTH_REDIRECT("auth.redirect"),
    
    AUTHENTICATION_COOKIE_ENCRYPT("authentication.cookie.encrypt"),
    AUTHENTICATION_COOKIE_EXPIRES("authentication.cookie.expires"),
    AUTHENTICATION_COOKIE_NAME("authentication.cookie.name"),
    AUTHENTICATION_COOKIE_REMEMBER_EXPIRES("authentication.cookie.remember.expires"),
    AUTHENTICATION_COOKIE_SECURE("authentication.cookie.secure"),
    AUTHENTICATION_COOKIE_SIGNKEY("authentication.cookie.signkey"),
    AUTHENTICATION_COOKIE_ENCRYPTIONKEY("authentication.cookie.encryptionkey"),
    AUTHENTICATION_COOKIE_VERSION("authentication.cookie.version"),
    AUTHENTICATION_LOCK("authentication.lock"),
    AUTHENTICATION_REDIRECT("authentication.redirect"),
    CACHE_CLUSTER_ENABLE("cache.cluster.enable"),
    CACHE_CLUSTER_URL("cache.cluster.url"),
    CONNECTOR_AJP_HOST("connector.ajp.host"),
    CONNECTOR_AJP_PORT("connector.ajp.port"),
    CONNECTOR_HTTP_HOST("connector.http.host"),
    
    CONNECTOR_HTTP_PORT("connector.http.port"),
    
    @Deprecated /** Use SESSION_COOKIE_ENCRYPTION instead **/
    COOKIE_ENCRYPTION("cookie.encryption"),
    
    @Deprecated /** Use SESSION_COOKIE_ENCRYPTION instead **/
    COOKIE_EXPIRES("cookie.expires"),
    
    @Deprecated /** Use SESSION_COOKIE_NAME instead **/
    COOKIE_NAME("cookie.name"),
    
    @Deprecated /** Use SESSION_COOKIE_SECURE instead **/
    COOKIE_SECURE("cookie.secure"),
    
    @Deprecated /** Use SESSION_COOKIE_VERSION instead **/
    COOKIE_VERSION("cookie.version"),
    I18N_COOKIE_NAME("i18n.cookie.name"),
    LOGGER_MESSAGE("logger.configuration.message"),
    MANGOOIO_WARNINGS("MANGOOIO-WARNINGS"),
    METRICS_ENABLE("metrics.enable"),
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
    SESSION_COOKIE_ENCRYPTION("session.cookie.encryption"),
    SESSION_COOKIE_ENCRYPTIONKEY("session.cookie.encryptionkey"),
    SESSION_COOKIE_EXPIRES("session.cookie.expires"),
    SESSION_COOKIE_NAME("session.cookie.name"),
    SESSION_COOKIE_SECURE("session.cookie.secure"),
    SESSION_COOKIE_SIGNKEY("session.cookie.signkey"),
    SESSION_COOKIE_VERSION("session.cookie.version"),
    SMTP_FROM("smtp.from"),
    SMTP_HOST("smtp.host"),
    SMTP_PASSWORD("smtp.password"),
    SMTP_PORT("smtp.port"),
    SMTP_SSL("smtp.ssl"),
    SMTP_USERNAME("smtp.username"),
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