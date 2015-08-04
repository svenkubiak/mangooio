package io.mangoo.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.google.common.io.Resources;
import com.google.inject.Singleton;

import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
@SuppressWarnings({"rawtypes", "unchecked"})
public class Config {
    private static final Logger LOG = LoggerFactory.getLogger(Config.class);
    private Map<String, String> values = new HashMap<String, String>();

    public Config() {
        init(Default.CONFIGURATION_FILE.toString(), Application.getMode());
    }

    public Config(String configFile, Mode mode) {
        init(configFile, mode);
    }

    private void init(String configFile, Mode mode) {
        String configPath = System.getProperty(Key.APPLICATION_CONFIG.toString());

        Map map = null;
        try {
            if (StringUtils.isNotBlank(configPath)) {
                map = (Map) loadConfiguration(new FileInputStream(new File(configPath))); //NOSONAR
            } else {
                map = (Map) loadConfiguration(Resources.getResource(configFile).openStream());
            }
        } catch (IOException e) {
            LOG.error("Failed to load application.yaml", e);
        }

        if (map != null) {
            Map<String, Object> defaults = (Map<String, Object>) map.get(Default.DEFAULT_CONFIGURATION.toString());
            Map<String, Object> environment = (Map<String, Object>) map.get(mode.toString());

            load("", defaults);
            if (environment != null && !environment.isEmpty()) {
                load("", environment);
            }
        }
    }

    private Object loadConfiguration(InputStream inputStream) {
        Yaml yaml = new Yaml();
        return yaml.load(inputStream);
    }

    /**
     * Recursively iterates over the yaml file and flatting out the values
     *
     * @param parentKey The current key
     * @param map The map to iterate over
     */
    private void load(String parentKey, Map<String, Object> map) {
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();

            if (value instanceof Map) {
                load(parentKey + "." + key, (Map<String, Object>) value);
            } else {
                this.values.put(StringUtils.substringAfter(parentKey + "." + key, "."), (value == null) ? "" : String.valueOf(value));
            }
        }
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as String or null if the key is not configured
     */
    public String getString(String key) {
        return this.values.get(key);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as String or the passed defautlValue if the key is not configured
     */
    public String getString(String key, String defaultValue) {
        String value = this.values.get(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }

        return value;
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as int or 0 if the key is not configured
     */
    public int getInt(String key) {
        String value = this.values.get(key);
        if (StringUtils.isBlank(value)) {
            return 0;
        }

        return Integer.valueOf(value);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as long or 0 if the key is not configured
     */
    public long getLong(String key) {
        String value = this.values.get(key);
        if (StringUtils.isBlank(value)) {
            return 0;
        }

        return Long.valueOf(value);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as int or the passed defautlValue if the key is not configured
     */
    public long getLong(String key, long defaultValue) {
        String value = this.values.get(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }

        return Long.valueOf(value);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as int or the passed defautlValue if the key is not configured
     */
    public int getInt(String key, int defaultValue) {
        String value = this.values.get(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }

        return Integer.valueOf(value);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as boolean or false if the key is not configured
     */
    public boolean getBoolean(String key) {
        String value = this.values.get(key);
        if (StringUtils.isBlank(value)) {
            return false;
        }

        return Boolean.valueOf(value);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as boolean or the passed defautlValue if the key is not configured
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = this.values.get(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }

        return Boolean.valueOf(value);
    }

    /**
     * Retrieves a configuration value with the given key constant (e.g. Key.APPLICATION_NAME)
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as String or null if the key is not configured
     */
    public String getString(Key key) {
        return getString(key.toString());
    }

    /**
     * Retrieves a configuration value with the given key constant (e.g. Key.APPLICATION_NAME)
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as String or the passed defautlValue if the key is not configured
     */
    public String getString(Key key, String defaultValue) {
        return getString(key.toString(), defaultValue);
    }

    /**
     * Retrieves a configuration value with the given key constant (e.g. Key.APPLICATION_NAME)
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as long or null if the key is not configured
     */
    public long getLong(Key key) {
        return getLong(key.toString());
    }

    /**
     * Retrieves a configuration value with the given key constant (e.g. Key.APPLICATION_NAME)
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as long or the passed defautlValue if the key is not configured
     */
    public long getLong(Key key, long defaultValue) {
        return getLong(key.toString(), defaultValue);
    }

    /**
     * Retrieves a configuration value with the given key constant (e.g. Key.APPLICATION_NAME)
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as int or 0 if the key is not configured
     */
    public int getInt(Key key) {
        return getInt(key.toString());
    }

    /**
     * Retrieves a configuration value with the given key constant (e.g. Key.APPLICATION_NAME)
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as int or the passed defautlValue if the key is not configured
     */
    public int getInt(Key key, int defaultValue) {
        return getInt(key.toString(), defaultValue);
    }

    /**
     * Retrieves a configuration value with the given key constant (e.g. Key.APPLICATION_NAME)
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as boolean or false if the key is not configured
     */
    public boolean getBoolean(Key key) {
        return getBoolean(key.toString());
    }

    /**
     * Retrieves a configuration value with the given key constant (e.g. Key.APPLICATION_NAME)
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as boolean or the passed defautlValue if the key is not configured
     */
    public boolean getBoolean(Key key, boolean defaultValue) {
        return getBoolean(key.toString(), defaultValue);
    }

    /**
     * Checks if the application.conf stored in conf/application.conf contains an application
     * secret property (application.secret) that has at least 16 characters (128-Bit)
     *
     * @return True if the configuration contains an application.secret property with at least 16 characters
     */
    public boolean hasValidSecret() {
        String secret = getString(Key.APPLICATION_SECRET);
        return StringUtils.isNotBlank(secret) && secret.length() >= Default.APPLICATION_SECRET_MIN_LENGTH.toInt();
    }

    public Map<String, String> getAllConfigurations() {
        return this.values;
    }

    public String getApplicationName() {
        return getString(Key.APPLICATION_NAME);
    }

    public String getApplicationHost() {
        return getString(Key.APPLICATION_HOST);
    }

    public int getApplicationPort() {
        return getInt(Key.APPLICATION_PORT);
    }

    public String getSmtpHost() {
        return getString(Key.SMTP_HOST);
    }

    public int getSmtpPort() {
        return getInt(Key.SMTP_PORT);
    }

    public String getFlashCookieName() {
        return Default.FLASH_COOKIE_NAME.toString();
    }

    public String getSessionCookieName() {
        return getString(Key.COOKIE_NAME, Default.SESSION_COOKIE_NAME.toString());
    }

    public String getApplicationSecret() {
        return getString(Key.APPLICATION_SECRET);
    }

    public String getAuthenticationCookieName() {
        return getString(Key.AUTH_COOKIE_NAME, Default.AUTH_COOKIE_NAME.toString());
    }

    public long getAuthenticationExpires() {
        return getLong(Key.AUTH_COOKIE_EXPIRES, Default.AUTH_COOKIE_EXPIRES.toLong());
    }

    public long getSessionExpires() {
        return getLong(Key.COOKIE_EXPIRES, Default.COOKIE_EXPIRES.toLong());
    }

    public boolean getSessionCookieSecure() {
        return getBoolean(Key.COOKIE_SECURE, Default.COOKIE_SECURE.toBoolean());
    }

    public boolean getAuthenticationCookieSecure() {
        return getBoolean(Key.AUTH_COOKIE_SECURE, Default.AUTH_COOKIE_SECURE.toBoolean());
    }

    public boolean getFlashCookieSecure() {
        return getSessionCookieSecure();
    }

    public String getApplicationLanguage() {
        return getString(Key.APPLICATION_LANGUAGE, Default.LANGUAGE.toString());
    }
}