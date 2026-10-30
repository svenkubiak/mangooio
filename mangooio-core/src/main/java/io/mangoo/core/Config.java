package io.mangoo.core;

import com.google.common.io.Resources;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.constants.Const;
import io.mangoo.constants.Default;
import io.mangoo.constants.Key;
import io.mangoo.enums.Mode;
import io.mangoo.persistence.MongoSchema;
import io.mangoo.utils.internal.MangooUtils;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class Config {
    private static final Logger LOG = LogManager.getLogger(Config.class);
    private static final String OPTIONAL = "?";
    private static final long MAX_SECRET_FILE_SIZE = 64 * 1024L;
    private static final Pattern SECRET_FILE_NAME = Pattern.compile("[A-Za-z0-9_-][A-Za-z0-9._-]*");
    private static final Set<String> SECRETS = Set.of(
            Key.APPLICATION_SECRET, Key.APPLICATION_ADMIN_SECRET, Key.APPLICATION_API_KEY,
            Key.SESSION_COOKIE_SECRET, Key.SESSION_COOKIE_KEY,
            Key.AUTHENTICATION_COOKIE_SECRET, Key.AUTHENTICATION_COOKIE_KEY,
            Key.FLASH_COOKIE_SECRET, Key.FLASH_COOKIE_KEY,
            Key.SMTP_PASSWORD, Key.CONNECTOR_HTTPS_PASSWORD);
    private final Map<String, String> values = new ConcurrentHashMap<>();
    private final Set<String> external = ConcurrentHashMap.newKeySet();
    private final Set<String> errors = new TreeSet<>();
    private final Path secretsPath;
    private final boolean externalConfig;
    private Pattern corsUrl;
    private Pattern corsAllowOrigin;
    private boolean valid;

    @Inject
    public Config() {
        this(secretsPath());
    }

    Config(Path secretsPath) {
        this.secretsPath = Objects.requireNonNull(secretsPath, "secretsPath can not be null");
        this.externalConfig = StringUtils.isNotBlank(System.getProperty(Key.APPLICATION_CONFIG));
        load();
    }

    private static Path secretsPath() {
        String path = System.getenv("APPLICATION_SECRETS_PATH");
        if (StringUtils.isBlank(path)) {
            path = System.getProperty(Key.APPLICATION_SECRETS_PATH);
        }

        return Path.of(StringUtils.defaultIfBlank(path, Default.APPLICATION_SECRETS_PATH));
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
                MangooUtils.mergeMaps(mergedConfig, activeEnvironment);

                Map<String, String> falttenedMap = MangooUtils.flattenMap(mergedConfig);
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

    // Placeholders are resolved from their source first; a default given in the braces is only used as fallback.
    private void parse(String key, String value) {
        if (value.startsWith("env{")) {
            resolve(key, value, "env{", System.getenv(toEnvKey(key)));
        } else if (value.startsWith("arg{")) {
            resolve(key, value, "arg{", System.getProperty(key));
        } else if (value.startsWith("file{")) {
            resolve(key, value, "file{", readSecretFile(key));
        } else if (value.startsWith("vault{")) {
            errors.add(key + " uses vault{}, which was removed in 11.0.0. Use env{}, arg{} or file{} instead");
        } else {
            values.put(key, value);
        }
    }

    // An optional placeholder without value leaves the key unset, as if it were not in config.yaml, so the default applies
    private void resolve(String key, String value, String tag, String resolved) {
        if (StringUtils.isNotBlank(resolved)) {
            values.put(key, resolved);
            external.add(key);
        } else {
            String fallback = StringUtils.substringBetween(value, tag, "}");
            if (OPTIONAL.equals(fallback)) {
                return;
            }
            if (StringUtils.isNotBlank(fallback)) {
                values.put(key, fallback);
            } else {
                errors.add(key + " has no value, " + tag + "} is not set and has no fallback");
            }
        }
    }

    // Mounted secrets (Kubernetes, Docker) are files named like the key; symbolic links are followed, as Kubernetes mounts through them
    private String readSecretFile(String key) {
        if (!SECRET_FILE_NAME.matcher(key).matches()) {
            errors.add(key + " can not be read with file{}, as it is not a valid file name");
            return null;
        }

        var file = secretsPath.resolve(key);
        try {
            if (!Files.isRegularFile(file)) {
                return null;
            }
            if (Files.size(file) > MAX_SECRET_FILE_SIZE) {
                errors.add(key + " can not be read from " + file + ", as it is larger than 64 KiB");
                return null;
            }

            return stripLineBreak(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException e) {
            errors.add(key + " can not be read from " + file + ": " + e.getMessage());
            return null;
        }
    }

    // Only the line break an editor or echo appends is removed, as secrets must keep their exact length
    static String stripLineBreak(String value) {
        if (value.endsWith("\r\n")) {
            return value.substring(0, value.length() - 2);
        }
        if (value.endsWith("\n")) {
            return value.substring(0, value.length() - 1);
        }

        return value;
    }

    static boolean isSecret(String key) {
        return SECRETS.contains(key) || (key.startsWith(Default.PERSISTENCE_PREFIX) && key.endsWith("." + Key.PERSISTENCE_MONGO_PASSWORD));
    }

    private static String toEnvKey(String key) {
        return key.toUpperCase(Locale.ENGLISH)
                .replace(".", "_") // NOSONAR
                .trim();
    }

    /** Fails for unresolved placeholders and, in prod mode, for secrets written literally into the config.yaml of the classpath. */
    public void validate() {
        Set<String> failures = new TreeSet<>(errors);
        if (Application.getMode() == Mode.PROD && !externalConfig) {
            getLiteralSecrets().forEach(key -> failures.add(key + " is a secret and must not be set literally in prod, use env{}, arg{} or file{}"));
        }

        failures.forEach(LOG::error);
        if (!failures.isEmpty()) {
            valid = false;
        }
    }

    // A fallback in the braces counts as literal, as it is written into the config.yaml as well; an empty value holds no secret
    Set<String> getLiteralSecrets() {
        return values.entrySet().stream()
                .filter(entry -> isSecret(entry.getKey()))
                .filter(entry -> StringUtils.isNotBlank(entry.getValue()))
                .filter(entry -> !external.contains(entry.getKey()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toCollection(TreeSet::new));
    }

    Set<String> getErrors() {
        return Collections.unmodifiableSet(errors);
    }

    public Properties toProperties() {
        var properties = new Properties();
        properties.putAll(values);

        return properties;
    }

    public String getString(String key) {
        return values.get(key);
    }

    public String getString(String key, String defaultValue) {
        return values.getOrDefault(key, defaultValue);
    }

    public int getInt(String key) {
        final String value = values.get(key);
        if (StringUtils.isBlank(value)) {
            return 0;
        }

        return Integer.parseInt(value);
    }

    public long getLong(String key) {
        final String value = values.get(key);
        if (StringUtils.isBlank(value)) {
            return 0;
        }

        return Long.parseLong(value);
    }

    public long getLong(String key, long defaultValue) {
        final String value = values.get(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }

        return Long.parseLong(value);
    }

    public int getInt(String key, int defaultValue) {
        final String value = values.get(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }

        return Integer.parseInt(value);
    }

    public Boolean getBoolean(String key) {
        final String value = values.get(key);
        if (StringUtils.isBlank(value)) {
            return Boolean.FALSE;
        }

        return Boolean.valueOf(value);
    }

    public Boolean getBoolean(String key, Boolean defaultValue) {
        final String value = values.get(key);
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }

        return Boolean.valueOf(value);
    }

    public Map<String, String> getAllConfigurations() {
        return new ConcurrentHashMap<>(values);
    }

    public String getApplicationName() {
        return getString(Key.APPLICATION_NAME, Default.APPLICATION_NAME);
    }

    public String getFlashCookieName() {
        return getString(Key.FLASH_COOKIE_NAME, Default.FLASH_COOKIE_NAME);
    }

    public String getSessionCookieName() {
        return getString(Key.SESSION_COOKIE_NAME, Default.SESSION_COOKIE_NAME);
    }

    public String getApplicationSecret() {
        return getString(Key.APPLICATION_SECRET);
    }

    public String getAuthenticationCookieName() {
        return getString(Key.AUTHENTICATION_COOKIE_NAME, Default.AUTHENTICATION_COOKIE_NAME);
    }

    public long getSessionCookieTokenExpires() {
        return getLong(Key.SESSION_COOKIE_TOKEN_EXPIRES, Default.SESSION_COOKIE_TOKEN_EXPIRES);
    }

    public boolean isSessionCookieSecure() {
        return getBoolean(Key.SESSION_COOKIE_SECURE, Default.SESSION_COOKIE_SECURE);
    }

    public boolean isAuthenticationCookieSecure() {
        return getBoolean(Key.AUTHENTICATION_COOKIE_SECURE, Default.AUTHENTICATION_COOKIE_SECURE);
    }

    public String getI18nCookieName() {
        return getString(Key.I18N_COOKIE_NAME, Default.I18N_COOKIE_NAME);
    }

    public boolean isFlashCookieSecure() {
        return isSessionCookieSecure();
    }

    public String getApplicationLanguage() {
        return getString(Key.APPLICATION_LANGUAGE, Default.APPLICATION_LANGUAGE);
    }

    public String getApplicationAdminUsername() {
        return getString(Key.APPLICATION_ADMIN_USERNAME, null);
    }

    public String getApplicationAdminPassword() {
        return getString(Key.APPLICATION_ADMIN_PASSWORD, null);
    }

    public long getAuthenticationCookieRememberExpires() {
        return getLong(Key.AUTHENTICATION_COOKIE_REMEMBER_EXPIRES, Default.AUTHENTICATION_COOKIE_REMEMBER_EXPIRES);
    }

    public boolean isApplicationAdminEnable() {
        return getBoolean(Key.APPLICATION_ADMIN_ENABLE, Default.APPLICATION_ADMIN_ENABLE);
    }

    /** If enabled, a request whose query parameter collides with a route parameter is rejected; the route value always wins. */
    public boolean isParameterStrict() {
        return getBoolean(Key.APPLICATION_PARAMETER_STRICT, Default.APPLICATION_PARAMETER_STRICT);
    }

    public String getSmtpHost() {
        return getString(Key.SMTP_HOST, Default.SMTP_HOST);
    }

    public int getSmtpPort() {
        return getInt(Key.SMTP_PORT, Default.SMTP_PORT);
    }

    public String getSmtpUsername() {
        return getString(Key.SMTP_USERNAME, null);
    }

    public String getSmtpPassword() {
        return getString(Key.SMTP_PASSWORD, null);
    }

    public String getSmtpFrom() {
        return getString(Key.SMTP_FROM, Default.SMTP_FROM);
    }

    public String getConnectorHttpHost() {
        return getString(Key.CONNECTOR_HTTP_HOST, null);
    }

    public int getConnectorHttpPort() {
        return getInt(Key.CONNECTOR_HTTP_PORT, 0);
    }

    public String getConnectorHttpsHost() {
        return getString(Key.CONNECTOR_HTTPS_HOST, null);
    }

    public int getConnectorHttpsPort() {
        return getInt(Key.CONNECTOR_HTTPS_PORT, 0);
    }

    public boolean isConnectorProxy() {
        return getBoolean(Key.CONNECTOR_PROXY, Default.CONNECTOR_PROXY);
    }

    /** Seconds the shutdown waits in total for open requests and running scheduled tasks. */
    public long getApplicationShutdownTimeout() {
        return Math.max(0, getLong(Key.APPLICATION_SHUTDOWN_TIMEOUT, Default.APPLICATION_SHUTDOWN_TIMEOUT));
    }

    public boolean isMetricsEnable() {
        return getBoolean(Key.METRICS_ENABLE, Default.METRICS_ENABLE);
    }

    /** Failed attempts allowed per identifier, counted separately for the password and the second factor step. */
    public int getAuthenticationLock() {
        return getInt(Key.AUTHENTICATION_LOCK, Default.AUTHENTICATION_LOCK);
    }

    /** Lock duration in minutes; the lock is not extended by further failed attempts. */
    public int getAuthenticationLockDuration() {
        return getInt(Key.AUTHENTICATION_LOCK_DURATION, Default.AUTHENTICATION_LOCK_DURATION);
    }

    /** Limits concurrent Argon2 computations, as each occupies the configured memory; 0 derives the limit from the heap (clamped to 2 to 8). */
    public int getAuthenticationHashingConcurrency() {
        return getInt(Key.AUTHENTICATION_HASHING_CONCURRENCY, Default.AUTHENTICATION_HASHING_CONCURRENCY);
    }

    /** Milliseconds to wait for a free hashing slot before a {@link io.mangoo.exceptions.MangooHashingException} is thrown. */
    public long getAuthenticationHashingTimeout() {
        return getLong(Key.AUTHENTICATION_HASHING_TIMEOUT, Default.AUTHENTICATION_HASHING_TIMEOUT);
    }

    /** Changing this does not invalidate stored hashes; {@link io.mangoo.utils.CommonUtils#needsRehash(String)} reports them as outdated. */
    public int getAuthenticationHashingMemory() {
        return getInt(Key.AUTHENTICATION_HASHING_MEMORY, Default.AUTHENTICATION_HASHING_MEMORY);
    }

    /** Changing this does not invalidate stored hashes; {@link io.mangoo.utils.CommonUtils#needsRehash(String)} reports them as outdated. */
    public int getAuthenticationHashingIterations() {
        return getInt(Key.AUTHENTICATION_HASHING_ITERATIONS, Default.AUTHENTICATION_HASHING_ITERATIONS);
    }

    /** Lanes are computed sequentially, so raising this does not increase CPU utilization. Changing it does not invalidate stored hashes. */
    public int getAuthenticationHashingParallelism() {
        return getInt(Key.AUTHENTICATION_HASHING_PARALLELISM, Default.AUTHENTICATION_HASHING_PARALLELISM);
    }

    public long getUndertowMaxEntitySize() {
        return getLong(Key.UNDERTOW_MAX_ENTITY_SIZE, Default.UNDERTOW_MAX_ENTITY_SIZE);
    }

    /** Per-file limit; a request body exceeding undertow.maxentitysize is already rejected before. */
    public long getFormMaxFileSize() {
        return getLong(Key.FORM_MAX_FILE_SIZE, Default.FORM_MAX_FILE_SIZE);
    }

    public byte[] getSessionCookieSecret() {
        return getString(Key.SESSION_COOKIE_SECRET, getApplicationSecret()).getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getAuthenticationCookieSecret() {
        return getString(Key.AUTHENTICATION_COOKIE_SECRET, getApplicationSecret()).getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getFlashCookieSecret() {
        return getString(Key.FLASH_COOKIE_SECRET, getApplicationSecret()).getBytes(StandardCharsets.UTF_8);
    }

    public boolean isSchedulerEnabled() {
        return getBoolean(Key.SCHEDULER_ENABLE, Default.SCHEDULER_ENABLE);
    }

    public String getApplicationAdminSecret() {
        return getString(Key.APPLICATION_ADMIN_SECRET, null);
    }

    public boolean isSmtpDebug() {
        return getBoolean(Key.SMTP_DEBUG, Default.SMTP_DEBUG);
    }

    public boolean isCorsEnable() {
        return getBoolean(Key.CORS_ENABLE, Default.CORS_ENABLE);
    }

    public Pattern getCorsUrlPattern() {
        if (corsUrl == null) {
            corsUrl = Pattern.compile(getString(Key.CORS_URL_PATTERN, Default.CORS_URL_PATTERN));
        }
        return corsUrl;
    }

    public Pattern getCorsAllowOrigin() {
        if (corsAllowOrigin == null) {
            corsAllowOrigin = Pattern.compile(getString(Key.CORS_ALLOW_ORIGIN, Default.CORS_ALLOW_ORIGIN));
        }

        return corsAllowOrigin;
    }

    public String getCorsHeadersAllowCredentials() {
        return getString(Key.CORS_HEADERS_ALLOW_CREDENTIALS, Default.CORS_HEADERS_ALLOW_CREDENTIALS.toString());
    }

    public String getCorsHeadersAllowHeaders() {
        return getString(Key.CORS_HEADERS_ALLOW_HEADERS, Default.CORS_HEADERS_ALLOW_HEADERS);
    }

    public String getCorsHeadersAllowMethods() {
        return getString(Key.CORS_HEADERS_ALLOW_METHODS, Default.CORS_HEADERS_ALLOW_METHODS);
    }

    public String getCorsHeadersExposeHeaders() {
        return getString(Key.CORS_HEADERS_EXPOSE_HEADERS, Default.CORS_HEADERS_EXPOSE_HEADERS);
    }

    public String getCorsHeadersMaxAge() {
        return getString(Key.CORS_HEADERS_MAX_AGE, Default.CORS_HEADERS_MAX_AGE);
    }

    public String getMongoHost(String prefix) {
        return getString(prefix + Key.PERSISTENCE_MONGO_HOST, Default.PERSISTENCE_MONGO_HOST);
    }

    public int getMongoPort(String prefix) {
        return getInt(prefix + Key.PERSISTENCE_MONGO_PORT, Default.PERSISTENCE_MONGO_PORT);
    }

    public String getMongoUrl(String prefix) {
        return getString(prefix + Key.PERSISTENCE_MONGO_URL);
    }

    public String getMongoUsername(String prefix) {
        return getString(prefix + Key.PERSISTENCE_MONGO_USERNAME, null);
    }

    public String getMongoPassword(String prefix) {
        return getString(prefix + Key.PERSISTENCE_MONGO_PASSWORD, null);
    }

    public String getMongoAuthDB(String prefix) {
        return getString(prefix + Key.PERSISTENCE_MONGO_AUTHDB, null);
    }

    public Boolean isMongoAuth(String prefix) {
        return getBoolean(prefix + Key.PERSISTENCE_MONGO_AUTH, Default.PERSISTENCE_MONGO_AUTH);
    }

    public String getMongoDbName(String prefix) {
        return getString(prefix + Key.PERSISTENCE_MONGO_DBNAME, Default.PERSISTENCE_MONGO_DBNAME);
    }

    /** Applies to the default datastore only, as the startup creates collections and indexes there only. */
    public String getMongoSchema() {
        return getString(Key.PERSISTENCE_MONGO_SCHEMA, Default.PERSISTENCE_MONGO_SCHEMA);
    }

    public boolean isMongoSchemaCreate() {
        return !MongoSchema.VALIDATE.equals(getMongoSchema());
    }

    public Boolean isMongoEmbedded(String prefix) {
        return getBoolean(prefix + Key.PERSISTENCE_MONGO_EMBEDDED, Default.PERSISTENCE_MONGO_EMBEDDED);
    }

    public Boolean isSessionCookieExpires() {
        return getBoolean(Key.SESSION_COOKIE_EXPIRES, Default.SESSION_COOKIE_EXPIRES);
    }

    public long getAuthenticationCookieTokenExpires() {
        return getLong(Key.AUTHENTICATION_COOKIE_TOKEN_EXPIRES, Default.AUTHENTICATION_COOKIE_TOKEN_EXPIRES);
    }

    public boolean isSmtpAuthentication() {
        return getBoolean(Key.SMTP_AUTHENTICATION, Default.SMTP_AUTHENTICATION);
    }

    public boolean isPersistenceEnabled() {
        return getBoolean(Key.PERSISTENCE_ENABLE, Default.PERSISTENCE_ENABLE);
    }

    public String getSmtpProtocol() {
        return getString(Key.SMTP_PROTOCOL, Default.SMTP_PROTOCOL);
    }

    public boolean isAuthOrigin() {
        return getBoolean(Key.AUTHENTICATION_ORIGIN, Default.AUTHENTICATION_ORIGIN);
    }

    public Object getApplicationAdminLocale() {
        return getString(Key.APPLICATION_ADMIN_LOCALE, Default.APPLICATION_ADMIN_LOCALE);
    }

    public String getAuthenticationCookieSameSiteMode() {
        return getString(Key.AUTHENTICATION_COOKIE_SAME_SITE_MODE, Default.AUTHENTICATION_COOKIE_SAME_SITE_MODE);
    }

    public String getSessionCookieSameSiteMode() {
        return getString(Key.SESSION_COOKIE_SAME_SITE_MODE, Default.SESSION_COOKIE_SAME_SITE_MODE);
    }

    public byte[] getSessionCookieKey() {
        return getString(Key.SESSION_COOKIE_KEY, getApplicationSecret()).getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getFlashCookieKey() {
        return getString(Key.FLASH_COOKIE_KEY, getApplicationSecret()).getBytes(StandardCharsets.UTF_8);
    }

    public byte[] getAuthenticationCookieKey() {
        return getString(Key.AUTHENTICATION_COOKIE_KEY, getApplicationSecret()).getBytes(StandardCharsets.UTF_8);
    }

    /** May be empty if the keystore holds exactly one key entry. */
    public String getConnectorHttpsCertificateAlias() {
        return getString(Key.CONNECTOR_HTTPS_CERTIFICATE_ALIAS, null);
    }

    public String getConnectorHttpsKeystore() {
        return getString(Key.CONNECTOR_HTTPS_KEYSTORE, null);
    }

    public String getConnectorHttpsPassword() {
        return getString(Key.CONNECTOR_HTTPS_PASSWORD, null);
    }

    public boolean isAuthenticationBlacklist() {
        return getBoolean(Key.AUTHENTICATION_BLACKLIST, Default.AUTHENTICATION_BLACKLIST);
    }

    public boolean isValid() {
        return valid;
    }

    public boolean isOtlpEnable() {
        return getBoolean(Key.OTLP_ENABLE, Default.OTLP_ENABLE);
    }

    public String getOtlpEndpoint() {
        return getString(Key.OTLP_ENDPOINT, null);
    }

    public boolean isValidationPassthrough() {
        return getBoolean(Key.APPLICATION_VALIDATION_PASSTHROUGH, Default.APPLICATION_VALIDATION_PASSTHROUGH);
    }

    public String getApplicationTimezone() {
        var timezone = getString(Key.APPLICATION_TIMEZONE);
        if (MangooUtils.isValidTimeZone(timezone)) {
            return timezone;
        }

        return Default.APPLICATION_TIMEZONE;
    }

    public ZoneId getApplicationTimeZone() {
        return ZoneId.of(getApplicationTimezone());
    }
}
