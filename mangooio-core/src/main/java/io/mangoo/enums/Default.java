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
    ADMIN_COOKIE_NAME("mangooio-admin"),
    APPLICATION_ADMIN_ENABLE(Constants.FALSE),
    APPLICATION_ADMIN_HEALTH_ENABLE(Constants.FALSE),
    APPLICATION_ADMIN_HEALTH_HEADER("mangooio-health"),
    APPLICATION_CONTROLLER("controllers."),
    APPLICATION_HEADERS_CONTENTSECURITYPOLICY(""),
    APPLICATION_HEADERS_FEATUREPOLICY(""),
    APPLICATION_HEADERS_REFERERPOLICY("no-referrer"),
    APPLICATION_HEADERS_SERVER("Undertow"),
    APPLICATION_HEADERS_XCONTENTTYPEOPTIONS("nosniff"),
    APPLICATION_HEADERS_XFRAMEOPTIONS("DENY"),
    APPLICATION_HEADERS_XSSPROTECTION("1"),
    APPLICATION_LANGUAGE("en"),
    APPLICATION_TEMPLATEENGINE("io.mangoo.templating.TemplateEngineFreemarker"),
    APPLICATION_THREADPOOL("10"),
    AUTHENTICATION_COOKIE_EXPIRES(Constants.FALSE),
    AUTHENTICATION_COOKIE_NAME("mangooio-auth"),
    AUTHENTICATION_COOKIE_REMEMBER_EXPIRES("1209600"),
    AUTHENTICATION_COOKIE_SECURE(Constants.FALSE),
    AUTHENTICATION_COOKIE_TOKEN_EXPIRES("60"),
    AUTHENTICATION_LOCK("10"),
    AUTHENTICITY("authenticity"),
    BUNDLE_NAME("translations/messages"),
    CACHE_CLUSTER_ENABLE(Constants.FALSE),
    CONFIG_PATH("/src/main/resources/config.props"),
    CONFIGURATION_FILE("config.props"),
    CONTENT_TYPE("text/html; charset=UTF-8"),
    CORS_ALLOWORIGIN("^http(s)?://(www\\.)?example\\.(com|org)$"),
    CORS_ENABLE(Constants.FALSE),
    CORS_HEADERS_ALLOWCREDENTIALS(Constants.TRUE),
    CORS_HEADERS_ALLOWHEADERS("Authorization,Content-Type,Link,X-Total-Count,Range"),
    CORS_HEADERS_ALLOWMETHODS("DELETE,GET,HEAD,OPTIONS,PATCH,POST,PUT"),
    CORS_HEADERS_EXPOSEHEADERS("Accept-Ranges,Content-Length,Content-Range,ETag,Link,Server,X-Total-Count"),
    CORS_HEADERS_MAXAGE("864000"),
    CORS_URLPATTERN("^http(s)?://([^/]+)(:([^/]+))?(/([^/])+)?/api(/.*)?$"),
    DEFAULT_TEMPLATES_DIR("/templates/defaults/"),
    ENCODING("UTF-8"),
    FILES_FOLDER("files"),
    FILES_PATH("src/main/resources/files/"),
    FILTER_METHOD("execute"),
    FLASH_COOKIE_NAME("mangooio-flash"),
    I18N_COOKIE_NAME("mangooio-i18n"),
    JAVASCRIPT_FOLDER("javascript"),
    JBCRYPT_ROUNDS("12"),
    LOGO_FILE("logo.txt"),
    METRICS_ENABLE(Constants.FALSE),
    MODEL_CONF("model.conf"),
    MODULE_CLASS("app.Module"),
    NUMBER_FORMAT("0.######"),
    PERSISTENCE_MONGO_AUTH(Constants.FALSE),
    PERSISTENCE_MONGO_DBNAME("mangoo-io-mongodb"),
    PERSISTENCE_MONGO_EMBEDDED(Constants.FALSE),
    PERSISTENCE_MONGO_HOST("localhost"),
    PERSISTENCE_MONGO_PACKAGE("models"),
    PERSISTENCE_MONGO_PORT("27017"),
    PERSISTENCE_PREFIX("persistence."),
    POLICY_CSV("policy.csv"),
    SCHEDULER_ANNOTATION("io.mangoo.annotations.Schedule"),
    SCHEDULER_ENABLE(Constants.TRUE),
    SCHEDULER_POOSLIZE("20"),
    SESSION_COOKIE_EXPIRES(Constants.FALSE),
    SESSION_COOKIE_NAME("mangooio-session"),
    SESSION_COOKIE_SECURE(Constants.FALSE),
    SESSION_COOKIE_TOKEN_EXPIRES("60"),
    SMTP_AUTHENTICATION(Constants.FALSE),
    SMTP_DEBUG(Constants.FALSE),
    SMTP_FROM("mangoo I/O <noreply@example.com>"),
    SMTP_HOST("localhost"),
    SMTP_PORT("25"),
    SMTP_PROTOCOL("smtps"),
    SMTP_SERVER_NAME("smtp"),
    SSE_CACHE_PREFIX("mangooio-sse-"),
    STYLESHEET_FOLDER("stylesheet"),
    TEMPLATES_FOLDER("/templates/"),
    UNDERTOW_MAX_ENTITY_SIZE("4194304"),
    VERSION_PROPERTIES("version.properties"),
    VERSION_UNKNOW("unknown"),
    WSS_CACHE_PREFIX("mangooio-wss-");

    private static class Constants {
        public static final String FALSE = "false";
        public static final String TRUE = "true";
        
        private Constants() {
        }
    }

    private static Map<String, String> messages = new HashMap<>();
    
    static {
        messages.put(Validation.REQUIRED_KEY.toString(), Validation.REQUIRED.toString());
        messages.put(Validation.MIN_LENGTH_KEY.toString(), Validation.MIN_LENGTH.toString());
        messages.put(Validation.MIN_VALUE_KEY.toString(), Validation.MIN_VALUE.toString());
        messages.put(Validation.MAX_LENGTH_KEY.toString(), Validation.MAX_LENGTH.toString());
        messages.put(Validation.MAX_VALUE_KEY.toString(), Validation.MAX_VALUE.toString());        
        messages.put(Validation.EXACT_MATCH_KEY.toString(), Validation.EXACT_MATCH.toString());
        messages.put(Validation.MATCH_KEY.toString(), Validation.MATCH.toString());
        messages.put(Validation.EMAIL_KEY.toString(), Validation.EMAIL.toString());
        messages.put(Validation.IPV4_KEY.toString(), Validation.IPV4.toString());
        messages.put(Validation.IPV6_KEY.toString(), Validation.IPV6.toString());
        messages.put(Validation.RANGE_LENGTH_KEY.toString(), Validation.RANGE_LENGTH.toString());
        messages.put(Validation.RANGE_VALUE_KEY.toString(), Validation.RANGE_VALUE.toString());
        messages.put(Validation.URL_KEY.toString(), Validation.URL.toString());
        messages.put(Validation.MATCH_VALUES_KEY.toString(), Validation.MATCH_VALUES.toString());
        messages.put(Validation.REGEX_KEY.toString(), Validation.REGEX.toString());
        messages.put(Validation.NUMERIC_KEY.toString(), Validation.NUMERIC.toString());
        messages.put(Validation.DOMAIN_NAME_KEY.toString(), Validation.DOMAIN_NAME.toString());
    }
    public static Map<String, String> getMessages() {
        return messages;
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
    
    @Override
    public String toString() {
        return this.value;
    }
}