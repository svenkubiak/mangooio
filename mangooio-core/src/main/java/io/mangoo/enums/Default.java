package io.mangoo.enums;

/**
 * Default application values
 *
 * @author svenkubiak
 * @author williamdunne
 *
 */
public enum Default {
    APPLICATION_ADMIN_CACHE("false"),
    APPLICATION_ADMIN_CONFIG("false"),
    APPLICATION_ADMIN_HEALTH("false"),
    APPLICATION_ADMIN_MEMORY("false"),
    APPLICATION_ADMIN_METRICS("false"),
    APPLICATION_ADMIN_ROUTES("false"),
    APPLICATION_ADMIN_SCHEDULER("false"),
    APPLICATION_ADMIN_SYSTEM("false"),
    APPLICATION_CONTROLLER("controllers."),
    APPLICATION_HOST("127.0.0.1"), //NOSONAR
    APPLICATION_PORT("8080"),
    APPLICATION_SECRET_MIN_LENGTH("16"),
    APPLICATION_TIMER("false"),
    ASSETS_PATH("src/main/resources/files/assets/"),
    AUTHENTICATION("@authentication"),
    AUTHENTICITY_TOKEN("authenticityToken"),
    AUTH_COOKIE_ENCRYPT("false"),
    AUTH_COOKIE_EXPIRES("3600"),
    AUTH_COOKIE_NAME("MANGOOIO-AUTH"),
    AUTH_COOKIE_REMEMBER_EXPIRES("1209600"),
    AUTH_COOKIE_SECURE("false"), //NOSONAR
    AUTH_COOKIE_VERSION("0"),
    BASICAUTH_CREDENTIALS_LENGTH("2"),
    BLOCKING("@blocking"),
    BUNDLE_NAME("translations/messages"),
    CACHE_CLASS("io.mangoo.cache.Cache"),
    CACHE_EXPIRES("afterAccess"),
    CACHE_EXPIRES_ACCESS("3600"),
    CACHE_EXPIRES_WRITE("3600"),
    CACHE_MAX_SIZE("5000"),
    CACHE_NAME("mangooio"),
    CONFIGURATION_FILE("application.yaml"),
    CONFIG_PATH("/src/main/resources/application.yaml"),
    CONTENT_TYPE("text/html; charset=UTF-8"),
    COOKIE_ENCRYPTION("false"),
    COOKIE_EXPIRES("86400"),
    COOKIE_NAME("MANGOOIO-SESSION"),
    COOKIE_SECURE("false"), //NOSONAR
    COOKIE_VERSION("0"),
    DATA_DELIMITER("#"),
    DEFAULT_CONFIGURATION("default"),
    DEFAULT_TEMPLATES_DIR("/templates/defaults/"),
    DELIMITER("|"),
    EXCEPTION_TEMPLATE_NAME("exception.ftl"),
    EXECUTION_THREADPOOL("10"),
    FILES_FOLDER("files"),
    FILTER_METHOD("execute"),
    FLASH_COOKIE_NAME("MANGOOIO-FLASH"),
    JAVSCRIPT_FOLDER("javascripts"),
    JBCRYPT_ROUNDS("12"),
    LANGUAGE("en"),
    LOCALE_COOKIE_NAME("lang"),
    LOCALHOST("127.0.0.1"), //NOSONAR
    LOGBACK_PROD_FILE("logback.prod.xml"),
    LOGO_FILE("logo.txt"),
    MODULE_CLASS("conf.Module"),
    NOSNIFF("nosniff"),
    NUMBER_FORMAT("0.######"),
    OAUTH_REQUEST_PARAMETER("oauth"),
    ROUTES_FILE("routes.yaml"),
    SAMEORIGIN("SAMEORIGIN"),
    SCHEDULER_AUTOSTART("true"),
    SCHEDULER_JOB_GROUP("MangooSchedulerJobGroup"),
    SCHEDULER_PACKAGE("jobs"),
    SCHEDULER_PREFIX("org.quartz."),
    SCHEDULER_TRIGGER_GROUP("MangooSchedulerTriggerGroup"),
    SEPERATOR(":"),
    SERVER("Undertow"),
    SPLITTER("&"),
    SSE_CACHE_PREFIX("MANGOOIO-SSE-"),
    STYLESHEET_FOLDER("stylesheets"),
    TEMPLATES_FOLDER("/templates/"),
    TEMPLATE_SUFFIX(".ftl"),
    VERSION("unknown"),
    VERSION_PROPERTIES("version.properties"),
    WSS_CACHE_PREFIX("MANGOOIO-WSS-"),
    XSS_PROTECTION("1"),
    X_XSS_PPROTECTION("1");

    private final String value;

    Default (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public int toInt() {
        return Integer.valueOf(this.value);
    }

    public long toLong() {
        return Long.valueOf(this.value);
    }

    public boolean toBoolean() {
        return Boolean.valueOf(this.value);
    }
}