package io.mangoo.enums;

import java.util.HashMap;
import java.util.Map;

/**
 * Default application values
 *
 * @author svenkubiak
 * @author williamdunne
 *
 */
public enum Default {
    APPLICATION_ADMIN_ENABLE(Constants.FALSE),
    APPLICATION_CONTROLLER("controllers."),
    APPLICATION_HEADERS_CONTENTSECURITYPOLICY(""),
    APPLICATION_HEADERS_REFERERPOLICY("no-referrer"),
    APPLICATION_HEADERS_SERVER("Undertow"),
    APPLICATION_HEADERS_XCONTENTTYPEOPTIONS("nosniff"),
    APPLICATION_HEADERS_XFRAMEOPTIONS("DENY"),
    APPLICATION_HEADERS_XSSPROTECTION("1"),
    APPLICATION_JWT_ENCRYPT(Constants.FALSE),
    APPLICATION_LANGUAGE("en"),
    APPLICATION_MINIFY_CSS(Constants.FALSE),
    APPLICATION_MINIFY_JS(Constants.FALSE),
    APPLICATION_PREPROCESS_LESS(Constants.FALSE),
    APPLICATION_PREPROCESS_SASS(Constants.FALSE),
    APPLICATION_SECRET_MIN_LENGTH("32"),
    APPLICATION_TEMPLATEENGINE("io.mangoo.templating.TemplateEngineFreemarker"),
    APPLICATION_TEST_MASTERKEY("f8%q8G6Px8vxn7Tl%2P40vyT9e8KeTJ9"),
    APPLICATION_THREADPOOL("10"),
    ASSETS_PATH("src/main/resources/files/assets/"),
    AUTHENTICATION_COOKIE_EXPIRES("0"),
    AUTHENTICATION_COOKIE_NAME("MANGOOIO-AUTH"),
    AUTHENTICATION_COOKIE_REMEMBER_EXPIRES("1209600"),
    AUTHENTICATION_COOKIE_SECURE(Constants.FALSE),
    AUTHENTICATION_LOCK("10"),
    AUTHENTICITY("authenticity"),
    BASICAUTH_CREDENTIALS_LENGTH("2"),
    BUNDLE_NAME("translations/messages"),
    CACHE_CLUSTER_ENABLE(Constants.FALSE),
    CONFIG_PATH("/src/main/resources/config.props"),
    CONFIGURATION_FILE("config.props"),
    CONTENT_TYPE("text/html; charset=UTF-8"),
    DATA_DELIMITER("#"),
    DEFAULT_CONFIGURATION("default"),
    DEFAULT_TEMPLATES_DIR("/templates/defaults/"),
    DELIMITER("|"),
    ENCODING("UTF-8"),
    FILES_FOLDER("files"),
    FILES_PATH("src/main/resources/files/"),
    FILTER_METHOD("execute"),
    FLASH_COOKIE_NAME("MANGOOIO-FLASH"),
    I18N_COOKIE_NAME("MANGOOIO-I18N"),
    JAVASCRIPT_FOLDER("javascript"),
    JBCRYPT_ROUNDS("12"),
    LIFECYCLE_CLASS("conf.Lifecycle"),
    LOGO_FILE("logo.txt"),
    METRICS_ENABLE(Constants.FALSE),
    MODEL_CONF("model.conf"),
    MODULE_CLASS("app.Module"),
    NUMBER_FORMAT("0.######"),
    POLICY_CSV("policy.csv"),
    ROUTES_FILE("routes.yaml"),
    SCHEDULER_ANNOTATION("io.mangoo.annotations.Schedule"),
    SCHEDULER_AUTOSTART("true"),
    SCHEDULER_ENABLE("true"),
    SCHEDULER_JOB_GROUP("MangooSchedulerJobGroup"),
    SCHEDULER_PACKAGE("jobs"),
    SCHEDULER_PREFIX("org.quartz."),
    SCHEDULER_TRIGGER_GROUP("MangooSchedulerTriggerGroup"),
    SESSION_COOKIE_EXPIRES("0"),
    SESSION_COOKIE_NAME("MANGOOIO-SESSION"),
    SESSION_COOKIE_SECURE(Constants.FALSE),
    SMTP_FROM("mangoo I/O <noreply@example.com>"),
    SMTP_HOST("localhost"),
    SMTP_PORT("25"),
    SMTP_SERVER_NAME("smtp"),
    SMTP_SSL(Constants.FALSE),
    SSE_CACHE_PREFIX("MANGOOIO-SSE-"),
    STYLESHEET_FOLDER("stylesheet"),
    TEMPLATES_FOLDER("/templates/"),
    UNDERTOW_MAX_ENTITY_SIZE("4194304"),
    VERSION_UNKNOW("unknown"),
    VERSION_PROPERTIES("version.properties"),
    WSS_CACHE_PREFIX("MANGOOIO-WSS-");

    private static Map<String, String> messages = new HashMap<>();
    static {
        messages.put(Validation.REQUIRED_KEY.name(), Validation.REQUIRED.toString());
        messages.put(Validation.MIN_KEY.name(), Validation.MIN.toString());
        messages.put(Validation.MAX_KEY.name(), Validation.MAX.toString());
        messages.put(Validation.EXACT_MATCH_KEY.name(), Validation.EXACT_MATCH.toString());
        messages.put(Validation.MATCH_KEY.name(), Validation.MATCH.toString());
        messages.put(Validation.EMAIL_KEY.name(), Validation.EMAIL.toString());
        messages.put(Validation.IPV4_KEY.name(), Validation.IPV4.toString());
        messages.put(Validation.IPV6_KEY.name(), Validation.IPV6.toString());
        messages.put(Validation.RANGE_KEY.name(), Validation.RANGE.toString());
        messages.put(Validation.URL_KEY.name(), Validation.URL.toString());
        messages.put(Validation.MATCH_VALUES_KEY.name(), Validation.MATCH_VALUES.toString());
        messages.put(Validation.REGEX_KEY.name(), Validation.REGEX.toString());
        messages.put(Validation.NUMERIC_KEY.name(), Validation.NUMERIC.toString());
        messages.put(Validation.DOMAIN_NAME_KEY.name(), Validation.DOMAIN_NAME.toString());
    }
    
    private static class Constants {
        public static final String FALSE = "false";
        
        private Constants() {
        }
    }
    
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
    
    public static Map<String, String> getMessages() {
        return messages;
    }
    
    @Override
    public String toString() {
        return this.value;
    }
}