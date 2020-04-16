package io.mangoo.enums;

/**
 * Key strings for reading configuration properties of application
 *
 * @author svenkubiak
 *
 */
public enum Key {
    APPLICATION_ADMIN_ENABLE("application.admin.enable"),
    APPLICATION_ADMIN_PASSWORD("application.admin.password"),
    APPLICATION_ADMIN_SECRET("application.admin.secret"),
    APPLICATION_ADMIN_USERNAME("application.admin.username"),
    APPLICATION_CONFIG("application.config"),
    APPLICATION_CONTROLLER("application.controller"),
    APPLICATION_LANGUAGE("application.language"),
    APPLICATION_LOG("application.log"),
    APPLICATION_MINIFY_CSS("application.minify.css"),
    APPLICATION_MINIFY_JS("application.minify.js"),
    APPLICATION_MODE("application.mode"),
    APPLICATION_NAME("application.name"),
    APPLICATION_PRIVATEKEY("application.privatekey"),
    APPLICATION_PUBLICKEY("application.publickey"),
    APPLICATION_SECRET("application.secret"),
    APPLICATION_TEMPLATEENGINE("application.templateengine"),
    APPLICATION_THREADPOOL("application.threadpool"),
    AUTHENTICATION_COOKIE_ENCRYPTIONKEY("authentication.cookie.encryptionkey"),
    AUTHENTICATION_COOKIE_EXPIRES("authentication.cookie.expires"),
    AUTHENTICATION_COOKIE_NAME("authentication.cookie.name"),
    AUTHENTICATION_COOKIE_REMEMBER_EXPIRES("authentication.cookie.remember.expires"),
    AUTHENTICATION_COOKIE_SECURE("authentication.cookie.secure"),
    AUTHENTICATION_COOKIE_SIGNKEY("authentication.cookie.signkey"),
    AUTHENTICATION_LOCK("authentication.lock"),
    AUTHENTICATION_REDIRECT("authentication.redirect"),
    CACHE_CLUSTER_ENABLE("cache.cluster.enable"),
    CACHE_CLUSTER_URL("cache.cluster.url"),
    CONNECTOR_AJP_HOST("connector.ajp.host"),
    CONNECTOR_AJP_PORT("connector.ajp.port"),
    CONNECTOR_HTTP_HOST("connector.http.host"),
    CONNECTOR_HTTP_PORT("connector.http.port"),
    CORS_ALLOWORIGIN("cors.alloworigin"),
    CORS_ENABLE("cors.enable"),
    CORS_HEADERS_ALLOWCREDENTIALS("cors.headers.allowcredentials"),
    CORS_HEADERS_ALLOWHEADERS("cors.headers.allowheaders"),
    CORS_HEADERS_ALLOWMETHODS("cors.headers.allowmethods"),
    CORS_HEADERS_EXPOSEHEADERS("cors.headers.exposeheaders"),
    CORS_HEADERS_MAXAGE("cors.headers.maxage"),
    CORS_URLPATTERN("cors.urlpattern"),
    FLASH_COOKIE_ENCRYPTIONKEY("flash.cookie.encryptionkey"),
    FLASH_COOKIE_NAME("flash.cookie.name"),
    FLASH_COOKIE_SIGNKEY("flash.cookie.signkey"),
    I18N_COOKIE_NAME("i18n.cookie.name"),
    LOGGER_MESSAGE("logger.configuration.message"),
    MANGOOIO_WARNINGS("MANGOOIO-WARNINGS"),
    METRICS_ENABLE("metrics.enable"),
    PERSISTENCE_MONGO_AUTH("persistence.mongo.auth"),
    PERSISTENCE_MONGO_AUTHDB("persistence.mongo.authdb"),
    PERSISTENCE_MONGO_DBNAME("persistence.mongo.dbname"),
    PERSISTENCE_MONGO_EMBEDDED("persistence.mongo.embedded"),
    PERSISTENCE_MONGO_HOST("persistence.mongo.host"),
    PERSISTENCE_MONGO_PACKAGE("persistence.mongo.package"),
    PERSISTENCE_MONGO_PASSWORD("persistence.mongo.password"),
    PERSISTENCE_MONGO_PORT("persistence.mongo.port"),
    PERSISTENCE_MONGO_USERNAME("persistence.mongo.username"),
    SCHEDULER_AUTOSTART("scheduler.autostart"),
    SCHEDULER_ENABLE("scheduler.enable"),
    SCHEDULER_PACKAGE("scheduler.package"),
    SESSION_COOKIE_ENCRYPTIONKEY("session.cookie.encryptionkey"),
    SESSION_COOKIE_EXPIRES("session.cookie.expires"),
    SESSION_COOKIE_NAME("session.cookie.name"),
    SESSION_COOKIE_SECURE("session.cookie.secure"),
    SESSION_COOKIE_SIGNKEY("session.cookie.signkey"),
    SMTP_DEBUG("smtp.debug"),
    SMTP_FROM("smtp.from"),
    SMTP_HOST("smtp.host"),
    SMTP_PASSWORD("smtp.password"),
    SMTP_PLAINTEXTTLS("smtp.plaintexttls"),
    SMTP_PORT("smtp.port"),
    SMTP_SSL("smtp.ssl"),
    SMTP_STARTTLS("smtp.starttls"),
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