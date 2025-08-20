package io.mangoo.core;

import com.google.common.collect.ImmutableMap;
import io.mangoo.TestExtension;
import io.mangoo.constants.Default;
import io.mangoo.constants.Key;
import io.mangoo.crypto.Vault;
import io.mangoo.enums.Mode;
import io.mangoo.utils.CodecUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class ConfigTest {
    @TempDir
    Path tempDir;

    /**
     * Adds a dot-separated key into a nested map structure.
     *
     * @param map  The map to which the key-value pair is added.
     * @param key  The dot-separated key (e.g., "application.name").
     * @param value The value to associate with the key.
     */
    @SuppressWarnings("unchecked")
    private static void addDotSeparatedKey(Map<String, Object> map, String key, Object value) {
        String[] parts = key.split("\\.");
        Map<String, Object> currentMap = map;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            if (i == parts.length - 1) {
                // Final part of the key: set the value
                currentMap.put(part, value);
            } else {
                // Intermediate part: ensure the map exists
                currentMap = (Map<String, Object>) currentMap.computeIfAbsent(part, k -> new HashMap<>());
            }
        }
    }

    @BeforeAll
    public static void setup(){
        Vault vault = Application.getInstance(Vault.class);
        vault.put("application.foo", "admin");
        vault.put("application.admin.username", "admin");
        vault.put("application.admin.password", "admin");
        vault.put("application.foobar", "essos");
        vault.put("application.bar", "westeros");
    }

    @AfterAll
    public static void cleanUp() throws IOException {
        System.clearProperty(Key.APPLICATION_CONFIG);
    }

    @Test
    void testValidConfig() {
        // given
        System.clearProperty(Key.APPLICATION_CONFIG);
        final Config config = Application.getInstance(Config.class);

        // then
        assertThat(config.isValid(), equalTo(true));
    }

    @Test
    void testArgDefaultValue() {
        // given
        System.clearProperty(Key.APPLICATION_CONFIG);
        final Config config = Application.getInstance(Config.class);

        // then
        assertThat(config.isValid(), equalTo(true));
        assertThat(config.getString("application.url"), equalTo("http://localhost:4223"));
    }

    @Test
    void testFlashCookieName() {
        // given
        System.clearProperty(Key.APPLICATION_CONFIG);
        final Config config = Application.getInstance(Config.class);

        // then
        assertThat(config.getFlashCookieName(), equalTo("test-flash"));
    }

    @Test
    void testGetSessionCookieName() throws IOException {
        // given
        String sessionCookieName = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.name", sessionCookieName);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSessionCookieName(), equalTo(sessionCookieName));
    }

    @Test
    void testGetSessionCookieNameDefaultValue() throws IOException {
        //given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSessionCookieName(), equalTo(Default.SESSION_COOKIE_NAME));
    }

    @Test
    void testGetApplicationSecret() throws IOException {
        // given
        String applicationSecret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", applicationSecret);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getApplicationSecret(), equalTo(applicationSecret));
    }

    @Test
    void testGetAuthenticationCookieName() throws IOException {
        // given
        String authenticationCookieName = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.cookie.name", authenticationCookieName);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getAuthenticationCookieName(), equalTo(authenticationCookieName));
    }

    @Test
    void testGetAuthenticationCookieNameDefaultValue() throws IOException {
        //given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getAuthenticationCookieName(), equalTo(Default.AUTHENTICATION_COOKIE_NAME));
    }

    @Test
    void testGetSessionCookieExpires() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String expires = "3600";

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.expires", expires);
        createTempConfig(configValues);
        System.setProperty("application.mode", Mode.TEST.toString().toLowerCase());
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSessionCookieTokenExpires(), equalTo(Long.valueOf(expires)));
    }

    @Test
    void testGetSessionCookieExpiresDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSessionCookieTokenExpires(), equalTo(Default.SESSION_COOKIE_TOKEN_EXPIRES));
    }

    @Test
    void testIsSessionCookieSecure() throws IOException {
        // given
        String secure = "true";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.secure", secure);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isSessionCookieSecure(), equalTo(Boolean.valueOf(secure)));
    }

    @Test
    void testIsSessionCookieSecureDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isSessionCookieSecure(), equalTo(Default.SESSION_COOKIE_SECURE));
    }

    @Test
    void testIsAuthenticationCookieSecure() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String secure = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.cookie.secure", secure);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isAuthenticationCookieSecure(), equalTo(Boolean.valueOf(secure)));
    }

    @Test
    void testIsAuthenticationCookieSecureDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isSessionCookieSecure(), equalTo(Default.AUTHENTICATION_COOKIE_SECURE));
    }

    @Test
    void testI18nCookieName() throws IOException {
        // given
        String name = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("i18n.cookie.name", name);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getI18nCookieName(), equalTo(name));
    }

    @Test
    void testI18nCookieNameDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getI18nCookieName(), equalTo(Default.I18N_COOKIE_NAME));
    }

    @Test
    void testIsFlashCookieSecure() throws IOException {
        // given
        String secure = "true";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.secure", secure);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isFlashCookieSecure(), equalTo(Boolean.valueOf(secure)));
    }

    @Test
    void testAplicationLanguage() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String language = "fr";

        // when
        Map<String, String> configValues = ImmutableMap.of("application.language", language);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getApplicationLanguage(), equalTo(language));
    }

    @Test
    void testApplicationLanguageDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getApplicationLanguage(), equalTo(Default.APPLICATION_LANGUAGE));
    }

    @Test
    void testIsSchedulerEnabled() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String enabled = "false";

        // when
        Map<String, String> configValues = ImmutableMap.of("scheduler.enable", enabled);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isSchedulerEnabled(), equalTo(Boolean.valueOf(enabled)));
    }

    @Test
    void testIsSchedulerEnabledDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isSchedulerEnabled(), equalTo(Default.SCHEDULER_ENABLE));
    }

    @Test
    void testGetApplicationAdminUsername() throws IOException {
        // given
        String username = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.admin.username", username);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getApplicationAdminUsername(), equalTo(username));
    }

    @Test
    void testGetApplicationAdminSecret() throws IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.admin.secret", secret);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getApplicationAdminSecret(), equalTo(secret));
    }

    @Test
    void testGetApplicationAdminSecretDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getApplicationAdminSecret(), equalTo(null));
    }

    @Test
    void testGetApplicationAdminPassword() throws IOException {
        // given
        String password = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.admin.password", password);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getApplicationAdminPassword(), equalTo(password));
    }

    @Test
    void testAuthenticationCookieRememberExpires() throws IOException {
        // given
        String expires = "6000";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.cookie.remember.expires", expires);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getAuthenticationCookieRememberExpires(), equalTo(Long.valueOf(expires)));
    }

    @Test
    void testAuthenticationCookieRememberExpiresDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getAuthenticationCookieRememberExpires(), equalTo(Default.AUTHENTICATION_COOKIE_REMEMBER_EXPIRES));
    }

    @Test
    void testGetApplicationController() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String controller = UUID.randomUUID().toString();

        // when
        Map<String, String> configValues = ImmutableMap.of("application.controller", controller);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getApplicationController(), equalTo(controller));
    }

    @Test
    void testGetApplicationControllerDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getApplicationController(), equalTo(Default.APPLICATION_CONTROLLER));
    }

    @Test
    void testValueFromSystemPropertyInProfile() {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        System.clearProperty(Key.APPLICATION_CONFIG);

        // when
        Config config = Application.getInstance(Config.class);
        String value = config.getString("application.test");

        // then
        assertThat(value, equalTo("valuefromarg"));
    }

    @Test
    void testValueFromSystemProperty() {
        // given
        System.clearProperty(Key.APPLICATION_CONFIG);
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Config config = Application.getInstance(Config.class);
        String applicationName = config.getApplicationName();

        // then
        assertThat(applicationName, equalTo("namefromarg"));
    }

    @Test
    void testIsApplicationAdminEnable() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String enable = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("application.admin.enable", enable);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isApplicationAdminEnable(), equalTo(Boolean.valueOf(enable)));
    }

    @Test
    void testIsApplicationAdminEnableDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isApplicationAdminEnable(), equalTo(Default.APPLICATION_ADMIN_ENABLE));
    }

    @Test
    void testGetSmptHost() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String host = "192.168.2.24";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.host", host);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSmtpHost(), equalTo(host));
    }

    @Test
    void testGetSmptHostDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSmtpHost(), equalTo(Default.SMTP_HOST));
    }

    @Test
    void testGetSmtpPort() throws IOException {
        // given
        String port = "555";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.port", port);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSmtpPort(), equalTo(Integer.valueOf(port)));
    }

    @Test
    void testGetSmtpPortDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSmtpPort(), equalTo(Default.SMTP_PORT));
    }

    @Test
    void testIsAuthentication() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String ssl = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.authentication", ssl);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isSmtpAuthentication(), equalTo(Boolean.valueOf(ssl)));
    }

    @Test
    void testIsSmtpAuthenticationDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isSmtpAuthentication(), equalTo(Default.SMTP_AUTHENTICATION));
    }

    @Test
    void testGetSmtpUsername() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String username = "smtpuser";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.username", username);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSmtpUsername(), equalTo(username));
    }

    @Test
    void testGetSmtpUsernameDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSmtpUsername(), equalTo(null));
    }

    @Test
    void testGetSmtpPassword() throws IOException {
        // given
        String password = "smtppass";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.password", password);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSmtpPassword(), equalTo(password));
    }

    @Test
    void testGetSmtpPasswordDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSmtpUsername(), equalTo(null));
    }

    @Test
    void testGetSmtpFrom() throws IOException {
        // given
        String from = "smtpform";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.from", from);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSmtpFrom(), equalTo(from));
    }

    @Test
    void testGetSmtpFromDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSmtpFrom(), equalTo(Default.SMTP_FROM));
    }

    @Test
    void testGetConnectorHttpsHost() throws IOException {
        // given
        String host = "192.168.3.24";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("connector.https.host", host);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getConnectorHttpsHost(), equalTo(host));
    }

    @Test
    void testGetConnectorHttpsHostDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getConnectorHttpsHost(), equalTo(null));
    }

    @Test
    void testGetConnectorHttpsPort() throws IOException {
        // given
        String port = "2542";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("connector.https.port", port);
        createTempConfig(configValues);
        System.setProperty("application.mode", Mode.TEST.toString().toLowerCase());
        Config config = new Config(new Vault());

        // then
        assertThat(config.getConnectorHttpsPort(), equalTo(Integer.valueOf(port)));
    }

    @Test
    void testGetConnectorHttpsPortDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getConnectorHttpsPort(), equalTo(0));
    }

    @Test
    void testGetConnectorHttpHost() throws IOException {
        // given
        String host = "192.168.2.42";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("connector.http.host", host);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getConnectorHttpHost(), equalTo(host));
    }

    @Test
    void testGetConnectorHttpHostDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getConnectorHttpHost(), equalTo(null));
    }

    @Test
    void testGetConnectorHttpPort() throws IOException {
        // given
        String port = "2442";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("connector.http.port", port);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getConnectorHttpPort(), equalTo(Integer.valueOf(port)));
    }

    @Test
    void testGetConnectorHttpPortDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getConnectorHttpPort(), equalTo(0));
    }

    @Test
    void testMetricsEnable() throws IOException {
        // given
        String enable = "true";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("metrics.enable", enable);
        createTempConfig(configValues);
        System.setProperty("application.mode", Mode.TEST.toString().toLowerCase());
        Config config = new Config(new Vault());

        // then
        assertThat(config.isMetricsEnable(), equalTo(Boolean.valueOf(enable)));
    }

    @Test
    void testMetricsEnableDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isMetricsEnable(), equalTo(Default.METRICS_ENABLE));
    }

    @Test
    void testPersistenceEnable() throws IOException {
        // given
        String enable = "true";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("mongo.enable", enable);
        createTempConfig(configValues);
        System.setProperty("mongo.enable", Mode.TEST.toString().toLowerCase());
        Config config = new Config(new Vault());

        // then
        assertThat(config.isPersistenceEnabled(), equalTo(Boolean.valueOf(enable)));
    }

    @Test
    void testPersistenceEnableDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isPersistenceEnabled(), equalTo(Default.PERSISTENCE_ENABLE));
    }

    @Test
    void testAuthenticationLock() throws IOException {
        // given
        String lock = "24";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.lock", lock);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getAuthenticationLock(), equalTo(Integer.valueOf(lock)));
    }

    @Test
    void testAuthenticationLockDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getAuthenticationLock(), equalTo(Default.AUTHENTICATION_LOCK));
    }

    @Test
    void testGetUndertowMaxEntitySize() throws IOException {
        // given
        String size = "4096";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("undertow.maxentitysize", size);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getUndertowMaxEntitySize(), equalTo(Long.valueOf(size)));
    }

    @Test
    void testGetUndertowMaxEntitySizeDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getUndertowMaxEntitySize(), equalTo(Default.UNDERTOW_MAX_ENTITY_SIZE));
    }

    @Test
    void testGetSessionCookieSecret() throws IOException {
        // given
        String key = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.secret", key);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSessionCookieSecret(), equalTo(key));
    }

    @Test
    void testGetSessionCookieSecretDefaultValue() throws IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSessionCookieSecret(), equalTo(secret));
    }

    @Test
    void testGetFlashCookieSecret() throws IOException {
        // given
        String key = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("flash.cookie.secret", key);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getFlashCookieSecret(), equalTo(key));
    }

    @Test
    void testGetFlashCookieEncryptionKeyDefaultValue() throws IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getFlashCookieSecret(), equalTo(secret));
    }

    @Test
    void testGetAuthenticationCookieSignKeyDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String secret = UUID.randomUUID().toString();

        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getAuthenticationCookieSecret(), equalTo(secret));
    }

    @Test
    void testGetAuthenticationCookieSecret() throws IOException {
        // given
        String key = UUID.randomUUID().toString();

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.cookie.secret", key);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getAuthenticationCookieSecret(), equalTo(key));
    }

    @Test
    void testGetAuthenticationCookieSecretDefaultValue() throws IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getAuthenticationCookieSecret(), equalTo(secret));
    }

    @Test
    void testGetString() {
        //given
        System.clearProperty(Key.APPLICATION_CONFIG);
        System.setProperty(Key.APPLICATION_MODE, Mode.PROD.toString());
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString(Key.CONNECTOR_HTTP_PORT), equalTo("10808"));
        assertThat(config.getString(Key.CONNECTOR_HTTP_PORT), equalTo("10808"));
    }

    @Test
    void testGetInt() {
        //given
        System.clearProperty(Key.APPLICATION_CONFIG);
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getInt(Key.CONNECTOR_HTTP_PORT), equalTo(10808));
    }

    @Test
    void testGetBoolean() {
        //given
        System.clearProperty(Key.APPLICATION_CONFIG);
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getBoolean(Key.APPLICATION_ADMIN_ENABLE), equalTo(true));
    }

    @Test
    void testGetLong() {
        //given
        System.clearProperty(Key.APPLICATION_CONFIG);
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getLong(Key.CONNECTOR_HTTP_PORT), equalTo(10808L));
    }

    @Test
    void testGetStringDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("foo", "bar"), equalTo("bar"));
    }

    @Test
    void testGetIntDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getInt("foo", 42), equalTo(42));
    }

    @Test
    void testGetBooleanDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getBoolean("foo", true), equalTo(true));
        assertThat(config.getBoolean("foo", false), equalTo(false));
    }

    @Test
    void testGetLongDefaultValue() {
        //given
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getLong("foo", 42), equalTo(42L));
    }

    @Test
    void testGetAllConfigurationValues() {
        //given
        System.clearProperty(Key.APPLICATION_CONFIG);
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getAllConfigurations(), not(nullValue()));
        assertThat(config.getAllConfigurations().size(), greaterThan(12));
    }

    @Test
    void testEnvironmentValues() {
        //given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        System.clearProperty(Key.APPLICATION_CONFIG);
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("smtp.username"), equalTo("bla"));
        assertThat(config.getString("smtp.port"), equalTo("3025"));
    }

    @Test
    void testEncryptedValue() {
        //given
        System.clearProperty(Key.APPLICATION_CONFIG);
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("application.foo"), equalTo("admin"));
    }

    @Test
    void testEncryptedValueMultiKeyLineTwo() {
        //given
        System.clearProperty(Key.APPLICATION_CONFIG);
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("application.bar"), equalTo("westeros"));
    }

    @Test
    void testEncryptedValueMultiKeyLineThree() {
        //given
        System.clearProperty(Key.APPLICATION_CONFIG);
        final Config config = Application.getInstance(Config.class);

        //then
        assertThat(config.getString("application.foobar"), equalTo("essos"));
    }

    @Test
    void testIsSmtpDebug() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String debug = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.debug", debug);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isSmtpDebug(), equalTo(Boolean.valueOf(debug)));
    }

    @Test
    void testIsSmtpDebugDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isSmtpDebug(), equalTo(Default.SMTP_DEBUG));
    }

    @Test
    void testGetSmtpProtocol() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String protocol = "smtptls";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.protocol", protocol);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSmtpProtocol(), equalTo(protocol));

    }

    @Test
    void testGetSmtpProtocolDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getSmtpProtocol(), equalTo(Default.SMTP_PROTOCOL));
    }

    @Test
    void testGetMongoAuthDB() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String authDb = "admin";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.authdb", authDb);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getMongoAuthDB("persistence."), equalTo(authDb));
    }

    @Test
    void testGetMongoDBName() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String mongodb = "mongodb";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.dbname", mongodb);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getMongoDbName("persistence."), equalTo(mongodb));
    }

    @Test
    void testGetMongoDBNameDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getMongoDbName("persistence."), equalTo(Default.PERSISTENCE_MONGO_DBNAME));
    }

    @Test
    void testGetMongoHost() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String host = "127.0.0.5";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.host", host);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getMongoHost("persistence."), equalTo(host));
    }

    @Test
    void testGetMongoHostDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getMongoHost(""), equalTo(Default.PERSISTENCE_MONGO_HOST));
    }

    @Test
    void testGetMongoPort() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String port = "47235";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.port", port);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getMongoPort("persistence."), equalTo(Integer.parseInt(port)));
    }

    @Test
    void testGetMongoPortDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getMongoPort("persistence."), equalTo(Default.PERSISTENCE_MONGO_PORT));
    }

    @Test
    void testGetMongoPassword() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String password = "thisismypassword";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.password", password);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getMongoPassword("persistence."), equalTo(password));
    }

    @Test
    void testGetMongoUsername() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String username = "thisismyusername";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.username", username);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getMongoUsername("persistence."), equalTo(username));
    }

    @Test
    void testCorsEnable() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String cors = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.enable", cors);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isCorsEnable(), equalTo(Boolean.valueOf(cors)));
    }

    @Test
    void testCorsEnableDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isCorsEnable(), equalTo(Default.CORS_ENABLE));
    }

    @Test
    void testGetCorsAllowOrigin() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String origin = "^http(s)?://(www.)?example.(com|org)$";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.alloworigin", origin);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getCorsAllowOrigin().toString(), equalTo(Pattern.compile(origin).toString()));
    }

    @Test
    void testGetCorsAllowOriginDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getCorsAllowOrigin().toString(), equalTo(Pattern.compile(Default.CORS_ALLOW_ORIGIN).toString()));
    }

    @Test
    void testGetCorsHeadersAllowCredentials() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String credentials = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.allowcredentials", credentials);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getCorsHeadersAllowCredentials(), equalTo(credentials));
    }

    @Test
    void testGetCorsHeadersAllowCredentialsDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getCorsHeadersAllowCredentials(), equalTo(Default.CORS_HEADERS_ALLOW_CREDENTIALS.toString()));
    }

    @Test
    void testGetCorsHeadersAllowHeaders() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String headers = "Authorization,Content-Type";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.allowheaders", headers);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getCorsHeadersAllowHeaders(), equalTo(headers));
    }

    @Test
    void testGetCorsHeadersAllowHeadersDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getCorsHeadersAllowHeaders(), equalTo(Default.CORS_HEADERS_ALLOW_HEADERS));
    }

    @Test
    void testIsAuthOrigin() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String authOrigin = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.origin", authOrigin);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isAuthOrigin(), equalTo(Boolean.valueOf(authOrigin)));
    }

    @Test
    void testIsAuthOriginDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.isAuthOrigin(), equalTo(Default.AUTHENTICATION_ORIGIN));
    }

    @Test
    void testGetApplicationAdminLocale() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String adminLocale = "de_DE";

        // when
        Map<String, String> configValues = ImmutableMap.of("application.admin.locale", adminLocale);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getApplicationAdminLocale(), equalTo(adminLocale));
    }

    @Test
    void testGetApplicationAdminLocaleDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getApplicationAdminLocale(), equalTo(Default.APPLICATION_ADMIN_LOCALE));
    }

    @Test
    void testGetCorsHeadersAllowMethods() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String headers = "GET,POST";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.allowmethods", headers);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getCorsHeadersAllowMethods(), equalTo(headers));
    }

    @Test
    void testGetCorsHeadersAllowMethodsDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getCorsHeadersAllowMethods(), equalTo(Default.CORS_HEADERS_ALLOW_METHODS));
    }

    @Test
    void testGetCorsHeadersExposeHeaders() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String headers = "Accept-Ranges,Content-Length";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.exposeheaders", headers);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getCorsHeadersExposeHeaders(), equalTo(headers));
    }

    @Test
    void testGetCorsHeadersExposeHeadersDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getCorsHeadersExposeHeaders(), equalTo(Default.CORS_HEADERS_EXPOSE_HEADERS));
    }

    @Test
    void testGetCorsHeadersMaxAge() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String maxage = "86400";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.maxage", maxage);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getCorsHeadersMaxAge(), equalTo(maxage));
    }

    @Test
    void testGetCorsHeadersMaxAgeDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getCorsHeadersMaxAge(), equalTo(Default.CORS_HEADERS_MAX_AGE));
    }

    @Test
    void testGetCorsUrlPattern() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String pattern = "^http(s)?://([^/]+)(:([^/]+))?(/([^/])+)?/api(/.*)?$";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.urlpattern", pattern);
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getCorsUrlPattern().toString(), equalTo(Pattern.compile(pattern).toString()));
    }

    @Test
    void testGetCorsUrlPatternDefaultValue() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = new HashMap<>();
        createTempConfig(configValues);
        Config config = new Config(new Vault());

        // then
        assertThat(config.getCorsUrlPattern().toString(), equalTo(Pattern.compile(Default.CORS_URL_PATTERN).toString()));
    }

    @Test
    void testCheckAnnotationExists() throws ClassNotFoundException {
        // given
        List<String> annotations = List.of("io.mangoo.annotations.Collection", "io.mangoo.annotations.Indexed", "io.mangoo.annotations.Run");

        // when
        for (String annotation : annotations) {
            Class<?> result = Class.forName(annotation);

            //then
            assertThat(result, not(nullValue()));
        }
    }

    private void createTempConfig(Map<String, String> values) {
        Path configTempFile = tempDir.resolve(CodecUtils.uuidV6());

        try {
            // Create the main configuration map
            Map<String, Object> config = new HashMap<>();

            // Add the default section
            Map<String, Object> defaultConfig = new HashMap<>();
            config.put("default", defaultConfig);

            // Add a key like "application.name" under the default entry
            values.forEach((key, value) -> {
                addDotSeparatedKey(defaultConfig, key, value);
            });

            // Add environments section
            Map<String, Object> environments = new HashMap<>();
            config.put("environments", environments);

            // Add development environment
            Map<String, Object> developmentConfig = new HashMap<>();
            environments.put("test", developmentConfig);
            addDotSeparatedKey(developmentConfig, "application.debug", "true");

            // Add production environment
            Map<String, Object> productionConfig = new HashMap<>();
            environments.put("prod", productionConfig);
            addDotSeparatedKey(productionConfig, "application.debug", "false");

            // Configure YAML DumperOptions
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK); // Use block style
            options.setPrettyFlow(true);

            // Create Yaml instance
            Yaml yaml = new Yaml(options);

            // Write YAML to a file
            try (Writer writer = new FileWriter(configTempFile.toAbsolutePath().toString())) {
                yaml.dump(config, writer);
            }

            System.setProperty(Key.APPLICATION_CONFIG, configTempFile.toAbsolutePath().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}