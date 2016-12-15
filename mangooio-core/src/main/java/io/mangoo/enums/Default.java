package io.mangoo.enums;

/**
 * Default application values
 *
 * @author svenkubiak
 * @author williamdunne
 *
 */
public enum Default {
    APPLICATION_CONTROLLER("controllers."),
    APPLICATION_HEADERS_CONTENTSECURITYPOLICY(""),
    APPLICATION_HEADERS_SERVER("Undertow"),
    APPLICATION_HEADERS_XCONTENTTYPEOPTIONS("nosniff"),
    APPLICATION_HEADERS_XFRAMEOPTIONS("DENY"),
    APPLICATION_HEADERS_XSSPROTECTION("1"),
    APPLICATION_JWT_ENCRYPT("false"),
    APPLICATION_SECRET_MIN_LENGTH("32"),
    APPLICATION_TEST_MASTERKEY("f8%q8G6Px8vxn7Tl%2P40vyT9e8KeTJ9"),
    ASSETS_PATH("src/main/resources/files/assets/"),
    AUTH_COOKIE_ENCRYPT("false"),
    AUTH_COOKIE_EXPIRES("3600"),
    AUTH_COOKIE_NAME("MANGOOIO-AUTH"),
    AUTH_COOKIE_REMEMBER_EXPIRES("1209600"),
    AUTH_COOKIE_SECURE("false"),
    AUTH_COOKIE_VERSION("0"),
    AUTH_LOCK("10"),
    AUTHENTICITY("authenticity"),
    BASICAUTH_CREDENTIALS_LENGTH("2"),
    BUNDLE_NAME("translations/messages"),
    CACHE_CLUSTER_ENABLE("false"),
    CONFIG_PATH("/src/main/resources/application.yaml"),
    CONFIGURATION_FILE("application.yaml"),
    CONTENT_TYPE("text/html; charset=UTF-8"),
    COOKIE_ENCRYPTION("false"),
    COOKIE_EXPIRES("86400"),
    COOKIE_I18N_NAME("MANGOOIO-I18N"),
    COOKIE_NAME("MANGOOIO-SESSION"),
    COOKIE_SECURE("false"),
    COOKIE_VERSION("0"),
    DATA_DELIMITER("#"),
    DEFAULT_CONFIGURATION("default"),
    DEFAULT_TEMPLATES_DIR("/templates/defaults/"),
    DELIMITER("|"), //NOSONAR
    ENCODING("UTF-8"), //NOSONAR
    EXECUTION_THREADPOOL("10"),
    FILES_FOLDER("files"),
    FILTER_METHOD("execute"),
    FILES_PATH("src/main/resources/files/"),
    FLASH_COOKIE_NAME("MANGOOIO-FLASH"),
    JAVASCRIPT_FOLDER("javascript"),
    JBCRYPT_ROUNDS("12"),
    LANGUAGE("en"),
    LOGO_FILE("logo.txt"),
    MODULE_CLASS("conf.Module"),
    NUMBER_FORMAT("0.######"),
    OAUTH_REQUEST_PARAMETER("oauth"),
    ROUTES_FILE("routes.yaml"),
    SCHEDULER_AUTOSTART("true"),
    SCHEDULER_JOB_GROUP("MangooSchedulerJobGroup"),
    SCHEDULER_PACKAGE("jobs"),
    SCHEDULER_PREFIX("org.quartz."),
    SCHEDULER_TRIGGER_GROUP("MangooSchedulerTriggerGroup"),
    SMTP_FROM("mangoo I/O application <noreply@example.com>"),
    SMTP_HOST("localhost"),
    SMTP_PORT("25"),
    SMTP_SERVER_NAME("smtp"),
    SMTP_SSL("false"),
    SSE_CACHE_PREFIX("MANGOOIO-SSE-"),
    STYLESHEET_FOLDER("stylesheet"),
    TEMPLATE_ENGINE_CLASS("io.mangoo.templating.freemarker.TemplateEngineFreemarker"),
    TEMPLATES_FOLDER("/templates/"),
    UNDERTOW_MAX_ENTITY_SIZE("4194304"),
    VERSION("unknown"),
    VERSION_PROPERTIES("version.properties"),
    WSS_CACHE_PREFIX("MANGOOIO-WSS-");

    private final String value;

    Default (String value) {
        this.value = value;
    }

    public boolean toBoolean() {
        return Boolean.parseBoolean(this.value);
    }

    public int toInt() {
        return Integer.parseInt(this.value);
    }

    public long toLong() {
        return Long.parseLong(this.value);
    }

    @Override
    public String toString() {
        return this.value;
    }
}