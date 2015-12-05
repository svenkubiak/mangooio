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
    /**
     * @return application.name from application.yaml
     */
    public static String getApplicationName() {
        return Application.getInstance(Config.class).getString(Key.APPLICATION_NAME);
    }

    /**
     * @return application.host from application.yaml
     */
    public static String getApplicationHost() {
        return Application.getInstance(Config.class).getString(Key.APPLICATION_HOST);
    }

    /**
     * @return appliction.port from application.yaml
     */
    public static int getApplicationPort() {
        return Application.getInstance(Config.class).getInt(Key.APPLICATION_PORT);
    }

    /**
     * @return default name of flash cookie name
     */
    public static String getFlashCookieName() {
        return Default.FLASH_COOKIE_NAME.toString();
    }

    /**
     * @return cookie.name from application.yaml or default value if undefined
     */
    public static String getSessionCookieName() {
        return Application.getInstance(Config.class).getString(Key.COOKIE_NAME, Default.COOKIE_NAME.toString());
    }

    /**
     * @return application.secret from application.yaml
     */
    public static String getApplicationSecret() {
        return Application.getInstance(Config.class).getString(Key.APPLICATION_SECRET);
    }

    /**
     * @return auth.cookie.name from application.yaml or default value if undefined
     */
    public static String getAuthenticationCookieName() {
        return Application.getInstance(Config.class).getString(Key.AUTH_COOKIE_NAME, Default.AUTH_COOKIE_NAME.toString());
    }

    /**
     * @return auth.cookie.expires from application.yaml or default value if undefined
     */
    public static long getAuthenticationExpires() {
        return Application.getInstance(Config.class).getLong(Key.AUTH_COOKIE_EXPIRES, Default.AUTH_COOKIE_EXPIRES.toLong());
    }

    /**
     * @return cookie.expires from application.yaml or default value if undefined
     */
    public static long getSessionExpires() {
        return Application.getInstance(Config.class).getLong(Key.COOKIE_EXPIRES, Default.COOKIE_EXPIRES.toLong());
    }

    /**
     * @return cookie.secure from application.yaml or default value if undefined
     */
    public static boolean isSessionCookieSecure() {
        return Application.getInstance(Config.class).getBoolean(Key.COOKIE_SECURE, Default.COOKIE_SECURE.toBoolean());
    }

    /**
     * @return auth.cookie.secure from application.yaml or default value if undefined
     */
    public static boolean isAuthenticationCookieSecure() {
        return Application.getInstance(Config.class).getBoolean(Key.AUTH_COOKIE_SECURE, Default.AUTH_COOKIE_SECURE.toBoolean());
    }

    /**
     * @return same value as isSessionCookieSecure()
     */
    public static boolean isFlashCookieSecure() {
        return isSessionCookieSecure();
    }

    /**
     * @return application.language from application.yaml or default value if undefined
     */
    public static String getApplicationLanguage() {
        return Application.getInstance(Config.class).getString(Key.APPLICATION_LANGUAGE, Default.LANGUAGE.toString());
    }

    /**
     * @return application.admin.health from application.yaml or default value if undefined
     */
    public static boolean isAdminHealthEnabled() {
        return Application.getInstance(Config.class).getBoolean(Key.APPLICATION_ADMIN_HEALTH, Default.APPLICATION_ADMIN_HEALTH.toBoolean());
    }

    /**
     * @return application.admin.routes from application.yaml or default value if undefined
     */
    public static boolean isAdminRoutesEnabled() {
        return Application.getInstance(Config.class).getBoolean(Key.APPLICATION_ADMIN_ROUTES, Default.APPLICATION_ADMIN_ROUTES.toBoolean());
    }

    /**
     * @return application.admin.cache from application.yaml or default value if undefined
     */
    public static boolean isAdminCacheEnabled() {
        return Application.getInstance(Config.class).getBoolean(Key.APPLICATION_ADMIN_CACHE, Default.APPLICATION_ADMIN_CACHE.toBoolean());
    }

    /**
     * @return application.admin.config from application.yaml or default value if undefined
     */
    public static boolean isAdminConfigEnabled() {
        return Application.getInstance(Config.class).getBoolean(Key.APPLICATION_ADMIN_CONFIG, Default.APPLICATION_ADMIN_CONFIG.toBoolean());
    }

    /**
     * @return application.admin.metrics from application.yaml or default value if undefined
     */
    public static boolean isAdminMetricsEnabled() {
        return Application.getInstance(Config.class).getBoolean(Key.APPLICATION_ADMIN_METRICS, Default.APPLICATION_ADMIN_METRICS.toBoolean());
    }

    /**
     * @return application.admin.scheduler from application.yaml or default value if undefined
     */
    public static boolean isAdminSchedulerEnabled() {
        return Application.getInstance(Config.class).getBoolean(Key.APPLICATION_ADMIN_SCHEDULER, Default.APPLICATION_ADMIN_SCHEDULER.toBoolean());
    }

    /**
     * @return auth.cookie.encrypt from application.yaml or default value if undefined
     */
    public static boolean isAuthenticationCookieEncrypt() {
        return Application.getInstance(Config.class).getBoolean(Key.AUTH_COOKIE_ENCRYPT, Default.AUTH_COOKIE_ENCRYPT.toBoolean());
    }

    /**
     * @return auth.cookie.version from application.yaml or default value if undefined
     */
    public static String getAuthCookieVersion() {
        return Application.getInstance(Config.class).getString(Key.AUTH_COOKIE_VERSION, Default.AUTH_COOKIE_VERSION.toString());
    }

    /**
     * @return cookie.version from application.yaml or default value if undefined
     */
    public static String getCookieVersion() {
        return Application.getInstance(Config.class).getString(Key.COOKIE_VERSION, Default.COOKIE_VERSION.toString());
    }

    /**
     * @return scheduler.autostart from application.yaml or default value if undefined
     */
    public static boolean isSchedulerAutostart() {
        return Application.getInstance(Config.class).getBoolean(Key.SCHEDULER_AUTOSTART, Default.SCHEDULER_AUTOSTART.toBoolean());
    }

    /**
     * @return true if application.admin.username and application.admin.password are note blank
     */
    public static boolean isAdminAuthenticationEnabled() {
        return StringUtils.isNotBlank(Application.getInstance(Config.class).getString(Key.APPLICATION_ADMIN_USERNAME)) && StringUtils.isNotBlank(Application.getInstance(Config.class).getString(Key.APPLICATION_ADMIN_PASSWORD));
    }

    /**
     * @return application.admin.username from application.yaml or null if undefined
     */
    public static String getAdminAuthenticationUser() {
        return Application.getInstance(Config.class).getString(Key.APPLICATION_ADMIN_USERNAME);
    }

    /**
     * @return application.admin.password from application.yaml or null if undefined
     */
    public static String getAdminAuthenticationPassword() {
        return Application.getInstance(Config.class).getString(Key.APPLICATION_ADMIN_PASSWORD);
    }

    /**
     * @return scheduler.package from application.yaml or default value if undefined
     */
    public static String getSchedulerPackage() {
        return Application.getInstance(Config.class).getString(Key.SCHEDULER_PACKAGE, Default.SCHEDULER_PACKAGE.toString());
    }

    /**
     * @return cookie.encryption from application.yaml or default value if undefined
     */
    public static boolean isSessionCookieEncrypt() {
        return Application.getInstance(Config.class).getBoolean(Key.COOKIE_ENCRYPTION, Default.COOKIE_ENCRYPTION.toBoolean());
    }

    /**
     * @return auth.cookie.remember.expires from application.yaml or default value if undefined
     */
    public static long getAuthenticationRememberExpires() {
        return Application.getInstance(Config.class).getLong(Key.AUTH_COOKIE_REMEMBER_EXPIRES, Default.AUTH_COOKIE_REMEMBER_EXPIRES.toLong());
    }

    /**
     * @return execution.threadpool from application.yaml or default value if undefined
     */
    public static int getExecutionPool() {
        return Application.getInstance(Config.class).getInt(Key.EXECUTION_THREADPOOL, Default.EXECUTION_THREADPOOL.toInt());
    }

    /**
     * @return application.controller from application.yaml or default value if undefined
     */
    public static String getControllerPackage() {
        return Application.getInstance(Config.class).getString(Key.APPLICATION_CONTROLLER, Default.APPLICATION_CONTROLLER.toString());
    }
}