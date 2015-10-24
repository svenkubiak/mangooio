package io.mangoo.enums;

/**
 * Default application values
 *
 * @author svenkubiak
 *
 */
public enum Default {
    LANGUAGE("en"),
    DATA_DELIMITER("#"),
    DELIMITER("|"),
    FLASH_COOKIE_NAME("MANGOOIO-FLASH"),
    AUTH_COOKIE_NAME("MANGOOIO-AUTH"),
    COOKIE_EXPIRES("86400"),
    LOCALHOST("127.0.0.1"), //NOSONAR
    APPLICATION_HOST("127.0.0.1"), //NOSONAR
    JBCRYPT_ROUNDS("12"),
    SMTP_PORT("25"),
    SMTP_SSL("false"), //NOSONAR
    APPLICATION_PORT("8080"),
    BUNDLE_NAME("translations/messages"),
    ASSETS_PATH("src/main/resources/files/assets/"),
    CONFIG_PATH("/src/main/resources/application.yaml"),
    FAKE_SMTP_PROTOCOL("smtp"),
    STYLESHEET_FOLDER("stylesheets"),
    JAVSCRIPT_FOLDER("javascripts"),
    CONFIGURATION_FILE("application.yaml"),
    DEFAULT_CONFIGURATION("default"),
    VERSION_PROPERTIES("version.properties"),
    CONTENT_TYPE("text/html; charset=UTF-8"),
    SCHEDULER_PREFIX("org.quartz."),
    APPLICATION_SECRET_MIN_LENGTH("16"),
    SERVER("Undertow"),
    CACHE_NAME("mangooio"),
    TEMPLATES_FOLDER("/templates/"),
    TEMPLATE_SUFFIX(".ftl"),
    AUTH_COOKIE_EXPIRES("3600"),
    COOKIE_NAME("MANGOOIO-SESSION"),
    SPLITTER("&"),
    SEPERATOR(":"),
    NOSNIFF("nosniff"),
    SAMEORIGIN("SAMEORIGIN"),
    FILTER_METHOD("execute"),
    AUTHENTICITY_TOKEN("authenticityToken"),
    XSS_PROTECTION("1"),
    ROUTES_CLASS("conf.Routes"),
    FILES_FOLDER("files"),
    MODULE_CLASS("conf.Module"),
    VERSION("unknown"),
    LOGBACK_PROD_FILE("logback.prod.xml"),
    NUMBER_FORMAT("0.######"),
    EXCEPTION_TEMPLATE_NAME("exception.ftl"),
    DEFAULT_TEMPLATES_DIR("/templates/defaults/"),
    X_XSS_PPROTECTION("1"),
    COOKIE_SECURE("false"), //NOSONAR
    CACHE_TYPE("ehcache"),
    AUTH_COOKIE_SECURE("false"), //NOSONAR
    EHCACHE("ehcache"),
    MEMCACHED("memcached"),
    HAZELCAST("hazelcast"),
    REDIS("redis"),
    CACHE_MAX_SIZE("5000"),
    CACHE_EXPIRES("3600"),
    APPLICATION_ADMIN_HEALTH("false"),
    APPLICATION_ADMIN_CACHE("false"),
    APPLICATION_ADMIN_CONFIG("false"),
    APPLICATION_ADMIN_ROUTES("false"),
    APPLICATION_ADMIN_METRICS("false"),
    APPLICATION_ADMIN_SCHEDULER("false"),
    AUTH_COOKIE_ENCRYPT("false"),
    AUTH_COOKIE_VERSION("0"),
    COOKIE_VERSION("0"),
    SCHEDULER_AUTOSTART("true"),
    SCHEDULER_JOB_GROUP("MangooSchedulerJobGroup"),
    SCHEDULER_TRIGGER_GROUP("MangooSchedulerTriggerGroup"),
    BASICAUTH_CREDENTIALS_LENGTH("2"),
    OAUTH_REQUEST_PARAMETER("oauth"),
    SCHEDULER_PACKAGE("jobs");

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