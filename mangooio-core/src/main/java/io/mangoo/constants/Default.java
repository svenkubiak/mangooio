package io.mangoo.constants;

import java.util.HashMap;
import java.util.Map;

public final class Default {
    public static final String APPLICATION_ADMIN_COOKIE_NAME = "mangooio-admin";
    public static final Boolean APPLICATION_ADMIN_ENABLE = Boolean.FALSE;
    public static final String APPLICATION_ADMIN_LOCALE = "en_EN";
    public static final String APPLICATION_CONTROLLER = "controllers.";
    public static final String APPLICATION_LANGUAGE = "en";
    public static final String APPLICATION_NAME = "mangooio-application";
    public static final String AUTHENTICATION_COOKIE_NAME = "mangooio-auth";
    public static final long AUTHENTICATION_COOKIE_REMEMBER_EXPIRES = 2592000;
    public static final String AUTHENTICATION_COOKIE_SAME_SITE_MODE = "Strict";
    public static final Boolean AUTHENTICATION_COOKIE_SECURE = Boolean.FALSE;
    public static final long AUTHENTICATION_COOKIE_TOKEN_EXPIRES = 3600;
    public static final int AUTHENTICATION_LOCK = 10;
    public static final Boolean AUTHENTICATION_ORIGIN = Boolean.FALSE;
    public static final String BUNDLE_NAME = "translations/messages";
    public static final String CORS_ALLOW_ORIGIN = "^http(s)?://(www\\.)?example\\.(com|org)$";
    public static final Boolean CORS_ENABLE = Boolean.FALSE;
    public static final Boolean CORS_HEADERS_ALLOW_CREDENTIALS = Boolean.TRUE;
    public static final String CORS_HEADERS_ALLOW_HEADERS = "Authorization,Content-Type,Link,X-Total-Count,Range";
    public static final String CORS_HEADERS_ALLOW_METHODS = "DELETE,GET,HEAD,OPTIONS,PATCH,POST,PUT";
    public static final String CORS_HEADERS_EXPOSE_HEADERS = "Accept-Ranges,Content-Length,Content-Range,ETag,Link,Server,X-Total-Count";
    public static final String CORS_HEADERS_MAX_AGE = "864000";
    public static final String CORS_URL_PATTERN = "^http(s)?://([^/]+)(:([^/]+))?(/([^/])+)?/api(/.*)?$";
    public static final String CSRF_TOKEN = "x-csrf-token";
    public static final String FILES_FOLDER = "files";
    public static final String FLASH_COOKIE_NAME = "mangooio-flash";
    public static final String I18N_COOKIE_NAME = "mangooio-i18n";
    public static final String JAVASCRIPT_FOLDER = "javascript";
    public static final String LANGUAGE = "en";
    public static final Boolean METRICS_ENABLE = Boolean.FALSE;
    public static final Boolean PERSISTENCE_ENABLE = Boolean.TRUE;
    public static final Boolean PERSISTENCE_MONGO_AUTH = Boolean.FALSE;
    public static final Boolean PERSISTENCE_MONGO_EMBEDDED = Boolean.FALSE;
    public static final String PERSISTENCE_MONGO_DBNAME = "mangoo-io-mongodb";
    public static final String PERSISTENCE_MONGO_HOST = "localhost";
    public static final int PERSISTENCE_MONGO_PORT = 27017;
    public static final String PERSISTENCE_PREFIX = "persistence.";
    public static final Boolean SCHEDULER_ENABLE = Boolean.TRUE;
    public static final String SESSION_COOKIE_SAME_SITE_MODE = "Strict";
    public static final Boolean SESSION_COOKIE_EXPIRES = Boolean.FALSE;
    public static final String SESSION_COOKIE_NAME = "mangooio-session";
    public static final Boolean SESSION_COOKIE_SECURE = Boolean.FALSE;
    public static final long SESSION_COOKIE_TOKEN_EXPIRES = 3600;
    public static final Boolean SMTP_AUTHENTICATION = Boolean.FALSE;
    public static final Boolean SMTP_DEBUG = Boolean.FALSE;
    public static final String SMTP_FROM = "mangoo <noreply@mangoo.local>";
    public static final String SMTP_HOST = "localhost";
    public static final int SMTP_PORT = 25;
    public static final String SMTP_PROTOCOL = "smtps";
    public static final String STYLESHEET_FOLDER = "stylesheet";
    public static final long UNDERTOW_MAX_ENTITY_SIZE = 4194304L;

    private static final Map<String, String> messages = new HashMap<>();

    static {
        messages.put(Validation.REQUIRED_KEY, Validation.REQUIRED);
        messages.put(Validation.MIN_LENGTH_KEY, Validation.MIN_LENGTH);
        messages.put(Validation.MIN_VALUE_KEY, Validation.MIN_VALUE);
        messages.put(Validation.MAX_LENGTH_KEY, Validation.MAX_LENGTH);
        messages.put(Validation.MAX_VALUE_KEY, Validation.MAX_VALUE);
        messages.put(Validation.EXACT_MATCH_KEY, Validation.EXACT_MATCH);
        messages.put(Validation.MATCH_KEY, Validation.MATCH);
        messages.put(Validation.EMAIL_KEY, Validation.EMAIL);
        messages.put(Validation.IPV4_KEY, Validation.IPV4);
        messages.put(Validation.IPV6_KEY, Validation.IPV6);
        messages.put(Validation.RANGE_LENGTH_KEY, Validation.RANGE_LENGTH);
        messages.put(Validation.RANGE_VALUE_KEY, Validation.RANGE_VALUE);
        messages.put(Validation.URL_KEY, Validation.URL);
        messages.put(Validation.MATCH_VALUES_KEY, Validation.MATCH_VALUES);
        messages.put(Validation.REGEX_KEY, Validation.REGEX);
        messages.put(Validation.NUMERIC_KEY, Validation.NUMERIC);
        messages.put(Validation.DOMAIN_NAME_KEY, Validation.DOMAIN_NAME);
    }

    private Default() {
    }

    public static Map<String, String> getMessages() {
        return messages;
    }
}
