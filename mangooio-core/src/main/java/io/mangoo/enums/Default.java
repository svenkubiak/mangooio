package io.mangoo.enums;

/**
 * Default application values
 *
 * @author svenkubiak
 * @author williamdunne
 *
 */
public enum Default {
    LANGUAGE("en"),
    ENCODING("UTF-8"),
    DATA_DELIMITER("#"),
    DELIMITER("|"),
    FLASH_COOKIE_NAME("MANGOOIO-FLASH"),
    AUTH_COOKIE_NAME("MANGOOIO-AUTH"),
    COOKIE_EXPIRES("86400"),
    LOCALHOST("127.0.0.1"), //NOSONAR
    JBCRYPT_ROUNDS("12"),
    BUNDLE_NAME("translations/messages"),
    ASSETS_PATH("src/main/resources/files/assets/"),
    FILES_PATH("src/main/resources/files/"),
    CONFIG_PATH("/src/main/resources/application.yaml"),
    STYLESHEET_FOLDER("stylesheet"),
    JAVASCRIPT_FOLDER("javascript"),
    CONFIGURATION_FILE("application.yaml"),
    DEFAULT_CONFIGURATION("default"),
    VERSION_PROPERTIES("version.properties"),
    LOGO_FILE("logo.txt"),
    CONTENT_TYPE("text/html; charset=UTF-8"),
    SCHEDULER_PREFIX("org.quartz."),
    APPLICATION_SECRET_MIN_LENGTH("32"),
    SERVER("Undertow"),
    TEMPLATES_FOLDER("/templates/"),
    AUTH_COOKIE_EXPIRES("3600"),
    COOKIE_NAME("MANGOOIO-SESSION"),
    COOKIE_I18N_NAME("MANGOOIO-I18N"),
    SPLITTER("&"),
    SEPERATOR(":"),
    APPLICATION_HEADERS_XCONTENTTYPEOPTIONS("nosniff"),
    APPLICATION_HEADERS_XFRAMEOPTIONS("DENY"),
    FILTER_METHOD("execute"),
    AUTHENTICITY_TOKEN("authenticityToken"),
    XSS_PROTECTION("1"),
    FILES_FOLDER("files"),
    MODULE_CLASS("conf.Module"),
    VERSION("unknown"),
    LOGBACK_PROD_FILE("logback.prod.xml"),
    NUMBER_FORMAT("0.######"),
    DEFAULT_TEMPLATES_DIR("/templates/defaults/"),
    COOKIE_SECURE("false"), //NOSONAR
    AUTH_COOKIE_SECURE("false"), //NOSONAR
    APPLICATION_TEST_MASTERKEY("f8%q8G6Px8vxn7Tl%2P40vyT9e8KeTJ9"),
    APPLICATION_ADMIN_URL("/@admin/{space}"),
    APPLICATION_TIMER("false"),
    APPLICATION_HEADERS_CONTENTSECURITYPOLICY(""),
    APPLICATION_HEADERS_XSSPROTECTION("1"),
    APPLICATION_JWT_ENCRYPT("false"),
    APPLICATION_CONTROLLER("controllers."),
    APPLICATION_HEADERS_SERVER("Undertow"),
    AUTH_COOKIE_ENCRYPT("false"),
    AUTH_COOKIE_VERSION("0"),
    AUTH_COOKIE_REMEMBER_EXPIRES("1209600"),
    COOKIE_VERSION("0"),
    SCHEDULER_AUTOSTART("true"),
    SCHEDULER_JOB_GROUP("MangooSchedulerJobGroup"),
    SCHEDULER_TRIGGER_GROUP("MangooSchedulerTriggerGroup"),
    BASICAUTH_CREDENTIALS_LENGTH("2"),
    OAUTH_REQUEST_PARAMETER("oauth"),
    SCHEDULER_PACKAGE("jobs"),
    COOKIE_ENCRYPTION("false"),
    EXECUTION_THREADPOOL("10"),
    ROUTES_FILE("routes.yaml"),
    SSE_CACHE_PREFIX("MANGOOIO-SSE-"),
    WSS_CACHE_PREFIX("MANGOOIO-WSS-"),
    AUTHENTICATION("@authentication"),
    BLOCKING("@blocking"),
    VALUE_REQUIRED("For a new cache entry a non null value is required"),
    KEY_REQUIRED("For a new cache entry a non null key is required"),
    TEMPLATE_ENGINE_CLASS("io.mangoo.templating.freemarker.TemplateEngineFreemarker"),
    UNDERTOW_MAX_ENTITY_SIZE("4194304"),
    SMTP_PORT("25"),
    SMTP_SSL("false"),
    SMTP_HOST("localhost"),
    SMTP_SERVER_NAME("smtp"),
    SMTP_FROM("mangoo I/O application <noreply@example.com>"),
    CACHE_CLUSTER_ENABLE("false");

    private final String value;

    Default (String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public int toInt() {
        return Integer.parseInt(this.value);
    }

    public long toLong() {
        return Long.parseLong(this.value);
    }

    public boolean toBoolean() {
        return Boolean.parseBoolean(this.value);
    }
}