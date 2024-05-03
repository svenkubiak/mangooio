package io.mangoo.enums;

import org.apache.logging.log4j.util.Strings;

import java.util.HashMap;
import java.util.Map;

public enum Default {
    ADMIN_COOKIE_NAME("mangooio-admin"),
    APPLICATION_ADMIN_ENABLE(Constants.FALSE),
    APPLICATION_ADMIN_HEALTH_ENABLE(Constants.FALSE),
    APPLICATION_CONTROLLER("controllers."),
    APPLICATION_HEADERS_CONTENT_SECURITY_POLICY(Strings.EMPTY),
    APPLICATION_HEADERS_FEATURE_POLICY(Strings.EMPTY),
    APPLICATION_HEADERS_REFERER_POLICY("no-referrer"),
    APPLICATION_HEADERS_SERVER("Undertow"),
    APPLICATION_HEADERS_X_CONTENT_TYPE_OPTIONS("nosniff"),
    APPLICATION_HEADERS_X_FRAME_OPTIONS("DENY"),
    APPLICATION_HEADERS_XSS_PROTECTION("1"),
    APPLICATION_LANGUAGE("en"),
    AUTHENTICATION_COOKIE_EXPIRES(Constants.FALSE),
    AUTHENTICATION_COOKIE_NAME("mangooio-auth"),
    AUTHENTICATION_COOKIE_REMEMBER_EXPIRES("1209600"),
    AUTHENTICATION_COOKIE_SECURE(Constants.FALSE),
    AUTHENTICATION_COOKIE_TOKEN_EXPIRES("60"),
    AUTHENTICATION_LOCK("10"),
    BUNDLE_NAME("translations/messages"),
    CACHE_CLUSTER_ENABLE(Constants.FALSE),
    CONFIGURATION_FILE("config.props"),
    CONTENT_TYPE("text/html; charset=UTF-8"),
    CORS_ALLOW_ORIGIN("^http(s)?://(www\\.)?example\\.(com|org)$"),
    CORS_ENABLE(Constants.FALSE),
    CORS_HEADERS_ALLOW_CREDENTIALS(Constants.TRUE),
    CORS_HEADERS_ALLOW_HEADERS("Authorization,Content-Type,Link,X-Total-Count,Range"),
    CORS_HEADERS_ALLOW_METHODS("DELETE,GET,HEAD,OPTIONS,PATCH,POST,PUT"),
    CORS_HEADERS_EXPOSE_HEADERS("Accept-Ranges,Content-Length,Content-Range,ETag,Link,Server,X-Total-Count"),
    CORS_HEADERS_MAX_AGE("864000"),
    CORS_URL_PATTERN("^http(s)?://([^/]+)(:([^/]+))?(/([^/])+)?/api(/.*)?$"),
    DEFAULT_TEMPLATES_DIR("/templates/defaults/"),
    ENCODING("UTF-8"),
    FILES_FOLDER("files"),
    FILES_PATH("src/main/resources/files/"),
    FILTER_METHOD("execute"),
    FLASH_COOKIE_NAME("mangooio-flash"),
    I18N_COOKIE_NAME("mangooio-i18n"),
    JAVASCRIPT_FOLDER("javascript"),
    METRICS_ENABLE(Constants.FALSE),
    MODULE_CLASS("app.Module"),
    NUMBER_FORMAT("0.######"),
    PERSISTENCE_ENABLE(Constants.TRUE),
    PERSISTENCE_MONGO_AUTH(Constants.FALSE),
    PERSISTENCE_MONGO_DBNAME("mangoo-io-mongodb"),
    PERSISTENCE_MONGO_EMBEDDED(Constants.FALSE),
    PERSISTENCE_MONGO_HOST("localhost"),
    PERSISTENCE_MONGO_PORT("27017"),
    PERSISTENCE_PREFIX("persistence."),
    SCHEDULER_ENABLE(Constants.TRUE),
    SESSION_COOKIE_EXPIRES(Constants.FALSE),
    SESSION_COOKIE_NAME("mangooio-session"),
    SESSION_COOKIE_SECURE(Constants.FALSE),
    SESSION_COOKIE_TOKEN_EXPIRES("60"),
    SMTP_AUTHENTICATION(Constants.FALSE),
    SMTP_DEBUG(Constants.FALSE),
    SMTP_FROM("mangoo <noreply@mangoo.local>"),
    SMTP_HOST("localhost"),
    SMTP_PORT("25"),
    SMTP_PROTOCOL("smtps"),
    SMTP_SERVER_NAME("smtp"),
    STYLESHEET_FOLDER("stylesheet"),
    TEMPLATES_FOLDER("templates/"),
    UNDERTOW_MAX_ENTITY_SIZE("4194304"),
    VERSION_PROPERTIES("version.properties"),
    VERSION_UNKNOWN("unknown");

    private static class Constants {
        public static final String FALSE = "false";
        public static final String TRUE = "true";
        
        private Constants() {
        }
    }

    private static final Map<String, String> messages = new HashMap<>();
    
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