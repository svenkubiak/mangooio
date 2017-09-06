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
import io.mangoo.enums.Jvm;
import io.mangoo.enums.Key;
import io.mangoo.enums.Mode;
import io.mangoo.enums.Required;

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
    private final Map<String, String> values = new ConcurrentHashMap<>(16, 0.9F, 1);
    private boolean decrypted = true;
    
    public Config() {
        prepare(Default.CONFIGURATION_FILE.toString(), Application.getMode());
        decrypt();
    }
    
    public Config(String configFile, Mode mode) {
        Objects.requireNonNull(configFile, Required.CONFIG_FILE.toString());
        Objects.requireNonNull(mode, Required.MODE.toString());

        prepare(configFile, mode);
        decrypt();
    }

    private void prepare(String configFile, Mode mode) {
        final String configPath = System.getProperty(Jvm.APPLICATION_CONFIG.toString());

        Map map;
        if (StringUtils.isNotBlank(configPath)) {
            map = (Map) loadConfiguration(configPath, false);
            LOG.info("Loading application configuration from: classpath");
        } else {
            map = (Map) loadConfiguration(configFile, true);
            LOG.info("Loading application configuration from: " + configFile);
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
                inputStream = new FileInputStream(new File(path)); //NOSONAR
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
                    if (value == null) {
                        this.values.put(StringUtils.substringAfter(parentKey + "." + key, "."), "");   
                    } else if (("${arg}").equalsIgnoreCase(String.valueOf(value))) {
                        this.values.put(StringUtils.substringAfter(parentKey + "." + key, "."), System.getProperty(entry.getKey()));   
                    } else {
                        this.values.put(StringUtils.substringAfter(parentKey + "." + key, "."), String.valueOf(value));   
                    }
                }
            }
        }
    }

    /**
     * Decrypts all encrypted config value
     */
    private void decrypt() {
        String key = null;
        Crypto crypto = new Crypto(this);

        for (final Entry<String, String> entry : this.values.entrySet()) {
            if (isEncrypted(entry.getValue())) {
                if (StringUtils.isBlank(key)) {
                    key = getMasterKey();
                }

                if (StringUtils.isNotBlank(key)) {
                    final String decryptedText = crypto.decrypt(StringUtils.substringBetween(entry.getValue(), "cryptex[", "]"), key);
                    if (StringUtils.isNotBlank(decryptedText)) {
                        this.values.put(entry.getKey(), decryptedText);
                    } else {
                        decrypted = false;
                    }
                } else {
                    LOG.error("Found encrypted config value '" + entry.getKey() + "' but no masterkey was set.");
                    decrypted = false;
                }
            }
        }
    }
    
    /**
     * @return True if decryption of config values was successful, false otherwise
     */
    public boolean isDecrypted() {
        return decrypted;
    }

    /**
     * @return The master key for encrypted config value, returns a default value if in test mode
     */
    public String getMasterKey() {
        String masterkey = System.getProperty(Jvm.APPLICATION_MASTERKEY.toString());
        if (StringUtils.isBlank(masterkey)) {
            String masterkeyFile = this.values.get(Key.APPLICATION_MASTERKEY_FILE.toString());
            if (StringUtils.isNotBlank(masterkeyFile)) {
                try {
                    masterkey = FileUtils.readFileToString(new File(masterkeyFile), Default.ENCODING.toString()); //NOSONAR
                } catch (final IOException e) {
                    LOG.error("Failed to read master key", e);
                }
            } else {
                LOG.error("Failed to load masterkey file. Please make sure to set a masterkey file if using encrypted config values");
            }
        }

        return masterkey;
    }

    /**
     * Checks if a value is encrypt by checking for the prefix crpytex
     *
     * @param value The value to check
     * @return True if the value starts with cryptex, false otherwise
    */
    public boolean isEncrypted(String value) {
        Objects.requireNonNull(value, Required.VALUE.toString());
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

        return Integer.parseInt(value);
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

        return Long.parseLong(value);
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

        return Long.parseLong(value);
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

        return Integer.parseInt(value);
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

        return Boolean.parseBoolean(value);
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

        return Boolean.parseBoolean(value);
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
     * secret property (application.secret) that has at least 32 characters (256-Bit)
     *
     * @return True if the configuration contains an application.secret property with at least 32 characters
     */
    public boolean hasValidSecret() {
        final String secret = getApplicationSecret();
        return StringUtils.isNotBlank(secret) && secret.length() >= Default.APPLICATION_SECRET_MIN_LENGTH.toInt();
    }

    /**
     * @return application.name from application.yaml
     */
    public String getApplicationName() {
        return getString(Key.APPLICATION_NAME);
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
        return getInt(Key.APPLICATION_THREADPOOL, Default.EXECUTION_THREADPOOL.toInt());
    }

    /**
     * @return application.controller from application.yaml or default value if undefined
     */
    public String getControllerPackage() {
        return getString(Key.APPLICATION_CONTROLLER, Default.APPLICATION_CONTROLLER.toString());
    }

    /**
     * @return templateengine.class from application.yaml
     */
    public String getTemplateEngineClass() {
        return getString(Key.APPLICATION_TEMPLATEENGINE, Default.TEMPLATE_ENGINE_CLASS.toString());
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

    /**
     * @return smtp.host or default value if undefined
     */
    public String getSmtpHost() {
        return getString(Key.SMTP_HOST, Default.SMTP_HOST.toString());
    }

    /**
     * @return smtp.port or default value if undefined
     */
    public int getSmtpPort() {
        return getInt(Key.SMTP_PORT, Default.SMTP_PORT.toInt());
    }

    /**
     * @return smtp.ssl or default value if undefined
     */
    public boolean isSmtpSSL() {
        return getBoolean(Key.SMTP_SSL, Default.SMTP_SSL.toBoolean());
    }

    /**
     * @return smtp.username or null value if undefined
     */
    public String getSmtpUsername() {
        return getString(Key.SMTP_USERNAME, null);
    }

    /**
     * @return smtp.username or null value if undefined
     */
    public String getSmtpPassword() {
        return getString(Key.SMTP_PASSWORD, null);
    }

    /**
     * @return smtp.from or default value if undefined
     */
    public String getSmtpFrom() {
        return getString(Key.SMTP_FROM, Default.SMTP_FROM.toString());
    }

    /**
     * @return jvm property http.host or connector.http.host or null if undefined
     */
    public String getConnectorHttpHost() {
        String httpHost = System.getProperty(Jvm.HTTP_HOST.toString());
        if (StringUtils.isNotBlank(httpHost)) {
            return httpHost;
        }
        
        return getString(Key.CONNECTOR_HTTP_HOST, null);
    }

    /**
     * @return jvm property http.port or connector.http.port or 0 if undefined
     */
    public int getConnectorHttpPort() {
        String httpPort = System.getProperty(Jvm.HTTP_PORT.toString());
        if (StringUtils.isNotBlank(httpPort)) {
            return Integer.parseInt(httpPort);
        }
        
        return getInt(Key.CONNECTOR_HTTP_PORT, 0);
    }

    /**
     * @return jvm property ajp.host or connector.ajp.host or null if undefined
     */
    public String getConnectorAjpHost() {
        String ajpHost = System.getProperty(Jvm.AJP_HOST.toString());
        if (StringUtils.isNotBlank(ajpHost)) {
            return ajpHost;
        }
        
        return getString(Key.CONNECTOR_AJP_HOST, null);
    }

    /**
     * @return jvm property ajp.port or connector.ajp.port or 0 if undefined
     */
    public int getConnectorAjpPort() {
        String ajpPort = System.getProperty(Jvm.AJP_PORT.toString());
        if (StringUtils.isNotBlank(ajpPort)) {
            return Integer.parseInt(ajpPort);
        }
        
        return getInt(Key.CONNECTOR_AJP_PORT, 0);
    }
    
    /**
     * @return application.jwt.signkey or application secret if undefined
     */
    public String getJwtsSignKey() {
        return getString(Key.APPLICATION_JWT_SIGNKEY, getApplicationSecret());
    }

    /**
     * @return application.jwt.encrypt or default value if undefined
     */
    public boolean isJwtsEncrypted() {
        return getBoolean(Key.APPLICATION_JWT_ENCRYPT, Default.APPLICATION_JWT_ENCRYPT.toBoolean());
    }

    /**
     * @return application.jwt.encryptionkey or application secret if undefined
     */
    public String getJwtsEncryptionKey() {
        return getString(Key.APPLICATION_JWT_ENCRYPTION_KEY, getApplicationSecret());
    }

    /**
     * 
     * @return application.headers.xssprotection or default value if undefined
     */
    public int getXssProectionHeader() {
        return getInt(Key.APPLICATION_HEADERS_XSSPROTECTION, Default.APPLICATION_HEADERS_XSSPROTECTION.toInt());
    }

    /**
     * @return application.headers.xcontenttypeoptions or default value if undefined
     */
    public String getXContentTypeOptionsHeader() {
        return getString(Key.APPLICATION_HEADERS_XCONTENTTYPEOPTIONS, Default.APPLICATION_HEADERS_XCONTENTTYPEOPTIONS.toString());
    }

    /**
     * @return application.headers.xframeoptions or default value if undefined
     */
    public String getXFrameOptionsHeader() {
        return getString(Key.APPLICATION_HEADERS_XFRAMEOPTIONS, Default.APPLICATION_HEADERS_XFRAMEOPTIONS.toString());
    }

    /**
     * @return application.headers.server or default value if undefined
     */
    public String getServerHeader() {
        return getString(Key.APPLICATION_HEADERS_SERVER, Default.APPLICATION_HEADERS_SERVER.toString());
    }

    /**
     * @return application.headers.contentsecuritypolicy or default value if undefined
     */
    public String getContentSecurityPolicyHeader() {
        return getString(Key.APPLICATION_HEADERS_CONTENTSECURITYPOLICY, Default.APPLICATION_HEADERS_CONTENTSECURITYPOLICY.toString());
    }

    /**
     * @return cache.cluster.enabled or default value if undefined
     */
    public boolean isClusteredCached() {
        return getBoolean(Key.CACHE_CLUSTER_ENABLE, Default.CACHE_CLUSTER_ENABLE.toBoolean());
    }

    /**
     * @return authentication.lock or default value if undefined
     */
    public int getAuthenticationLock() {
        return getInt(Key.AUTH_LOCK, Default.AUTH_LOCK.toInt());
    }

    /**
     * @return cache.cluster.url or null if undefined
     */
    public String getCacheClusterUrl() {
        return getString(Key.CACHE_CLUSTER_URL, null);
    }

    /**
     * @return application.headers.refererpolicy or default value if undefined
     */
    public String getRefererPolicy() {
        return getString(Key.APPLICATION_HEADERS_REFERERPOLICY, Default.APPLICATION_HEADERS_REFERERPOLICY.toString());
    }
}