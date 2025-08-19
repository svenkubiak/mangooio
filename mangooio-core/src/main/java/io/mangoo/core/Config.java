package io.mangoo.core;

import com.google.common.io.Resources;
import com.google.re2j.Pattern;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.constants.Const;
import io.mangoo.constants.Default;
import io.mangoo.constants.Key;
import io.mangoo.constants.NotNull;
import io.mangoo.crypto.Vault;
import io.mangoo.utils.ConfigUtils;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class Config {
    private static final Logger LOG = LogManager.getLogger(Config.class);
    private static final String VAULT_TAG = "vault{}";
    private static final String ARG_TAG = "arg{}";
    private final Map<String, String> values = new ConcurrentHashMap<>();
    private final Vault vault;
    private Pattern corsUrl;
    private Pattern corsAllowOrigin;
    private boolean valid;

    public Config(Vault vault) {
        this.vault = Objects.requireNonNull(vault, NotNull.VAULT);
        load();
    }

    @SuppressWarnings("unchecked")
    private void load() {
        try (var inputStream = getConfigInputStream()){
            var loaderOptions = new LoaderOptions();
            loaderOptions.setAllowDuplicateKeys(false);
            loaderOptions.setMaxAliasesForCollections(5);

            var yaml = new Yaml(loaderOptions);
            Map<String, Object> config = yaml.load(inputStream);

            Map<String, Object> defaultConfig = (Map<String, Object>) config.get("default");
            Map<String, Object> environments = (Map<String, Object>) config.get("environments");

            String activeEnv = Application.getMode().toString().toLowerCase(Locale.ENGLISH);

            Map<String, Object> activeEnvironment = (Map<String, Object>) environments.get(activeEnv);
            if (activeEnvironment != null) {
                Map<String, Object> mergedConfig = new HashMap<>(defaultConfig);
                ConfigUtils.mergeMaps(mergedConfig, activeEnvironment);

                Map<String, String> falttenedMap = ConfigUtils.flattenMap(mergedConfig);
                falttenedMap.forEach(this::parse);

                valid = true;
            } else {
                LOG.error("Active environment '{}' not found in config.yaml", activeEnv);
            }
        } catch (Exception e) {
            LOG.error("Failed to load config.yaml", e);
        }
    }

    @SuppressFBWarnings(justification = "Intentionally used to access the file system", value = {"PATH_TRAVERSAL_IN", "URLCONNECTION_SSRF_FD"})
    private InputStream getConfigInputStream() throws IOException {
        String configPath = System.getProperty(Key.APPLICATION_CONFIG);
        InputStream inputStream;

        if (StringUtils.isNotBlank(configPath)) {
            inputStream = Files.newInputStream(Path.of(configPath));
        } else {
            inputStream = Resources.getResource(Const.CONFIG_FILE).openStream();
        }

        return inputStream;
    }

    /**
     * Parses a given property key and value and checks if the value comes from
     * a system property and maybe decrypts the value
     *
     * @param key The property key
     * @param value The property value
     */
    private void parse(String key, String value) {
        if (ARG_TAG.equals(value)) {
            String propertyValue = System.getProperty(key);

            if (StringUtils.isNotBlank(propertyValue)) {
                values.put(key, propertyValue);
            }
        } else if (value.startsWith("arg{")) {
            value = StringUtils.substringBetween(value, "arg{", "}");

            if (StringUtils.isNotBlank(value)) {
                values.put(key, value);
            }
        } else if (VAULT_TAG.equals(value)) {
            String propertyValue = vault.get(key);

            if (StringUtils.isNotBlank(propertyValue)) {
                values.put(key, propertyValue);
            }
        } else if (value.startsWith("vault{")) {
            value = StringUtils.substringBetween(value, "vault{", "}");

            if (StringUtils.isNotBlank(value)) {
                values.put(key, value);
            }
        } else {
            values.put(key, value);
        }
    }

    /**
     * Validates if each config value has been decrypted and parsed correctly
     */
    public void validate() {
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String value = entry.getValue();
            if (value != null && (value.startsWith(VAULT_TAG) || value.startsWith(ARG_TAG)) ) {
                LOG.error("{} has not been decrypted or parsed correctly", entry.getKey());
                valid = false;
            }
        }
    }

    /**
     * Converts config values to standard java properties
     *
     * @return Properties instance with config values
     */
    public Properties toProperties() {
        var properties = new Properties();
        properties.putAll(values);

        return properties;
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as String or null if the key is not configured
     */
    public String getString(String key) {
        return values.get(key);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as String or the passed defaultValue if the key is not configured
     */
    public String getString(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @return The configured value as int or 0 if the key is not configured
     */
    public int getInt(String key) {
        final String value = values.get(key);
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
        final String value = values.get(key);
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
        final String value = values.get(key);
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
        final String value = values.get(key);
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
    public Boolean getBoolean(String key) {
        final String value = values.get(key);
        if (StringUtils.isBlank(value)) {
            return Boolean.FALSE;
        }

        return Boolean.valueOf(value);
    }

    /**
     * Retrieves a configuration value with the given key
     *
     * @param key The key of the configuration value (e.g. application.name)
     * @param defaultValue The default value to return of no key is found
     * @return The configured value as boolean or the passed defaultValue if the key is not configured
     */
    public Boolean getBoolean(String key, Boolean defaultValue) {
        final String value = values.get(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }

        return Boolean.valueOf(value);
    }

    /**
     * @return All configuration options of the current environment
     */
    public Map<String, String> getAllConfigurations() {
        return new ConcurrentHashMap<>(values);
    }

    /**
     * @return application.name from config.yaml
     */
    public String getApplicationName() {
        return getString(Key.APPLICATION_NAME, Default.APPLICATION_NAME);
    }

    /**
     * @return flash.cookie.name or default value if undefined
     */
    public String getFlashCookieName() {
        return getString(Key.FLASH_COOKIE_NAME, Default.FLASH_COOKIE_NAME);
    }

    /**
     * @return session.cookie.name from config.yaml or default value if undefined
     */
    public String getSessionCookieName() {
        return getString(Key.SESSION_COOKIE_NAME, Default.SESSION_COOKIE_NAME);
    }

    /**
     * @return application.secret from config.yaml
     */
    public String getApplicationSecret() {
        return getString(Key.APPLICATION_SECRET);
    }

    /**
     * @return authentication.cookie.name from config.yaml or default value if undefined
     */
    public String getAuthenticationCookieName() {
        return getString(Key.AUTHENTICATION_COOKIE_NAME, Default.AUTHENTICATION_COOKIE_NAME);
    }

    /**
     * @return session.cookie.token.expires from config.yaml or default value if undefined
     */
    public long getSessionCookieTokenExpires() {
        return getLong(Key.SESSION_COOKIE_TOKEN_EXPIRES, Default.SESSION_COOKIE_TOKEN_EXPIRES);
    }

    /**
     * @return session.cookie.secure from config.yaml or default value if undefined
     */
    public boolean isSessionCookieSecure() {
        return getBoolean(Key.SESSION_COOKIE_SECURE, Default.SESSION_COOKIE_SECURE);
    }

    /**
     * @return authentication.cookie.secure from config.yaml or default value if undefined
     */
    public boolean isAuthenticationCookieSecure() {
        return getBoolean(Key.AUTHENTICATION_COOKIE_SECURE, Default.AUTHENTICATION_COOKIE_SECURE);
    }

    /**
     * @return i18n.cookie.name from config.yaml or default value if undefined
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
     * @return application.language from config.yaml or default value if undefined
     */
    public String getApplicationLanguage() {
        return getString(Key.APPLICATION_LANGUAGE, Default.APPLICATION_LANGUAGE);
    }

    /**
     * @return application.admin.username from config.yaml or null if undefined
     */
    public String getApplicationAdminUsername() {
        return getString(Key.APPLICATION_ADMIN_USERNAME, null);
    }

    /**
     * @return application.admin.password from config.yaml or null if undefined
     */
    public String getApplicationAdminPassword() {
        return getString(Key.APPLICATION_ADMIN_PASSWORD, null);
    }

    /**
     * @return authentication.cookie.remember.expires from config.yaml or default value if undefined
     */
    public long getAuthenticationCookieRememberExpires() {
        return getLong(Key.AUTHENTICATION_COOKIE_REMEMBER_EXPIRES, Default.AUTHENTICATION_COOKIE_REMEMBER_EXPIRES);
    }

    /**
     * @return application.controller from config.yaml or default value if undefined
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
     * @return connector.https.host or null if undefined
     */
    public String getConnectorHttpsHost() {
        return getString(Key.CONNECTOR_HTTPS_HOST, null);
    }

    /**
     * @return connector.https.port or 0 if undefined
     */
    public int getConnectorHttpsPort() {
        return getInt(Key.CONNECTOR_HTTPS_PORT, 0);
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
    public Boolean isMongoAuth(String prefix) {
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
    public Boolean isMongoEmbedded(String prefix) {
        return getBoolean(prefix + Key.PERSISTENCE_MONGO_EMBEDDED, Default.PERSISTENCE_MONGO_EMBEDDED);
    }

    /**
     * @return session.cookie.expires or default value if undefined
     */
    public Boolean isSessionCookieExpires() {
        return getBoolean(Key.SESSION_COOKIE_EXPIRES, Default.SESSION_COOKIE_EXPIRES);
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

    /**
     * @return authentication.origin or default value if undefined
     */
    public boolean isAuthOrigin() {
        return getBoolean(Key.AUTHENTICATION_ORIGIN, Default.AUTHENTICATION_ORIGIN);
    }

    /**
     * @return application.admin.locale or default value if undefined
     */
    public Object getApplicationAdminLocale() {
        return getString(Key.APPLICATION_ADMIN_LOCALE, Default.APPLICATION_ADMIN_LOCALE);
    }

    /**
     * @return authentication.cookie.samesitemode or default value if undefined
     */
    public String getAuthenticationCookieSameSiteMode() {
        return getString(Key.AUTHENTICATION_COOKIE_SAME_SITE_MODE, Default.AUTHENTICATION_COOKIE_SAME_SITE_MODE);
    }

    /**
     * @return session.cookie.samesitemode or default value if undefined
     */
    public String getSessionCookieSameSiteMode() {
        return getString(Key.SESSION_COOKIE_SAME_SITE_MODE, Default.SESSION_COOKIE_SAME_SITE_MODE);
    }

    /**
     *
     * @return application.vault.secret or null if undefined
     */
    public String getApplicationVaultSecret() {
        return getString(Key.APPLICATION_VAULT_SECRET, null);
    }

    /**
     *
     * @return application.vault.path or null if undefined
     */
    public String getApplicationVaultPath() {
        return getString(Key.APPLICATION_VAULT_SECRET, null);
    }

    /**
     * @return session.cookie.key or application.secret if undefined
     */
    public String getSessionCookieKey() {
        return getString(Key.SESSION_COOKIE_KEY, getApplicationSecret());
    }

    /**
     * @return flash.cookie.key or application.secret if undefined
     */
    public String getFlashCookieKey() {
        return getString(Key.FLASH_COOKIE_KEY, getApplicationSecret());
    }

    /**
     * @return authentication.cookie.key or application.secret if undefined
     */
    public String getAuthenticationCookieKey() {
        return getString(Key.FLASH_COOKIE_KEY, getApplicationSecret());
    }

    public boolean isValid() {
        return valid;
    }

}
