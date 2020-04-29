package io.mangoo.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.io.Resources;
import com.google.inject.Singleton;
import com.google.re2j.Pattern;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooEncryptionException;
import jodd.props.Props;

/**
 * Main configuration class for all properties configured in config.props
 *
 * @author svenkubiak
 * @author williamdunne
 *
 */
@Singleton
public class Config {
    private static final Logger LOG = LogManager.getLogger(Config.class);
    private static final String CRYPTEX_TAG = "cryptex{";
    private static final String ARG_TAG = "arg{}";
    private Pattern corsUrl;
    private Pattern corsAllowOrigin;
    private String mode;
    private Props props = Props.create();
    private boolean decrypted = true;
    
    public Config() {
        this.mode = Application.getMode().toString();
        load();
    }
    
    public Config(String mode) {
        this.mode = Objects.requireNonNull(mode, Required.MODE.toString());
    }
    
    @SuppressFBWarnings(justification = "Intenionally used to access the file system", value = "URLCONNECTION_SSRF_FD")
    private final void load() {
        this.props.setActiveProfiles(this.mode);
        this.props.setSkipEmptyProps(false);
        final String configPath = System.getProperty(Key.APPLICATION_CONFIG.toString());
        
        if (StringUtils.isNotBlank(configPath)) {
            try {
                this.props.load(new File(configPath)); //NOSONAR ConfigPath can intentionally come from user input
            } catch (IOException e) {
                LOG.error("Failed to load config.props from {}", configPath, e);
            }
        } else {
            try (InputStream inputStream = Resources.getResource(Default.CONFIGURATION_FILE.toString()).openStream()){
                this.props.load(inputStream);
            } catch (IOException e) {
                LOG.error("Failed to load config.props from /src/main/resources/config.props", e);
            }
        } 
        
        Map<String, String> profileProps = new HashMap<>();
        this.props.extractProps(profileProps, Application.getMode().toString());
        profileProps.forEach(this::parse);

        System.setProperty(Key.APPLICATION_SECRET.toString(), "");
    }
    
    /**
     * Parses a given property key and value and checks if the value comes from
     * a system property and maybe decrypts the value
     * 
     * @param propKey The property key
     * @param propValue The property value
     */
    private void parse(String propKey, String propValue) {
        if (ARG_TAG.equals(propValue)) {
            String value = System.getProperty(propKey);

            if (StringUtils.isNotBlank(value) && value.startsWith(CRYPTEX_TAG)) {
                value = decrypt(value);
            }

            if (StringUtils.isNotBlank(value)) {
                this.props.setValue(propKey, value, Application.getMode().toString());
            }
        }

        if (propValue.startsWith(CRYPTEX_TAG)) {
            this.props.setValue(propKey, decrypt(propValue), Application.getMode().toString());
        }
    }

    /**
     * Decrypts a given property key and rewrites it to props
     * 
     * @param propKey The property key
     * @param propValue The property value
     */
    private String decrypt(String value) {
        Crypto crypto = new Crypto(this);
        
        String keyFile = System.getProperty(Key.APPLICATION_PRIVATEKEY.toString());
        if (StringUtils.isNotBlank(keyFile)) {
            try (Stream<String> lines = Files.lines(Paths.get(keyFile))) { //NOSONAR KeyFile can intentionally come from user input
                String key = lines.findFirst().orElse(null);
                if (StringUtils.isNotBlank(key)) {
                    PrivateKey privateKey = crypto.getPrivateKeyFromString(key);
                    String cryptex = StringUtils.substringBetween(value, CRYPTEX_TAG, "}");

                    if (privateKey != null && StringUtils.isNotBlank(cryptex)) {
                        return crypto.decrypt(cryptex, privateKey);
                    } else {
                        LOG.error("Failed to decrypt an encrypted config value");
                        this.decrypted = false;
                    }
                }
            } catch (IOException | SecurityException | MangooEncryptionException e) {
                LOG.error("Failed to decrypt an encrypted config value", e);
                this.decrypted = false;
            }
        } else {
            LOG.error("Found an encrypted value in config file but private key for decryption is missing");
            this.decrypted = false;
        }
        
        return "";
    }
    
    /**
     * Converts config values to standard java properties
     * 
     * @return Properties instance with config values
     */
    public Properties toProperties() {
        var map = new HashMap<>();
        this.props.extractProps(map);
        
        Properties properties = new Properties();
        properties.putAll(map);
        
        return properties;
    }
    
    /**
     * @return True if decryption of config values was successful, false otherwise
     */
    public boolean isDecrypted() {
        return this.decrypted;
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as String or null if the key is not configured
     */
    public String getString(String key) {
        return this.props.getValue(key);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as String or the passed defautlValue if the key is not configured
     */
    public String getString(String key, String defaultValue) {
        return this.props.getValueOrDefault(key, defaultValue);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as int or 0 if the key is not configured
     */
    public int getInt(String key) {
        final String value = this.props.getValue(key);
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
        final String value = this.props.getValue(key);
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
        final String value = this.props.getValue(key);
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
        final String value = this.props.getValue(key);
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
        final String value = this.props.getValue(key);
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
        final String value = this.props.getValue(key);
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
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();        
        this.props.entries().forEach(entry -> map.put(entry.getKey(), entry.getValue()));
        
        return map;
    }

    /**
     * @return application.name from config.props
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
     * @return session.cookie.name from config.props or default value if undefined
     */
    public String getSessionCookieName() {
        return getString(Key.SESSION_COOKIE_NAME, Default.SESSION_COOKIE_NAME.toString());
    }

    /**
     * @return application.secret from config.props
     */
    public String getApplicationSecret() {
        return getString(Key.APPLICATION_SECRET);
    }
    
    /**
     * @return application.publickey from config.props
     */
    public String getApplicationPublicKey() {
        return getString(Key.APPLICATION_PUBLICKEY);
    }

    /**
     * @return authentication.cookie.name from config.props or default value if undefined
     */
    public String getAuthenticationCookieName() {
        return getString(Key.AUTHENTICATION_COOKIE_NAME, Default.AUTHENTICATION_COOKIE_NAME.toString());
    }

    /**
     * @return session.cookie.tooken.expires from config.props or default value if undefined
     */
    public long getSessionCookieTokenExpires() {
        return getLong(Key.SESSION_COOKIE_TOKEN_EXPIRES, Default.SESSION_COOKIE_TOKEN_EXPIRES.toLong());
    }

    /**
     * @return session.cookie.secure from config.props or default value if undefined
     */
    public boolean isSessionCookieSecure() {
        return getBoolean(Key.SESSION_COOKIE_SECURE, Default.SESSION_COOKIE_SECURE.toBoolean());
    }

    /**
     * @return authentication.cookie.secure from config.props or default value if undefined
     */
    public boolean isAuthenticationCookieSecure() {
        return getBoolean(Key.AUTHENTICATION_COOKIE_SECURE, Default.AUTHENTICATION_COOKIE_SECURE.toBoolean());
    }

    /**
     * @return i18n.cookie.name from config.props or default value if undefined
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
     * @return application.language from config.props or default value if undefined
     */
    public String getApplicationLanguage() {
        return getString(Key.APPLICATION_LANGUAGE, Default.APPLICATION_LANGUAGE.toString());
    }

    /**
     * @return scheduler.autostart from config.props or default value if undefined
     */
    public boolean isSchedulerAutostart() {
        return getBoolean(Key.SCHEDULER_AUTOSTART, Default.SCHEDULER_AUTOSTART.toBoolean());
    }

    /**
     * @return application.admin.username from config.props or null if undefined
     */
    public String getApplicationAdminUsername() {
        return getString(Key.APPLICATION_ADMIN_USERNAME);
    }

    /**
     * @return application.admin.password from config.props or null if undefined
     */
    public String getApplicationAdminPassword() {
        return getString(Key.APPLICATION_ADMIN_PASSWORD);
    }

    /**
     * @return scheduler.package from config.props or default value if undefined
     */
    public String getSchedulerPackage() {
        return getString(Key.SCHEDULER_PACKAGE, Default.SCHEDULER_PACKAGE.toString());
    }

    /**
     * @return authentication.cookie.remember.expires from config.props or default value if undefined
     */
    public long getAuthenticationCookieRememberExpires() {
        return getLong(Key.AUTHENTICATION_COOKIE_REMEMBER_EXPIRES, Default.AUTHENTICATION_COOKIE_REMEMBER_EXPIRES.toLong());
    }

    /**
     * @return application.threadpool from config.props or default value if undefined
     */
    public int getApplicationThreadpool() {
        return getInt(Key.APPLICATION_THREADPOOL, Default.APPLICATION_THREADPOOL.toInt());
    }

    /**
     * @return application.controller from config.props or default value if undefined
     */
    public String getApplicationController() {
        return getString(Key.APPLICATION_CONTROLLER, Default.APPLICATION_CONTROLLER.toString());
    }

    /**
     * @return application.templateengine from config.props or default value if undefined
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
        return getString(Key.CONNECTOR_HTTP_HOST, null);
    }

    /**
     * @return jvm property http.port or connector.http.port or 0 if undefined
     */
    public int getConnectorHttpPort() {
        return getInt(Key.CONNECTOR_HTTP_PORT, 0);
    }

    /**
     * @return jvm property ajp.host or connector.ajp.host or null if undefined
     */
    public String getConnectorAjpHost() {
        return getString(Key.CONNECTOR_AJP_HOST, null);
    }

    /**
     * @return jvm property ajp.port or connector.ajp.port or 0 if undefined
     */
    public int getConnectorAjpPort() {
        return getInt(Key.CONNECTOR_AJP_PORT, 0);
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
     * @return undertow.maxentitysize or default value if undefined
     */
    public long getUndertowMaxEntitySize() {
        return getLong(Key.UNDERTOW_MAX_ENTITY_SIZE, Default.UNDERTOW_MAX_ENTITY_SIZE.toLong());
    }

    /**
     * @return session.cookie.secret or application secret if undefined
     */
    public String getSessionCookieSecret() {
        return getString(Key.SESSION_COOKIE_SECRET, getApplicationSecret());
    }

    /**
     * @return authentication.cookie.secret or application secret if undefined
     */
    public String getAuthenticationCookieSecret() {
        return getString(Key.AUTHENTICATION_COOKIE_SECRET, getApplicationSecret());
    }

    /**
     * @return flash.cookie.secret or application secret if undefined
     */
    public String getFlashCookieSecret() {
        return getString(Key.FLASH_COOKIE_SECRET, getApplicationSecret());
    }

    /**
     * @return scheduler.enable or default value if undefined
     */
    public boolean isSchedulerEnabled() {
        return getBoolean(Key.SCHEDULER_ENABLE, Default.SCHEDULER_ENABLE.toBoolean());
    }

    /**
     * @return application.admin.secret or null if undefined
     */
    public String getApplicationAdminSecret() {
        return getString(Key.APPLICATION_ADMIN_SECRET, null);
    }

    /**
     * @return smtp.debug or default value if undefined
     */
    public boolean isSmtpDebug() {
        return getBoolean(Key.SMTP_DEBUG, Default.SMTP_DEBUG.toBoolean());
    }
    
    /**
     * @return cors.enable or default value if undefined
     */
    public boolean isCorsEnable() {
        return getBoolean(Key.CORS_ENABLE, Default.CORS_ENABLE.toBoolean());
    }
    
    /**
     * @return cors.urlpattern as compiled pattern or default value if undefined
     */
    public Pattern getCorsUrlPattern() {
        if (this.corsUrl == null) {
            this.corsUrl = Pattern.compile(getString(Key.CORS_URLPATTERN, Default.CORS_URLPATTERN.toString()));
        }
        return this.corsUrl;
    }
    
    /**
     * @return cors.policyclass as compiled pattern or default value if undefined
     */
    public Pattern getCorsAllowOrigin() {
        if (this.corsAllowOrigin == null) {
            this.corsAllowOrigin = Pattern.compile(getString(Key.CORS_ALLOWORIGIN, Default.CORS_ALLOWORIGIN.toString()));
        }
        
        return this.corsAllowOrigin;
    }
    
    /**
     * @return cors.headers.allowcredentials or default value if undefined
     */
    public String getCorsHeadersAllowCredentials() {
        return getString(Key.CORS_HEADERS_ALLOWCREDENTIALS, Default.CORS_HEADERS_ALLOWCREDENTIALS.toString());
    }
    
    /**
     * @return cors.headers.allowheaders or default value if undefined
     */
    public String getCorsHeadersAllowHeaders() {
        return getString(Key.CORS_HEADERS_ALLOWHEADERS, Default.CORS_HEADERS_ALLOWHEADERS.toString());
    }
    
    /**
     * @return cors.headers.allowheaders or default value if undefined
     */
    public String getCorsHeadersAllowMethods() {
        return getString(Key.CORS_HEADERS_ALLOWMETHODS, Default.CORS_HEADERS_ALLOWMETHODS.toString());
    }
    
    /**
     * @return cors.headers.exposeheaders or default value if undefined
     */
    public String getCorsHeadersExposeHeaders() {
        return getString(Key.CORS_HEADERS_EXPOSEHEADERS, Default.CORS_HEADERS_EXPOSEHEADERS.toString());
    }

    /**
     * @return cors.headers.maxage or default value if undefined
     */
    public String getCorsHeadersMaxAge() {
        return getString(Key.CORS_HEADERS_MAXAGE, Default.CORS_HEADERS_MAXAGE.toString());
    }

    /**
     * @return persistence.mongo.host or default value if undefined
     */
    public String getMongoHost() {
        return getString(Key.PERSISTENCE_MONGO_HOST, Default.PERSISTENCE_MONGO_HOST.toString());
    }

    /**
     * @return persistence.mongo.port or default value if undefined
     */
    public int getMongoPort() {
        return getInt(Key.PERSISTENCE_MONGO_PORT, Default.PERSISTENCE_MONGO_PORT.toInt());
    }

    /**
     * @return persistence.mongo.username or null if undefined
     */
    public String getMongoUsername() {
        return getString(Key.PERSISTENCE_MONGO_USERNAME, null);
    }

    /**
     * @return persistence.mongo.password or null if undefined
     */
    public String getMongoPassword() {
        return getString(Key.PERSISTENCE_MONGO_PASSWORD, null);
    }

    /**
     * @return persistence.mongo.authdb or null if undefined
     */
    public String getMongoAuthDB() {
        return getString(Key.PERSISTENCE_MONGO_AUTHDB, null);
    }

    /**
     * @return persistence.mongo.auth or default value if undefined
     */
    public boolean isMongoAuth() {
        return getBoolean(Key.PERSISTENCE_MONGO_AUTH, Default.PERSISTENCE_MONGO_AUTH.toBoolean());
    }

    /**
     * @return persistence.mongo.package or default value if undefined
     */
    public String getMongoPackage() {
        return getString(Key.PERSISTENCE_MONGO_PACKAGE, Default.PERSISTENCE_MONGO_PACKAGE.toString());
    }

    /**
     * @return persistence.mongo.dbname or default value if undefined
     */
    public String getMongoDbName() {
        return getString(Key.PERSISTENCE_MONGO_DBNAME, Default.PERSISTENCE_MONGO_DBNAME.toString());
    }

    /**
     * @return persistence.mongo.embedded or default value if undefined
     */
    public boolean isMongoEmbedded() {
        return getBoolean(Key.PERSISTENCE_MONGO_EMBEDDED, Default.PERSISTENCE_MONGO_EMBEDDED.toBoolean());
    }

    /**
     * @return session.cookie.expires or default value if undefined
     */
    public boolean isSessionCookieExpires() {
        return getBoolean(Key.SESSION_COOKIE_EXPIRES, Default.SESSION_COOKIE_EXPIRES.toBoolean());
    }

    /**
     * @return authentication.cookie.expires or default value if undefined
     */
    public boolean isAuthenticationCookieExpires() {
        return getBoolean(Key.AUTHENTICATION_COOKIE_EXPIRES, Default.AUTHENTICATION_COOKIE_EXPIRES.toBoolean());
    }

    /**
     * @return authentication.cookie.expires or default value if undefined
     */
    public long getAuthenticationCookieTokenExpires() {
        return getLong(Key.AUTHENTICATION_COOKIE_TOKEN_EXPIRES, Default.AUTHENTICATION_COOKIE_TOKEN_EXPIRES.toLong());
    }

    /**
     * @return smtp.authentication
     */
    public boolean isSmtpAuthentication() {
        return getBoolean(Key.SMTP_AUTHENTICATION, Default.SMTP_AUTHENTICATION.toBoolean());
    }

    /**
     * @return smtp.protocol
     */
    public String getSmtpProtocol() {
        return getString(Key.SMTP_PROTOCOL, Default.SMTP_PROTOCOL.toString());
    }    
}