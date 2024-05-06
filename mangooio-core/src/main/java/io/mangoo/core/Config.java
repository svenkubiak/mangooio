package io.mangoo.core;

import com.google.common.io.Resources;
import com.google.inject.Singleton;
import com.google.re2j.Pattern;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.constants.Default;
import io.mangoo.constants.Key;
import io.mangoo.constants.NotNull;
import io.mangoo.crypto.Crypto;
import io.mangoo.exceptions.MangooEncryptionException;
import jodd.props.Props;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

@Singleton
public class Config {
    private static final Logger LOG = LogManager.getLogger(Config.class);
    private static final String CRYPTEX_TAG = "cryptex{";
    private static final String ARG_TAG = "arg{}";
    private final String mode;
    private final Props props = Props.create();
    private Pattern corsUrl;
    private Pattern corsAllowOrigin;
    private boolean decrypted = true;
    
    public Config() {
        this.mode = Application.getMode().toString();
        load();
    }
    
    public Config(String mode) {
        this.mode = Objects.requireNonNull(mode, NotNull.MODE);
    }
    
    @SuppressFBWarnings(justification = "Intentionally used to access the file system", value = "URLCONNECTION_SSRF_FD")
    private void load() {
        props.setActiveProfiles(mode);
        props.setSkipEmptyProps(false);
        final String configPath = System.getProperty(Key.APPLICATION_CONFIG);
        
        if (StringUtils.isNotBlank(configPath)) {
            try {
                props.load(new File(configPath)); //NOSONAR ConfigPath can intentionally come from user input
            } catch (IOException e) {
                LOG.error("Failed to load config.props from {}", configPath, e);
            }
        } else {
            try (var inputStream = Resources.getResource(Default.CONFIGURATION_FILE).openStream()){
                props.load(inputStream);
            } catch (IOException e) {
                LOG.error("Failed to load config.props from /src/main/resources/config.props", e);
            }
        } 
        
        Map<String, String> profileProps = new HashMap<>();
        props.extractProps(profileProps, Application.getMode().toString());
        profileProps.forEach(this::parse);

        System.setProperty(Key.APPLICATION_SECRET, Strings.EMPTY);
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
                props.setValue(propKey, value, Application.getMode().toString());
            }
        }

        if (propValue.startsWith(CRYPTEX_TAG)) {
            props.setValue(propKey, decrypt(propValue), Application.getMode().toString());
        }
    }

    /**
     * Decrypts a given property key and rewrites it to props
     * 
     * @param value The encrypted value to decrypt
     */
    private String decrypt(String value) {
        var crypto = new Crypto(this);
        
        String keyFile = System.getProperty(Key.APPLICATION_PRIVATEKEY);
        if (StringUtils.isNotBlank(keyFile)) {
            try (Stream<String> lines = Files.lines(Paths.get(keyFile))) { //NOSONAR KeyFile can intentionally come from user input
                String key = lines.findFirst().orElse(null);
                if (StringUtils.isNotBlank(key)) {
                    var privateKey = crypto.getPrivateKeyFromString(key);
                    var cryptex = StringUtils.substringBetween(value, CRYPTEX_TAG, "}");

                    if (privateKey != null && StringUtils.isNotBlank(cryptex)) {
                        return crypto.decrypt(cryptex, privateKey);
                    } else {
                        LOG.error("Failed to decrypt an encrypted config value");
                        decrypted = false;
                    }
                }
            } catch (IOException | SecurityException | MangooEncryptionException e) {
                LOG.error("Failed to decrypt an encrypted config value", e);
                decrypted = false;
            }
        } else {
            LOG.error("Found an encrypted value in config file but private key for decryption is missing");
            decrypted = false;
        }
        
        return Strings.EMPTY;
    }
    
    /**
     * Converts config values to standard java properties
     * 
     * @return Properties instance with config values
     */
    public Properties toProperties() {
        var map = new HashMap<>();
        props.extractProps(map);
        
        var properties = new Properties();
        properties.putAll(map);
        
        return properties;
    }
    
    /**
     * @return True if decryption of config values was successful, false otherwise
     */
    public boolean isDecrypted() {
        return decrypted;
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as String or null if the key is not configured
     */
    public String getString(String key) {
        return props.getValue(key);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as String or the passed defaultValue if the key is not configured
     */
    public String getString(String key, String defaultValue) {
        return props.getValueOrDefault(key, defaultValue);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as int or 0 if the key is not configured
     */
    public int getInt(String key) {
        final String value = props.getValue(key);
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
        final String value = props.getValue(key);
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
     * @return The configured value as int or the passed defaultValue if the key is not configured
     */
    public long getLong(String key, long defaultValue) {
        final String value = props.getValue(key);
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
        final String value = props.getValue(key);
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
        final String value = props.getValue(key);
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
        final String value = props.getValue(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }

        return Boolean.parseBoolean(value);
    }

    /**
     * @return All configuration options of the current environment
     */
    public Map<String, String> getAllConfigurations() {
        ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();        
        props.entries().forEach(entry -> map.put(entry.getKey(), entry.getValue()));
        
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
        return getString(Key.FLASH_COOKIE_NAME, Default.FLASH_COOKIE_NAME);
    }

    /**
     * @return session.cookie.name from config.props or default value if undefined
     */
    public String getSessionCookieName() {
        return getString(Key.SESSION_COOKIE_NAME, Default.SESSION_COOKIE_NAME);
    }

    /**
     * @return application.secret from config.props
     */
    public String getApplicationSecret() {
        return getString(Key.APPLICATION_SECRET);
    }
    
    /**
     * @return application.publicKey from config.props
     */
    public String getApplicationPublicKey() {
        return getString(Key.APPLICATION_PUBLICKEY);
    }

    /**
     * @return authentication.cookie.name from config.props or default value if undefined
     */
    public String getAuthenticationCookieName() {
        return getString(Key.AUTHENTICATION_COOKIE_NAME, Default.AUTHENTICATION_COOKIE_NAME);
    }

    /**
     * @return session.cookie.token.expires from config.props or default value if undefined
     */
    public long getSessionCookieTokenExpires() {
        return getLong(Key.SESSION_COOKIE_TOKEN_EXPIRES, Default.SESSION_COOKIE_TOKEN_EXPIRES);
    }

    /**
     * @return session.cookie.secure from config.props or default value if undefined
     */
    public boolean isSessionCookieSecure() {
        return getBoolean(Key.SESSION_COOKIE_SECURE, Default.SESSION_COOKIE_SECURE);
    }

    /**
     * @return authentication.cookie.secure from config.props or default value if undefined
     */
    public boolean isAuthenticationCookieSecure() {
        return getBoolean(Key.AUTHENTICATION_COOKIE_SECURE, Default.AUTHENTICATION_COOKIE_SECURE);
    }

    /**
     * @return i18n.cookie.name from config.props or default value if undefined
     */
    public String getI18nCookieName() {
        return getString(Key.I18N_COOKIE_NAME, Default.I18N_COOKIE_NAME);
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
        return getString(Key.APPLICATION_LANGUAGE, Default.APPLICATION_LANGUAGE);
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
     * @return authentication.cookie.remember.expires from config.props or default value if undefined
     */
    public long getAuthenticationCookieRememberExpires() {
        return getLong(Key.AUTHENTICATION_COOKIE_REMEMBER_EXPIRES, Default.AUTHENTICATION_COOKIE_REMEMBER_EXPIRES);
    }

    /**
     * @return application.controller from config.props or default value if undefined
     */
    public String getApplicationController() {
        return getString(Key.APPLICATION_CONTROLLER, Default.APPLICATION_CONTROLLER);
    }

    /**
     * @return application.admin.enable or default value if undefined
     */
    public boolean isApplicationAdminEnable() {
        return getBoolean(Key.APPLICATION_ADMIN_ENABLE, Default.APPLICATION_ADMIN_ENABLE);
    }

    /**
     * @return smtp.host or default value if undefined
     */
    public String getSmtpHost() {
        return getString(Key.SMTP_HOST, Default.SMTP_HOST);
    }

    /**
     * @return smtp.port or default value if undefined
     */
    public int getSmtpPort() {
        return getInt(Key.SMTP_PORT, Default.SMTP_PORT);
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
        return getString(Key.SMTP_FROM, Default.SMTP_FROM);
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
     * @return metrics.enable or default value if undefined
     */
    public boolean isMetricsEnable() {
        return getBoolean(Key.METRICS_ENABLE, Default.METRICS_ENABLE);
    }

    /**
     * @return authentication.lock or default value if undefined
     */
    public int getAuthenticationLock() {
        return getInt(Key.AUTHENTICATION_LOCK, Default.AUTHENTICATION_LOCK);
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
        return getLong(Key.UNDERTOW_MAX_ENTITY_SIZE, Default.UNDERTOW_MAX_ENTITY_SIZE);
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
        return getBoolean(Key.SCHEDULER_ENABLE, Default.SCHEDULER_ENABLE);
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
        return getBoolean(Key.SMTP_DEBUG, Default.SMTP_DEBUG);
    }
    
    /**
     * @return cors.enable or default value if undefined
     */
    public boolean isCorsEnable() {
        return getBoolean(Key.CORS_ENABLE, Default.CORS_ENABLE);
    }
    
    /**
     * @return cors.urlpattern as compiled pattern or default value if undefined
     */
    public Pattern getCorsUrlPattern() {
        if (corsUrl == null) {
            corsUrl = Pattern.compile(getString(Key.CORS_URL_PATTERN, Default.CORS_URL_PATTERN));
        }
        return corsUrl;
    }
    
    /**
     * @return cors.policyclass as compiled pattern or default value if undefined
     */
    public Pattern getCorsAllowOrigin() {
        if (corsAllowOrigin == null) {
            corsAllowOrigin = Pattern.compile(getString(Key.CORS_ALLOW_ORIGIN, Default.CORS_ALLOW_ORIGIN));
        }
        
        return corsAllowOrigin;
    }
    
    /**
     * @return cors.headers.allowcredentials or default value if undefined
     */
    public String getCorsHeadersAllowCredentials() {
        return getString(Key.CORS_HEADERS_ALLOW_CREDENTIALS, Default.CORS_HEADERS_ALLOW_CREDENTIALS.toString());
    }
    
    /**
     * @return cors.headers.allowheaders or default value if undefined
     */
    public String getCorsHeadersAllowHeaders() {
        return getString(Key.CORS_HEADERS_ALLOW_HEADERS, Default.CORS_HEADERS_ALLOW_HEADERS);
    }
    
    /**
     * @return cors.headers.allowheaders or default value if undefined
     */
    public String getCorsHeadersAllowMethods() {
        return getString(Key.CORS_HEADERS_ALLOW_METHODS, Default.CORS_HEADERS_ALLOW_METHODS);
    }
    
    /**
     * @return cors.headers.exposeheaders or default value if undefined
     */
    public String getCorsHeadersExposeHeaders() {
        return getString(Key.CORS_HEADERS_EXPOSE_HEADERS, Default.CORS_HEADERS_EXPOSE_HEADERS);
    }

    /**
     * @return cors.headers.maxage or default value if undefined
     */
    public String getCorsHeadersMaxAge() {
        return getString(Key.CORS_HEADERS_MAX_AGE, Default.CORS_HEADERS_MAX_AGE);
    }

    /**
     * @return persistence.mongo.host or default value if undefined
     * @param prefix The prefix to use
     */
    public String getMongoHost(String prefix) {
        return getString(prefix + Key.PERSISTENCE_MONGO_HOST, Default.PERSISTENCE_MONGO_HOST);
    }

    /**
     * @return persistence.mongo.port or default value if undefined
     * @param prefix The prefix to use
     */
    public int getMongoPort(String prefix) {
        return getInt(prefix + Key.PERSISTENCE_MONGO_PORT, Default.PERSISTENCE_MONGO_PORT);
    }

    /**
     * @return persistence.mongo.username or null if undefined
     * @param prefix The prefix to use
     */
    public String getMongoUsername(String prefix) {
        return getString(prefix + Key.PERSISTENCE_MONGO_USERNAME, null);
    }

    /**
     * @return persistence.mongo.password or null if undefined
     * @param prefix The prefix to use
     */
    public String getMongoPassword(String prefix) {
        return getString(prefix + Key.PERSISTENCE_MONGO_PASSWORD, null);
    }

    /**
     * @return persistence.mongo.authdb or null if undefined
     * @param prefix The prefix to use
     */
    public String getMongoAuthDB(String prefix) {
        return getString(prefix + Key.PERSISTENCE_MONGO_AUTHDB, null);
    }

    /**
     * @return persistence.mongo.auth or default value if undefined
     * @param prefix The prefix to use
     */
    public boolean isMongoAuth(String prefix) {
        return getBoolean(prefix + Key.PERSISTENCE_MONGO_AUTH, Default.PERSISTENCE_MONGO_AUTH);
    }

    /**
     * @return persistence.mongo.dbname or default value if undefined
     * @param prefix The prefix to use
     */
    public String getMongoDbName(String prefix) {
        return getString(prefix + Key.PERSISTENCE_MONGO_DBNAME, Default.PERSISTENCE_MONGO_DBNAME);
    }

    /**
     * @return persistence.mongo.embedded or default value if undefined
     * @param prefix The prefix to use
     */
    public boolean isMongoEmbedded(String prefix) {
        return getBoolean(prefix + Key.PERSISTENCE_MONGO_EMBEDDED, Default.PERSISTENCE_MONGO_EMBEDDED);
    }

    /**
     * @return session.cookie.expires or default value if undefined
     */
    public boolean isSessionCookieExpires() {
        return getBoolean(Key.SESSION_COOKIE_EXPIRES, Default.SESSION_COOKIE_EXPIRES);
    }

    /**
     * @return authentication.cookie.expires or default value if undefined
     */
    public boolean isAuthenticationCookieExpires() {
        return getBoolean(Key.AUTHENTICATION_COOKIE_EXPIRES, Default.AUTHENTICATION_COOKIE_EXPIRES);
    }

    /**
     * @return authentication.cookie.expires or default value if undefined
     */
    public long getAuthenticationCookieTokenExpires() {
        return getLong(Key.AUTHENTICATION_COOKIE_TOKEN_EXPIRES, Default.AUTHENTICATION_COOKIE_TOKEN_EXPIRES);
    }

    /**
     * @return smtp.authentication or default value if undefined
     */
    public boolean isSmtpAuthentication() {
        return getBoolean(Key.SMTP_AUTHENTICATION, Default.SMTP_AUTHENTICATION);
    }

    /**
     * @return mongo.enable or default value if undefined
     */
    public boolean isPersistenceEnabled() {
        return getBoolean(Key.PERSISTENCE_ENABLE, Default.PERSISTENCE_ENABLE);
    }

    /**
     * @return smtp.protocol or default value if undefined
     */
    public String getSmtpProtocol() {
        return getString(Key.SMTP_PROTOCOL, Default.SMTP_PROTOCOL);
    }
}