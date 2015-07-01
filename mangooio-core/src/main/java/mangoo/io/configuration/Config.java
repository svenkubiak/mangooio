package mangoo.io.configuration;

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

import mangoo.io.core.Application;
import mangoo.io.enums.Default;
import mangoo.io.enums.Key;
import mangoo.io.enums.Mode;

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

    public Config(String path, Mode mode) {
        init(path, mode);
    }

    private void init(String configFile, Mode mode) {
        String configPath = System.getProperty(Key.APPLICATION_CONFIG.toString());
        InputStream inputStream = null;
        try {
            Yaml yaml = new Yaml();
            Map map;
            if (StringUtils.isNotBlank(configPath)) {
                inputStream = new FileInputStream(new File(configPath));
                map = (Map) yaml.load(inputStream);
            } else {
                map = (Map) yaml.load(Resources.getResource(configFile).openStream());
            }
            Map<String, Object> defaults = (Map<String, Object>) map.get(Default.DEFAULT_CONFIGURATION.toString());
            Map<String, Object> environment = (Map<String, Object>) map.get(mode.toString());

            load("", defaults);
            if (environment != null && !environment.isEmpty()) {
                load("", environment);
            }
        } catch (IOException e) {
            LOG.error("Failed to load configuration from application.yaml", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    LOG.error("Failed to close inputstram while loading application.yaml from external config path", e);
                }
            }
        }
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
                this.values.put(StringUtils.substringAfter(parentKey + "." + key, "."), String.valueOf(value));
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
        return getString(Key.APPLICATION_NAME) + Default.FLASH_SUFFIX.toString();
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
}