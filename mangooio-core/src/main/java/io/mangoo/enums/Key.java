package io.mangoo.enums;

public enum Key {
    APPLICATION_ADMIN_ENABLE("application.admin.enable"),
    APPLICATION_ADMIN_HEALTH_ENABLE("application.admin.health.enable"),
    APPLICATION_ADMIN_HEALTH_TOKEN("application.admin.health.token"),
    APPLICATION_ADMIN_PASSWORD("application.admin.password"),
    APPLICATION_ADMIN_SECRET("application.admin.secret"),
    APPLICATION_ADMIN_USERNAME("application.admin.username"),
    APPLICATION_CONFIG("application.config"),
    APPLICATION_CONTROLLER("application.controller"),
    APPLICATION_LANGUAGE("application.language"),
    APPLICATION_MODE("application.mode"),
    APPLICATION_NAME("application.name"),
    APPLICATION_PRIVATEKEY("application.privatekey"),
    APPLICATION_PUBLICKEY("application.publickey"),
    APPLICATION_SECRET("application.secret"),
    AUTHENTICATION_COOKIE_EXPIRES("authentication.cookie.expires"),
    AUTHENTICATION_COOKIE_NAME("authentication.cookie.name"),
    AUTHENTICATION_COOKIE_REMEMBER_EXPIRES("authentication.cookie.remember.expires"),
    AUTHENTICATION_COOKIE_SECRET("authentication.cookie.secret"),
    AUTHENTICATION_COOKIE_SECURE("authentication.cookie.secure"),
    AUTHENTICATION_COOKIE_TOKEN_EXPIRES("authentication.cookie.token.expires"),
    AUTHENTICATION_LOCK("authentication.lock"),
    AUTHENTICATION_REDIRECT("authentication.redirect"),
    CACHE_CLUSTER_ENABLE("cache.cluster.enable"),
    CACHE_CLUSTER_URL("cache.cluster.url"),
    CONNECTOR_AJP_HOST("connector.ajp.host"),
    CONNECTOR_AJP_PORT("connector.ajp.port"),
    CONNECTOR_HTTP_HOST("connector.http.host"),
    CONNECTOR_HTTP_PORT("connector.http.port"),
    CORS_ALLOW_ORIGIN("cors.alloworigin"),
    CORS_ENABLE("cors.enable"),
    CORS_HEADERS_ALLOW_CREDENTIALS("cors.headers.allowcredentials"),
    CORS_HEADERS_ALLOW_HEADERS("cors.headers.allowheaders"),
    CORS_HEADERS_ALLOW_METHODS("cors.headers.allowmethods"),
    CORS_HEADERS_EXPOSE_HEADERS("cors.headers.exposeheaders"),
    CORS_HEADERS_MAX_AGE("cors.headers.maxage"),
    CORS_URL_PATTERN("cors.urlpattern"),
    FLASH_COOKIE_NAME("flash.cookie.name"),
    FLASH_COOKIE_SECRET("flash.cookie.secret"),
    I18N_COOKIE_NAME("i18n.cookie.name"),
    MANGOOIO_WARNINGS("MANGOOIO-WARNINGS"),
    METRICS_ENABLE("metrics.enable"),
    PERSISTENCE_ENABLE("persistence.enable"),
    PERSISTENCE_MONGO_AUTH("mongo.auth"),
    PERSISTENCE_MONGO_AUTHDB("mongo.authdb"),
    PERSISTENCE_MONGO_DBNAME("mongo.dbname"),
    PERSISTENCE_MONGO_EMBEDDED("mongo.embedded"),
    PERSISTENCE_MONGO_HOST("mongo.host"),
    PERSISTENCE_MONGO_PASSWORD("mongo.password"),
    PERSISTENCE_MONGO_PORT("mongo.port"),
    PERSISTENCE_MONGO_USERNAME("mongo.username"),
    SCHEDULER_ENABLE("scheduler.enable"),
    SESSION_COOKIE_EXPIRES("session.cookie.expires"),
    SESSION_COOKIE_NAME("session.cookie.name"),
    SESSION_COOKIE_SECRET("session.cookie.secret"),
    SESSION_COOKIE_SECURE("session.cookie.secure"),
    SESSION_COOKIE_TOKEN_EXPIRES("session.cookie.token.expires"),
    SMTP_AUTHENTICATION("smtp.authentication"),
    SMTP_DEBUG("smtp.debug"),
    SMTP_FROM("smtp.from"),
    SMTP_HOST("smtp.host"),
    SMTP_PASSWORD("smtp.password"),
    SMTP_PORT("smtp.port"),
    SMTP_PROTOCOL("smtp.protocol"),
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