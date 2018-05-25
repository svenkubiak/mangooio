package io.mangoo.configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.io.FileUtils;
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
import io.mangoo.enums.Required;
import io.mangoo.utils.IOUtils;

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
        prepare();
        decrypt();
    }

    private void prepare() {
        final String configPath = System.getProperty(Jvm.APPLICATION_CONFIG.toString());

        Map map;
        if (StringUtils.isNotBlank(configPath)) {
            map = (Map) loadConfiguration(configPath, false);
        } else {
            map = (Map) loadConfiguration(Default.CONFIGURATION_FILE.toString(), true);
        }

        if (map != null) {
            final Map<String, Object> defaults = (Map<String, Object>) map.get(Default.DEFAULT_CONFIGURATION.toString());
            final Map<String, Object> environment = (Map<String, Object>) map.get(Application.getMode().toString());

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
                LOG.info("Loading application configuration from {} in classpath", path);
            } else {
                inputStream = new FileInputStream(new File(path)); //NOSONAR
                LOG.info("Loading application configuration from: {}", path);
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
        Crypto crypto = new Crypto(this);

        for (final Entry<String, String> entry : this.values.entrySet()) {
            if (isEncrypted(entry.getValue())) {
                List<String> keys = getMasterKeys();
                
                String value = StringUtils.substringBetween(entry.getValue(), "cryptex[", "]");
                String [] cryptex = value.split(",");
                
                String decryptedValue = null;
                if (cryptex.length == 1) {
                    decryptedValue = crypto.decrypt(cryptex[0].trim(), keys.get(0));
                } else if (cryptex.length == 2) { //NOSONAR
                    decryptedValue = crypto.decrypt(cryptex[0].trim(), keys.get(Integer.parseInt(cryptex[1].trim()) - 1));
                }
                
                if (StringUtils.isNotBlank(decryptedValue)) {
                    this.values.put(entry.getKey(), decryptedValue);
                } else {
                    LOG.error("Failed to decrypt a config value");
                    this.decrypted = false;
                }
            }
        }
    }
    
    /**
     * @return True if decryption of config values was successful, false otherwise
     */
    public boolean isDecrypted() {
        return this.decrypted;
    }

    /**
     * @return The master key(s) for encrypted config value
     */
    public List<String> getMasterKeys() {
        String masterkey = System.getProperty(Jvm.APPLICATION_MASTERKEY.toString());
        List<String> keys = new ArrayList<>();
        
        if (StringUtils.isNotBlank(masterkey)) {
            keys.add(masterkey);
        } else {
            String masterkeyFile = this.values.get(Key.APPLICATION_MASTERKEY_FILE.toString());
            if (StringUtils.isNotBlank(masterkeyFile)) {
                try {
                    keys = FileUtils.readLines(new File(masterkeyFile), Default.ENCODING.toString()); //NOSONAR
                } catch (IOException e) {
                    LOG.error("Failed to load masterkey file. Please make sure to set a masterkey file if using encrypted config values", e);
                }
            } else {
                LOG.error("Failed to load masterkey file. Please make sure to set a masterkey file if using encrypted config values");
            }  
        }

        return keys;
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
     * @return application.name from application.yaml
     */
    public String getApplicationName() {
        return getString(Key.APPLICATION_NAME);
    }

    /**
     * @return flash.cookie.name or default value if undefined
     */
    public String getFlashCookieName() {
        return getString(Key.FLASH_COOKIE_NAME, Default.FLASH_COOKIE_NAME.toString());
    }

    /**
     * @return session.cookie.name from application.yaml or default value if undefined
     */
    public String getSessionCookieName() {
        return getString(Key.SESSION_COOKIE_NAME, Default.SESSION_COOKIE_NAME.toString());
    }

    /**
     * @return application.secret from application.yaml
     */
    public String getApplicationSecret() {
        return getString(Key.APPLICATION_SECRET);
    }

    /**
     * @return authentication.cookie.name from application.yaml or default value if undefined
     */
    public String getAuthenticationCookieName() {
        return getString(Key.AUTHENTICATION_COOKIE_NAME, Default.AUTHENTICATION_COOKIE_NAME.toString());
    }

    /**
     * @return authentication.cookie.expires from application.yaml or default value if undefined
     */
    public long getAuthenticationCookieExpires() {
        return getLong(Key.AUTHENTICATION_COOKIE_EXPIRES, Default.AUTHENTICATION_COOKIE_EXPIRES.toLong());
    }

    /**
     * @return session.cookie.expires from application.yaml or default value if undefined
     */
    public long getSessionCookieExpires() {
        return getLong(Key.SESSION_COOKIE_EXPIRES, Default.SESSION_COOKIE_EXPIRES.toLong());
    }

    /**
     * @return session.cookie.secure from application.yaml or default value if undefined
     */
    public boolean isSessionCookieSecure() {
        return getBoolean(Key.SESSION_COOKIE_SECURE, Default.SESSION_COOKIE_SECURE.toBoolean());
    }

    /**
     * @return authentication.cookie.secure from application.yaml or default value if undefined
     */
    public boolean isAuthenticationCookieSecure() {
        return getBoolean(Key.AUTHENTICATION_COOKIE_SECURE, Default.AUTHENTICATION_COOKIE_SECURE.toBoolean());
    }

    /**
     * @author William Dunne
     * @return i18n.cookie.name from application.yaml or default value if undefined
     */
    public String getI18nCookieName() {
        return getString(Key.I18N_COOKIE_NAME, Default.I18N_COOKIE_NAME.toString());
    }

    /**
     * @return calls isSessionCookieSecure()
     */
    public boolean isFlashCookieSecure() {
        return isSessionCookieSecure();
    }

    /**
     * @return application.language from application.yaml or default value if undefined
     */
    public String getApplicationLanguage() {
        return getString(Key.APPLICATION_LANGUAGE, Default.APPLICATION_LANGUAGE.toString());
    }

    /**
     * @deprecated As of 4.8.0, replace by getAuthenticationCookieVersion
     * @return auth.cookie.version from application.yaml or default value if undefined
     */
    @Deprecated
    public String getAuthCookieVersion() {
        return getString(Key.AUTHENTICATION_COOKIE_VERSION, Default.AUTHENTICATION_COOKIE_VERSION.toString());
    }
    
    /**
     * @return authentication.cookie.version from application.yaml or default value if undefined
     */
    public String getAuthenticationCookieVersion() {
        return getString(Key.AUTHENTICATION_COOKIE_VERSION, Default.AUTHENTICATION_COOKIE_VERSION.toString());
    }

    /**
     * @deprecated As of 4.8.0, replace by getSessionCookieVersion
     * @return cookie.version from application.yaml or default value if undefined
     */
    @Deprecated
    public String getCookieVersion() {
        return getString(Key.SESSION_COOKIE_VERSION, Default.SESSION_COOKIE_VERSION.toString());
    }
    
    /**
     * @return session.cookie.version from application.yaml or default value if undefined
     */
    public String getSessionCookieVersion() {
        return getString(Key.SESSION_COOKIE_VERSION, Default.SESSION_COOKIE_VERSION.toString());
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
    public String getApplicationAdminUsername() {
        return getString(Key.APPLICATION_ADMIN_USERNAME);
    }

    /**
     * @return application.admin.password from application.yaml or null if undefined
     */
    public String getApplicationAdminPassword() {
        return getString(Key.APPLICATION_ADMIN_PASSWORD);
    }

    /**
     * @return scheduler.package from application.yaml or default value if undefined
     */
    public String getSchedulerPackage() {
        return getString(Key.SCHEDULER_PACKAGE, Default.SCHEDULER_PACKAGE.toString());
    }

    /**
     * @return authentication.cookie.remember.expires from application.yaml or default value if undefined
     */
    public long getAuthenticationCookieRememberExpires() {
        return getLong(Key.AUTHENTICATION_COOKIE_REMEMBER_EXPIRES, Default.AUTHENTICATION_COOKIE_REMEMBER_EXPIRES.toLong());
    }

    /**
     * @return application.threadpool from application.yaml or default value if undefined
     */
    public int getApplicationThreadpool() {
        return getInt(Key.APPLICATION_THREADPOOL, Default.APPLICATION_THREADPOOL.toInt());
    }

    /**
     * @return application.controller from application.yaml or default value if undefined
     */
    public String getApplicationController() {
        return getString(Key.APPLICATION_CONTROLLER, Default.APPLICATION_CONTROLLER.toString());
    }

    /**
     * @return application.templateengine from application.yaml or default value if undefined
     */
    public String getApplicationTemplateEngine() {
        return getString(Key.APPLICATION_TEMPLATEENGINE, Default.APPLICATION_TEMPLATEENGINE.toString());
    }

    /**
     * @return application.minify.js or default value if undefined
     */
    public boolean isApplicationMinifyJS() {
        return getBoolean(Key.APPLICATION_MINIFY_JS, Default.APPLICATION_MINIFY_JS.toBoolean());
    }

    /**
     * @return application.minify.css or default value if undefined
     */
    public boolean isApplicationMinifyCSS() {
        return getBoolean(Key.APPLICATION_MINIFY_CSS, Default.APPLICATION_MINIFY_CSS.toBoolean());
    }

    /**
     * @return application.preprocess.sass or default value if undefined
     */
    public boolean isApplicationPreprocessSass() {
        return getBoolean(Key.APPLICATION_PREPROCESS_SASS, Default.APPLICATION_PREPROCESS_SASS.toBoolean());
    }

    /**
     * @return application.preprocess.less or default value if undefined
     */
    public boolean isApplicationPreprocessLess() {
        return getBoolean(Key.APPLICATION_PREPROCESS_LESS, Default.APPLICATION_PREPROCESS_LESS.toBoolean());
    }

    /**
     * @return application.admin.enable or default value if undefined
     */
    public boolean isApplicationAdminEnable() {
        return getBoolean(Key.APPLICATION_ADMIN_ENABLE, Default.APPLICATION_ADMIN_ENABLE.toBoolean());
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
        // FIXME in 5.0.0 - Remove ambiguous arguments; should be accessed from ${arg} in application.yaml
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
        // FIXME in 5.0.0 - Remove ambiguous arguments; should be accessed from ${arg} in application.yaml
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
        // FIXME in 5.0.0 - Remove ambiguous arguments; should be accessed from ${arg} in application.yaml
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
        // FIXME in 5.0.0 - Remove ambiguous arguments; should be accessed from ${arg} in application.yaml
        String ajpPort = System.getProperty(Jvm.AJP_PORT.toString());
        if (StringUtils.isNotBlank(ajpPort)) {
            return Integer.parseInt(ajpPort);
        }
        
        return getInt(Key.CONNECTOR_AJP_PORT, 0);
    }
    
    /**
     * @deprecated As of 4.10.0, will be removed in 5.0.0 without replacement
     * @return application.jwt.signkey or application secret if undefined
     */
    @Deprecated
    public String getJwtsSignKey() {
        return getString(Key.APPLICATION_JWT_SIGNKEY, getApplicationSecret());
    }

    /**
     * @deprecated As of 4.10.0, will be removed in 5.0.0 without replacement
     * @return application.jwt.encrypt or default value if undefined
     */
    @Deprecated
    public boolean isJwtsEncrypted() {
        return getBoolean(Key.APPLICATION_JWT_ENCRYPT, Default.APPLICATION_JWT_ENCRYPT.toBoolean());
    }

    /**
     * @deprecated As of 4.10.0, will be removed in 5.0.0 without replacement
     * @return application.jwt.encryptionkey or application secret if undefined
     */
    @Deprecated
    public String getJwtsEncryptionKey() {
        return getString(Key.APPLICATION_JWT_ENCRYPTION_KEY, getApplicationSecret());
    }

    /**
     * 
     * @return application.headers.xssprotection or default value if undefined
     */
    public int getApplicationHeaderXssProection() {
        return getInt(Key.APPLICATION_HEADERS_XSSPROTECTION, Default.APPLICATION_HEADERS_XSSPROTECTION.toInt());
    }

    /**
     * @return application.headers.xcontenttypeoptions or default value if undefined
     */
    public String getApplicationHeadersXContentTypeOptions() {
        return getString(Key.APPLICATION_HEADERS_XCONTENTTYPEOPTIONS, Default.APPLICATION_HEADERS_XCONTENTTYPEOPTIONS.toString());
    }

    /**
     * @return application.headers.xframeoptions or default value if undefined
     */
    public String getApplicationHeadersXFrameOptions() {
        return getString(Key.APPLICATION_HEADERS_XFRAMEOPTIONS, Default.APPLICATION_HEADERS_XFRAMEOPTIONS.toString());
    }

    /**
     * @return application.headers.server or default value if undefined
     */
    public String getApplicationHeadersServer() {
        return getString(Key.APPLICATION_HEADERS_SERVER, Default.APPLICATION_HEADERS_SERVER.toString());
    }

    /**
     * @return application.headers.contentsecuritypolicy or default value if undefined
     */
    public String getApplicationHeadersContentSecurityPolicy() {
        return getString(Key.APPLICATION_HEADERS_CONTENTSECURITYPOLICY, Default.APPLICATION_HEADERS_CONTENTSECURITYPOLICY.toString());
    }

    /**
     * @return cache.cluster.enable or default value if undefined
     */
    public boolean isCacheCluserEnable() {
        return getBoolean(Key.CACHE_CLUSTER_ENABLE, Default.CACHE_CLUSTER_ENABLE.toBoolean());
    }
    
    /**
     * @return metrics.enable or default value if undefined
     */
    public boolean isMetricsEnable() {
        return getBoolean(Key.METRICS_ENABLE, Default.METRICS_ENABLE.toBoolean());
    }

    /**
     * @return authentication.lock or default value if undefined
     */
    public int getAuthenticationLock() {
        return getInt(Key.AUTHENTICATION_LOCK, Default.AUTHENTICATION_LOCK.toInt());
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
    public String getApplicationHeadersRefererPolicy() {
        return getString(Key.APPLICATION_HEADERS_REFERERPOLICY, Default.APPLICATION_HEADERS_REFERERPOLICY.toString());
    }

    /**
     * @return undertow.maxentitysize or default value if undefined
     */
    public long getUndertowMaxEntitySize() {
        return getLong(Key.UNDERTOW_MAX_ENTITY_SIZE, Default.UNDERTOW_MAX_ENTITY_SIZE.toLong());
    }

    /**
     * @return session.cookie.signkey or application secret if undefined
     */
    public String getSessionCookieSignKey() {
        return getString(Key.SESSION_COOKIE_SIGNKEY, getApplicationSecret());
    }

    /**
     * @return session.cookie.encryptionkey or application secret if undefined
     */
    public String getSessionCookieEncryptionKey() {
        return getString(Key.SESSION_COOKIE_ENCRYPTIONKEY, getApplicationSecret());
    }

    /**
     * @return authentication.cookie.signkey or application secret if undefined
     */
    public String getAuthenticationCookieSignKey() {
        return getString(Key.AUTHENTICATION_COOKIE_SIGNKEY, getApplicationSecret());
    }

    /**
     * @return authentication.cookie.encryptionkey or application secret if undefined
     */
    public String getAuthenticationCookieEncryptionKey() {
        return getString(Key.AUTHENTICATION_COOKIE_ENCRYPTIONKEY, getApplicationSecret());
    }

    /**
     * @return flash.cookie.signkey or application secret if undefined
     */
    public String getFlashCookieSignKey() {
        return getString(Key.FLASH_COOKIE_SIGNKEY, getApplicationSecret());
    }

    /**
     * @return flash.cookie.encryptionkey or application secret if undefined
     */
    public String getFlashCookieEncryptionKey() {
        return getString(Key.FLASH_COOKIE_ENCRYPTIONKEY, getApplicationSecret());
    }
}