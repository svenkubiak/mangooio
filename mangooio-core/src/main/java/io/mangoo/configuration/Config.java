package io.mangoo.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import com.google.common.io.Resources;
import com.google.inject.Singleton;

import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;

/**
 * Main configuration class for all properties configured in application.yaml
 *
 * @author svenkubiak
 * @author williamdunne
 *
 */
@Singleton
@SuppressWarnings({"rawtypes", "unchecked"})
public class Config {
    private static final Logger LOG = LogManager.getLogger(Config.class);
    private final Map<String, String> values = new ConcurrentHashMap<>(16, 0.9f, 1);

    public Config() {
        prepare(Default.CONFIGURATION_FILE.toString(), Application.getMode());
    }

    public Config(String configFile, Mode mode) {
        Objects.requireNonNull(configFile, "configFile can not be null");
        Objects.requireNonNull(mode, "mode can not be null");

        prepare(configFile, mode);
    }

    private void prepare(String configFile, Mode mode) {
        final String configPath = System.getProperty(Key.APPLICATION_CONFIG.toString());

        Map map;
        if (StringUtils.isNotBlank(configPath)) {
            map = (Map) loadConfiguration(configPath, false);
        } else {
            map = (Map) loadConfiguration(configFile, true);
        }

        if (map != null) {
            final Map<String, Object> defaults = (Map<String, Object>) map.get(Default.DEFAULT_CONFIGURATION.toString());
            final Map<String, Object> environment = (Map<String, Object>) map.get(mode.toString());

            load("", defaults);
            if (environment != null && !environment.isEmpty()) {
                load("", environment);
            }
        }
    }

    private Object loadConfiguration(String path, boolean resource) {
        InputStream inputStream = null;
        try {
            if (resource) {
                inputStream = Resources.getResource(path).openStream();
            } else {
                inputStream = new FileInputStream(new File(path));
            }
        } catch (final IOException e) {
            LOG.error("Failed to load application.yaml", e);
        }

        Object object = null;
        if (inputStream != null) {
            final Yaml yaml = new Yaml();
            object = yaml.load(inputStream);

            IOUtils.closeQuietly(inputStream);
        }

        return object;
    }

    /**
     * Recursively iterates over the yaml file and flatting out the values
     *
     * @param parentKey The current key
     * @param map The map to iterate over
     */
    private void load(String parentKey, Map<String, Object> map) {
        for (final Map.Entry<String, Object> entry : map.entrySet()) {
            final String key = entry.getKey();
            final Object value = entry.getValue();

            if (key != null) {
                if (value instanceof Map) {
                    load(parentKey + "." + key, (Map<String, Object>) value);
                } else {
                    this.values.put(StringUtils.substringAfter(parentKey + "." + key, "."), (value == null) ? "" : String.valueOf(value));
                }
            }
        }
    }

    /**
     * Decrypts all encrypted config value
     */
    public void decrypt() {
        String key = null;
        Crypto crypto = null;
        
        for (Entry<String, String> entry : this.values.entrySet()) {
            if (isEncrypted(entry.getValue())) {
                if (StringUtils.isBlank(key)) {
                    key = getMasterKey();
                    crypto = Application.getInstance(Crypto.class);
                }
                
                if (StringUtils.isNotBlank(key) && crypto != null) {
                    String decryptedText = crypto.decrypt(StringUtils.substringBetween(entry.getValue(), "cryptex[", "]"), key);
                    if (StringUtils.isNotBlank(decryptedText)) {
                        this.values.put(entry.getKey(), decryptedText);                         
                    }
                }
            }
        }
    }

    /**
     * @return The master key for encrypted config value, returns a default value if in test mode
     */
    public String getMasterKey() {
        if (Application.inTestMode()) {
            return Default.APPLICATION_MASTERKEY.toString();
        }
        
        String key = null;
        try {
            key = FileUtils.readFileToString(new File(this.values.get(Key.APPLICATION_MASTERKEY.toString())), Default.ENCODING.toString());
        } catch (IOException e) {
            LOG.error("Failed to read master key", e);
        }
        
        return key;
    }

    /**
     * Checks if a value is encrypt by checking for the prefix crpytex
     * 
     * @param value The value to check
     * @return True if the value starts with cryptex, false othweise
     */
    public boolean isEncrypted(String value) {
        Objects.requireNonNull(value, "value can not be null");
        return value.startsWith("cryptex[");
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
        return this.values.getOrDefault(key, defaultValue);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as int or 0 if the key is not configured
     */
    public int getInt(String key) {
        final String value = this.values.get(key);
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
        final String value = this.values.get(key);
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
        final String value = this.values.get(key);
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
        final String value = this.values.get(key);
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
        final String value = this.values.get(key);
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
        final String value = this.values.get(key);
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
     * @return All configuration options of the current environment
     */
    public Map<String, String> getAllConfigurations() {
        return new ConcurrentHashMap(this.values);
    }

    /**
     * Checks if the application.conf stored in conf/application.conf contains an application
     * secret property (application.secret) that has at least 16 characters (128-Bit)
     *
     * @return True if the configuration contains an application.secret property with at least 16 characters
     */
    public boolean hasValidSecret() {
        final String secret = getString(Key.APPLICATION_SECRET);
        return StringUtils.isNotBlank(secret) && secret.length() >= Default.APPLICATION_SECRET_MIN_LENGTH.toInt();
    }

    /**
     * @return application.name from application.yaml
     */
    public String getApplicationName() {
        return getString(Key.APPLICATION_NAME);
    }

    /**
     * @return application.host from application.yaml
     */
    public String getApplicationHost() {
        return getString(Key.APPLICATION_HOST);
    }

    /**
     * @return appliction.port from application.yaml
     */
    public int getApplicationPort() {
        return getInt(Key.APPLICATION_PORT);
    }

    /**
     * @return default name of flash cookie name
     */
    public String getFlashCookieName() {
        return Default.FLASH_COOKIE_NAME.toString();
    }

    /**
     * @return cookie.name from application.yaml or default value if undefined
     */
    public String getSessionCookieName() {
        return getString(Key.COOKIE_NAME, Default.COOKIE_NAME.toString());
    }

    /**
     * @return application.secret from application.yaml
     */
    public String getApplicationSecret() {
        return getString(Key.APPLICATION_SECRET);
    }

    /**
     * @return auth.cookie.name from application.yaml or default value if undefined
     */
    public String getAuthenticationCookieName() {
        return getString(Key.AUTH_COOKIE_NAME, Default.AUTH_COOKIE_NAME.toString());
    }

    /**
     * @return auth.cookie.expires from application.yaml or default value if undefined
     */
    public long getAuthenticationExpires() {
        return getLong(Key.AUTH_COOKIE_EXPIRES, Default.AUTH_COOKIE_EXPIRES.toLong());
    }

    /**
     * @return cookie.expires from application.yaml or default value if undefined
     */
    public long getSessionExpires() {
        return getLong(Key.COOKIE_EXPIRES, Default.COOKIE_EXPIRES.toLong());
    }

    /**
     * @return cookie.secure from application.yaml or default value if undefined
     */
    public boolean isSessionCookieSecure() {
        return getBoolean(Key.COOKIE_SECURE, Default.COOKIE_SECURE.toBoolean());
    }

    /**
     * @return auth.cookie.secure from application.yaml or default value if undefined
     */
    public boolean isAuthenticationCookieSecure() {
        return getBoolean(Key.AUTH_COOKIE_SECURE, Default.AUTH_COOKIE_SECURE.toBoolean());
    }

    /**
     * @author William Dunne
     * @return cookie.i18n.name from application.yaml or default value if undefined
     */
    public String getI18nCookieName() {
        return getString(Key.COOKIE_I18N_NAME, Default.COOKIE_I18N_NAME.toString());
    }

    /**
     * @return same value as isSessionCookieSecure()
     */
    public boolean isFlashCookieSecure() {
        return isSessionCookieSecure();
    }

    /**
     * @return application.language from application.yaml or default value if undefined
     */
    public String getApplicationLanguage() {
        return getString(Key.APPLICATION_LANGUAGE, Default.LANGUAGE.toString());
    }
    /**
     * @return auth.cookie.encrypt from application.yaml or default value if undefined
     */
    public boolean isAuthenticationCookieEncrypt() {
        return getBoolean(Key.AUTH_COOKIE_ENCRYPT, Default.AUTH_COOKIE_ENCRYPT.toBoolean());
    }

    /**
     * @return auth.cookie.version from application.yaml or default value if undefined
     */
    public String getAuthCookieVersion() {
        return getString(Key.AUTH_COOKIE_VERSION, Default.AUTH_COOKIE_VERSION.toString());
    }

    /**
     * @return cookie.version from application.yaml or default value if undefined
     */
    public String getCookieVersion() {
        return getString(Key.COOKIE_VERSION, Default.COOKIE_VERSION.toString());
    }

    /**
     * @return scheduler.autostart from application.yaml or default value if undefined
     */
    public boolean isSchedulerAutostart() {
        return getBoolean(Key.SCHEDULER_AUTOSTART, Default.SCHEDULER_AUTOSTART.toBoolean());
    }

    /**
     * @return application.admin.username from application.yaml or null if undefined
     */
    public String getAdminAuthenticationUser() {
        return getString(Key.APPLICATION_ADMIN_USERNAME);
    }

    /**
     * @return application.admin.password from application.yaml or null if undefined
     */
    public String getAdminAuthenticationPassword() {
        return getString(Key.APPLICATION_ADMIN_PASSWORD);
    }

    /**
     * @return scheduler.package from application.yaml or default value if undefined
     */
    public String getSchedulerPackage() {
        return getString(Key.SCHEDULER_PACKAGE, Default.SCHEDULER_PACKAGE.toString());
    }

    /**
     * @return cookie.encryption from application.yaml or default value if undefined
     */
    public boolean isSessionCookieEncrypt() {
        return getBoolean(Key.COOKIE_ENCRYPTION, Default.COOKIE_ENCRYPTION.toBoolean());
    }

    /**
     * @return auth.cookie.remember.expires from application.yaml or default value if undefined
     */
    public long getAuthenticationRememberExpires() {
        return getLong(Key.AUTH_COOKIE_REMEMBER_EXPIRES, Default.AUTH_COOKIE_REMEMBER_EXPIRES.toLong());
    }

    /**
     * @return execution.threadpool from application.yaml or default value if undefined
     */
    public int getExecutionPool() {
        return getInt(Key.EXECUTION_THREADPOOL, Default.EXECUTION_THREADPOOL.toInt());
    }

    /**
     * @return application.controller from application.yaml or default value if undefined
     */
    public String getControllerPackage() {
        return getString(Key.APPLICATION_CONTROLLER, Default.APPLICATION_CONTROLLER.toString());
    }

    /**
     * @return application.timer from application.yaml or default value if undefined
     */
    public boolean isTimerEnabled() {
        return getBoolean(Key.APPLICATION_TIMER, Default.APPLICATION_TIMER.toBoolean());
    }

    /**
     * @return cache.class from application.yaml or default value if undefined
     */
    public String getCacheClass() {
        return getString(Key.CACHE_CLASS, Default.CACHE_CLASS.toString());
    }

    /**
     * @return cache.addresses from application.yaml
     */
    public String getCacheAddresses() {
        return getString(Key.CACHE_ADDRESSES);
    }

    /**
     * @return templateengine.class from application.yaml
     */
    public String getTemplateEngineClass() {
        return getString(Key.TEMPLATE_ENGINE_CLASS, Default.TEMPLATE_ENGINE_CLASS.toString());
    }

    /**
     * @return application.minify.js or default value if undefined
     */
    public boolean isMinifyJS() {
        return getBoolean(Key.APPLICATION_MINIFY_JS, false);
    }

    /**
     * @return application.minify.css or default value if undefined
     */
    public boolean isMinifyCSS() {
        return getBoolean(Key.APPLICATION_MINIFY_CSS, false);
    }

    /**
     * @return application.preprocess.sass or default value if undefined
     */
    public boolean isPreprocessSass() {
        return getBoolean(Key.APPLICATION_PREPROCESS_SASS, false);
    }
    
    /**
     * @return application.preprocess.less or default value if undefined
     */
    public boolean isPreprocessLess() {
        return getBoolean(Key.APPLICATION_PREPROCESS_LESS, false);
    }
    
    /**
     * @return application.assets.path (for testing purposes only)
     */
    public String getAssetsPath() {
        return Default.ASSETS_PATH.toString();
    }

    /**
     * 
     * @return application.admin.enable or default value if undefined
     */
    public boolean isAdminEnabled() {
        return getBoolean(Key.APPLICATION_ADMIN_ENABLE, false);
    }
}