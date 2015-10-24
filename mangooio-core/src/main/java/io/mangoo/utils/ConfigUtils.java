package io.mangoo.utils;

import org.apache.commons.lang3.StringUtils;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;

/**
 * Utility class for easy access of configuration values
 * 
 * @author kubi
 *
 */
public final class ConfigUtils {
    private static Config config;
    
    private ConfigUtils() {
        config = Application.getInstance(Config.class);
    }

    /**
     * Checks if the application.conf stored in conf/application.conf contains an application
     * secret property (application.secret) that has at least 16 characters (128-Bit)
     *
     * @return True if the configuration contains an application.secret property with at least 16 characters
     */
    public static boolean hasValidSecret() {
        String secret = config.getString(Key.APPLICATION_SECRET);
        return StringUtils.isNotBlank(secret) && secret.length() >= Default.APPLICATION_SECRET_MIN_LENGTH.toInt();
    }
    
    public static String getApplicationName() {
        return config.getString(Key.APPLICATION_NAME);
    }

    public static String getApplicationHost() {
        return config.getString(Key.APPLICATION_HOST);
    }

    public static int getApplicationPort() {
        return config.getInt(Key.APPLICATION_PORT);
    }

    public static String getSmtpHost() {
        return config.getString(Key.SMTP_HOST);
    }

    public static int getSmtpPort() {
        return config.getInt(Key.SMTP_PORT);
    }

    public static String getFlashCookieName() {
        return Default.FLASH_COOKIE_NAME.toString();
    }

    public static String getSessionCookieName() {
        return config.getString(Key.COOKIE_NAME, Default.COOKIE_NAME.toString());
    }

    public static String getApplicationSecret() {
        return config.getString(Key.APPLICATION_SECRET);
    }

    public static String getAuthenticationCookieName() {
        return config.getString(Key.AUTH_COOKIE_NAME, Default.AUTH_COOKIE_NAME.toString());
    }

    public static long getAuthenticationExpires() {
        return config.getLong(Key.AUTH_COOKIE_EXPIRES, Default.AUTH_COOKIE_EXPIRES.toLong());
    }

    public static long getSessionExpires() {
        return config.getLong(Key.COOKIE_EXPIRES, Default.COOKIE_EXPIRES.toLong());
    }

    public static boolean isSessionCookieSecure() {
        return config.getBoolean(Key.COOKIE_SECURE, Default.COOKIE_SECURE.toBoolean());
    }

    public static boolean isAuthenticationCookieSecure() {
        return config.getBoolean(Key.AUTH_COOKIE_SECURE, Default.AUTH_COOKIE_SECURE.toBoolean());
    }

    public static boolean isFlashCookieSecure() {
        return isSessionCookieSecure();
    }

    public static String getApplicationLanguage() {
        return config.getString(Key.APPLICATION_LANGUAGE, Default.LANGUAGE.toString());
    }

    public static boolean isAdminHealthEnabled() {
        return config.getBoolean(Key.APPLICATION_ADMIN_HEALTH, Default.APPLICATION_ADMIN_HEALTH.toBoolean());
    }

    public static boolean isAdminRoutesEnabled() {
        return config.getBoolean(Key.APPLICATION_ADMIN_ROUTES, Default.APPLICATION_ADMIN_ROUTES.toBoolean());
    }

    public static boolean isAdminCacheEnabled() {
        return config.getBoolean(Key.APPLICATION_ADMIN_CACHE, Default.APPLICATION_ADMIN_CACHE.toBoolean());
    }

    public static boolean isAdminConfigEnabled() {
        return config.getBoolean(Key.APPLICATION_ADMIN_CONFIG, Default.APPLICATION_ADMIN_CONFIG.toBoolean());
    }

    public static boolean isAdminMetricsEnabled() {
        return config.getBoolean(Key.APPLICATION_ADMIN_METRICS, Default.APPLICATION_ADMIN_METRICS.toBoolean());
    }

    public static boolean isAdminSchedulerEnabled() {
        return config.getBoolean(Key.APPLICATION_ADMIN_SCHEDULER, Default.APPLICATION_ADMIN_SCHEDULER.toBoolean());
    }
    
    public static boolean isAuthenticationCookieEncrypt() {
        return config.getBoolean(Key.AUTH_COOKIE_ENCRYPT, Default.AUTH_COOKIE_ENCRYPT.toBoolean());
    }

    public static String getAuthCookieVersion() {
        return config.getString(Key.AUTH_COOKIE_VERSION, Default.AUTH_COOKIE_VERSION.toString());
    }

    public static String getCookieVersion() {
        return config.getString(Key.COOKIE_VERSION, Default.COOKIE_VERSION.toString());
    }

    public static boolean isSmtpSSL() {
        return config.getBoolean(Key.SMTP_SSL, Default.SMTP_SSL.toBoolean());
    }

    public static boolean isSchedulerAutostart() {
        return config.getBoolean(Key.SCHEDULER_AUTOSTART, Default.SCHEDULER_AUTOSTART.toBoolean());
    }

    public static boolean isAdminAuthenticationEnabled() {
        return StringUtils.isNotBlank(config.getString(Key.APPLICATION_ADMIN_USERNAME)) && StringUtils.isNotBlank(config.getString(Key.APPLICATION_ADMIN_PASSWORD));
    }

    public static String getAdminAuthenticationUser() {
        return config.getString(Key.APPLICATION_ADMIN_USERNAME);
    }
    
    public static String getAdminAuthenticationPassword() {
        return config.getString(Key.APPLICATION_ADMIN_PASSWORD);
    }

    public static String getSchedulerPackage() {
        return config.getString(Key.SCHEDULER_PACKAGE, Default.SCHEDULER_PACKAGE.toString());
    }

    public static boolean isSessionCookieEncrypt() {
        return config.getBoolean(Key.COOKIE_ENCRYPTION, false);
    }    
}