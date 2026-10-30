package io.mangoo.core;

import com.google.common.collect.ImmutableMap;
import io.mangoo.TestExtension;
import io.mangoo.constants.Default;
import io.mangoo.constants.Key;
import io.mangoo.enums.Mode;
import io.mangoo.utils.CommonUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith({TestExtension.class})
class ConfigTest {
    @TempDir
    Path tempDir;

    @SuppressWarnings("unchecked")
    private static void addDotSeparatedKey(Map<String, Object> map, String key, Object value) {
        String[] parts = key.split("\\.");
        Map<String, Object> currentMap = map;

        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];

            if (i == parts.length - 1) {
                currentMap.put(part, value);
            } else {
                currentMap = (Map<String, Object>) currentMap.computeIfAbsent(part, k -> new HashMap<>());
            }
        }
    }

    @AfterAll
    public static void cleanUp() throws IOException {
        System.clearProperty(Key.APPLICATION_CONFIG);
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
    void testArgDefaultValueIsOverriddenBySystemProperty() {
        // given
        String key = "application.argdefault";
        String value = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        System.setProperty(key, value);

        try {
            // when
            createTempConfig(ImmutableMap.of(key, "arg{default}"));
            Config config = new Config();

            // then
            assertThat(config.getString(key), equalTo(value));
        } finally {
            System.clearProperty(key);
        }
    }

    @Test
    void testArgDefaultValueWithoutSystemProperty() {
        // given
        String key = "application.argdefault";
        System.clearProperty(key);
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        createTempConfig(ImmutableMap.of(key, "arg{default}"));
        Config config = new Config();

        // then
        assertThat(config.getString(key), equalTo("default"));
    }

    @Test
    void testEnvDefaultValueIsOverriddenByEnvironmentVariable() {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        createTempConfig(ImmutableMap.of("path", "env{default}"));
        Config config = new Config();

        // then
        assertThat(System.getenv("PATH"), not(emptyOrNullString()));
        assertThat(config.getString("path"), equalTo(System.getenv("PATH")));
    }

    @Test
    void testEnvDefaultValueWithoutEnvironmentVariable() {
        // given
        String key = "application.env" + UUID.randomUUID().toString().replace("-", "");
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        createTempConfig(ImmutableMap.of(key, "env{default}"));
        Config config = new Config();

        // then
        assertThat(config.getString(key), equalTo("default"));
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
        Config config = new Config();

        // then
        assertThat(config.getSessionCookieName(), equalTo(sessionCookieName));
    }

    @Test
    void testGetApplicationSecret() throws IOException {
        // given
        String applicationSecret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", applicationSecret);
        createTempConfig(configValues);
        Config config = new Config();

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
        Config config = new Config();

        // then
        assertThat(config.getAuthenticationCookieName(), equalTo(authenticationCookieName));
    }

    @Test
    void testGetSessionCookieExpires() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String expires = "7200";

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.token.expires", expires);
        createTempConfig(configValues);
        System.setProperty("application.mode", Mode.TEST.toString().toLowerCase());
        Config config = new Config();

        // then
        assertThat(config.getSessionCookieTokenExpires(), equalTo(Long.valueOf(expires)));
    }

    @Test
    void testIsSessionCookieSecure() throws IOException {
        // given
        String secure = "true";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.secure", secure);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isSessionCookieSecure(), equalTo(Boolean.valueOf(secure)));
    }

    @Test
    void testIsAuthenticationCookieSecure() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String secure = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.cookie.secure", secure);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isAuthenticationCookieSecure(), equalTo(Boolean.valueOf(secure)));
    }

    @Test
    void testI18nCookieName() throws IOException {
        // given
        String name = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("i18n.cookie.name", name);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getI18nCookieName(), equalTo(name));
    }

    @Test
    void testIsFlashCookieSecure() throws IOException {
        // given
        String secure = "true";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.secure", secure);
        createTempConfig(configValues);
        Config config = new Config();

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
        Config config = new Config();

        // then
        assertThat(config.getApplicationLanguage(), equalTo(language));
    }

    @Test
    void testIsSchedulerEnabled() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String enabled = "false";

        // when
        Map<String, String> configValues = ImmutableMap.of("scheduler.enable", enabled);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isSchedulerEnabled(), equalTo(Boolean.valueOf(enabled)));
    }

    private Path secrets(Map<String, String> files) throws IOException {
        Path directory = Files.createDirectory(tempDir.resolve("secrets-" + CommonUtils.uuidV6()));
        for (Map.Entry<String, String> file : files.entrySet()) {
            Files.writeString(directory.resolve(file.getKey()), file.getValue(), StandardCharsets.UTF_8);
        }

        return directory;
    }

    @Test
    void testFileReadsSecretFromSecretsDirectory() throws IOException {
        // given
        Path secrets = secrets(Map.of("session.cookie.secret", "from-file\n"));

        // when
        createTempConfig(ImmutableMap.of("session.cookie.secret", "file{}"));
        Config config = new Config(secrets);

        // then
        assertThat(config.getString("session.cookie.secret"), equalTo("from-file"));
        assertThat(config.getErrors(), empty());
        assertThat(config.getLiteralSecrets(), empty());
    }

    @Test
    void testFileRemovesOnlyOneTrailingLineBreak() throws IOException {
        // given
        Path secrets = secrets(Map.of("application.windows", " value \r\n", "application.unix", " value \n\n"));

        // when
        createTempConfig(ImmutableMap.of("application.windows", "file{}", "application.unix", "file{}"));
        Config config = new Config(secrets);

        // then
        assertThat(config.getString("application.windows"), equalTo(" value "));
        assertThat(config.getString("application.unix"), equalTo(" value \n"));
    }

    @Test
    void testFileFollowsSymbolicLinks() throws IOException {
        // given a secret mounted the way Kubernetes does, through a link into a data directory
        Path secrets = secrets(Map.of());
        Path data = Files.createDirectory(secrets.resolve("..data"));
        Files.writeString(data.resolve("application.linked"), "linked", StandardCharsets.UTF_8);
        try {
            Files.createSymbolicLink(secrets.resolve("application.linked"), data.resolve("application.linked"));
        } catch (UnsupportedOperationException | IOException e) {
            assumeTrue(false, "Symbolic links are not supported");
        }

        // when
        createTempConfig(ImmutableMap.of("application.linked", "file{}"));
        Config config = new Config(secrets);

        // then
        assertThat(config.getString("application.linked"), equalTo("linked"));
    }

    @Test
    void testFileFallbackIsUsedWithoutFile() throws IOException {
        // given
        Path secrets = secrets(Map.of());

        // when
        createTempConfig(ImmutableMap.of("application.missing", "file{fallback}"));
        Config config = new Config(secrets);

        // then
        assertThat(config.getString("application.missing"), equalTo("fallback"));
        assertThat(config.getErrors(), empty());
    }

    @Test
    void testFileWithoutFileAndFallbackFailsValidation() throws IOException {
        // given
        Path secrets = secrets(Map.of());

        // when
        createTempConfig(ImmutableMap.of("application.missing", "file{}"));
        Config config = new Config(secrets);
        config.validate();

        // then
        assertThat(config.getErrors(), contains(startsWith("application.missing has no value")));
        assertThat(config.isValid(), equalTo(false));
    }

    @Test
    void testFileLargerThanTheLimitIsRejected() throws IOException {
        // given
        Path secrets = secrets(Map.of("application.large", "x".repeat(64 * 1024 + 1)));

        // when
        createTempConfig(ImmutableMap.of("application.large", "file{}"));
        Config config = new Config(secrets);

        // then
        assertThat(config.getString("application.large"), nullValue());
        assertThat(config.getErrors(), hasItem(containsString("larger than 64 KiB")));
    }

    @Test
    void testUnresolvedArgFailsValidation() {
        // given
        String key = "application.unresolved." + CommonUtils.randomString(32).replaceAll("[^A-Za-z0-9]", "");

        // when
        createTempConfig(ImmutableMap.of(key, "arg{}"));
        Config config = new Config();
        config.validate();

        // then
        assertThat(config.getString(key), nullValue());
        assertThat(config.getErrors(), contains(key + " has no value, arg{} is not set and has no fallback"));
        assertThat(config.isValid(), equalTo(false));
    }

    @Test
    void testUnresolvedEnvFailsValidation() {
        // given
        String key = "application.unresolved.env";

        // when
        createTempConfig(ImmutableMap.of(key, "env{}"));
        Config config = new Config();
        config.validate();

        // then
        assertThat(config.getErrors(), contains(key + " has no value, env{} is not set and has no fallback"));
        assertThat(config.isValid(), equalTo(false));
    }

    @Test
    void testVaultPlaceholderNamesTheRemoval() {
        // given
        createTempConfig(ImmutableMap.of("session.cookie.secret", "vault{}"));

        // when
        Config config = new Config();
        config.validate();

        // then
        assertThat(config.getErrors(), contains(containsString("vault{}, which was removed in 11.0.0")));
        assertThat(config.isValid(), equalTo(false));
    }

    @Test
    void testLiteralSecretsAreDetected() {
        // given
        String secret = "s".repeat(64);
        System.setProperty(Key.SMTP_PASSWORD, "from-arg");
        try {
            createTempConfig(ImmutableMap.of(
                    "application.secret", secret,
                    "session.cookie.key", "env{" + secret + "}",
                    "persistence.reporting.mongo.password", "literal",
                    "smtp.password", "arg{}",
                    "application.name", "literal"));

            // when
            Config config = new Config();

            // then a fallback in the braces counts as literal, as it is written into the config.yaml
            assertThat(config.getLiteralSecrets(), contains("application.secret", "persistence.reporting.mongo.password", "session.cookie.key"));
        } finally {
            System.clearProperty(Key.SMTP_PASSWORD);
        }
    }

    private static String unsetKey() {
        return "application.optional." + CommonUtils.uuidV6().replace("-", "");
    }

    @Test
    void testOptionalEnvWithoutVariableLeavesTheKeyUnset() {
        //given
        String key = unsetKey();

        //when
        createTempConfig(ImmutableMap.of(key, "env{?}"));
        Config config = new Config();
        config.validate();

        //then
        assertThat(config.getAllConfigurations().containsKey(key), equalTo(false));
        assertThat(config.getErrors(), empty());
    }

    @Test
    void testOptionalArgWithEmptyValueLeavesTheKeyUnset() {
        //given
        String key = unsetKey();
        System.setProperty(key, "");
        try {
            //when
            createTempConfig(ImmutableMap.of(key, "arg{?}"));
            Config config = new Config();

            //then
            assertThat(config.getAllConfigurations().containsKey(key), equalTo(false));
            assertThat(config.getErrors(), empty());
        } finally {
            System.clearProperty(key);
        }
    }

    @Test
    void testOptionalArgWithValueIsExternal() {
        //given
        System.setProperty(Key.SMTP_PASSWORD, "from-arg");
        try {
            //when
            createTempConfig(ImmutableMap.of("smtp.password", "arg{?}"));
            Config config = new Config();

            //then
            assertThat(config.getSmtpPassword(), equalTo("from-arg"));
            assertThat(config.getLiteralSecrets(), empty());
        } finally {
            System.clearProperty(Key.SMTP_PASSWORD);
        }
    }

    @Test
    void testOptionalFileWithoutFileLeavesTheKeyUnset() throws IOException {
        //given
        Path secrets = secrets(Map.of());

        //when
        createTempConfig(ImmutableMap.of("smtp.password", "file{?}"));
        Config config = new Config(secrets);

        //then
        assertThat(config.getSmtpPassword(), nullValue());
        assertThat(config.getErrors(), empty());
    }

    @Test
    void testOptionalPlaceholderKeepsTheDefault() {
        //when
        createTempConfig(ImmutableMap.of(
                "smtp.protocol", "arg{?}",
                "smtp.from", "arg{?}",
                "smtp.port", "arg{?}",
                "persistence.mongo.host", "arg{?}",
                "persistence.mongo.url", "arg{?}"));
        Config config = new Config();

        //then an unset optional value never replaces the default with an empty string
        assertThat(config.getSmtpProtocol(), equalTo(Default.SMTP_PROTOCOL));
        assertThat(config.getSmtpFrom(), equalTo(Default.SMTP_FROM));
        assertThat(config.getSmtpPort(), equalTo(Default.SMTP_PORT));
        assertThat(config.getMongoHost(Default.PERSISTENCE_PREFIX), equalTo(Default.PERSISTENCE_MONGO_HOST));
        assertThat(config.getMongoUrl(Default.PERSISTENCE_PREFIX), nullValue());
    }

    @Test
    void testEmptySecretIsNotLiteral() {
        //when
        createTempConfig(ImmutableMap.of("smtp.password", "", "persistence.mongo.password", "  "));
        Config config = new Config();

        //then
        assertThat(config.getLiteralSecrets(), empty());
    }

    @Test
    void testIsSecret() {
        // then
        assertThat(Config.isSecret("application.secret"), equalTo(true));
        assertThat(Config.isSecret("authentication.cookie.key"), equalTo(true));
        assertThat(Config.isSecret("connector.https.password"), equalTo(true));
        assertThat(Config.isSecret("persistence.mongo.password"), equalTo(true));
        assertThat(Config.isSecret("persistence.reporting.mongo.password"), equalTo(true));
        assertThat(Config.isSecret("application.admin.password"), equalTo(false));
        assertThat(Config.isSecret("application.name"), equalTo(false));
    }

    @Test
    void testStripLineBreak() {
        // then
        assertThat(Config.stripLineBreak("value\n"), equalTo("value"));
        assertThat(Config.stripLineBreak("value\r\n"), equalTo("value"));
        assertThat(Config.stripLineBreak("value"), equalTo("value"));
        assertThat(Config.stripLineBreak("value\n\n"), equalTo("value\n"));
    }

    @Test
    void testShutdownTimeoutAndProxy() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.shutdown.timeout", "5", "connector.proxy", "true");
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getApplicationShutdownTimeout(), equalTo(5L));
        assertThat(config.isConnectorProxy(), equalTo(true));
    }

    @Test
    void testNegativeShutdownTimeoutIsZero() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.shutdown.timeout", "-1");
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getApplicationShutdownTimeout(), equalTo(0L));
    }

    @Test
    void testGetApplicationAdminUsername() throws IOException {
        // given
        String username = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.admin.username", username);
        createTempConfig(configValues);
        Config config = new Config();

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
        Config config = new Config();

        // then
        assertThat(config.getApplicationAdminSecret(), equalTo(secret));
    }

    @Test
    void testGetApplicationAdminPassword() throws IOException {
        // given
        String password = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.admin.password", password);
        createTempConfig(configValues);
        Config config = new Config();

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
        Config config = new Config();

        // then
        assertThat(config.getAuthenticationCookieRememberExpires(), equalTo(Long.valueOf(expires)));
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
        Config config = new Config();

        // then
        assertThat(config.isApplicationAdminEnable(), equalTo(Boolean.valueOf(enable)));
    }

    @Test
    void testIsParameterStrict() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.parameter.strict", "true");
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isParameterStrict(), equalTo(true));
    }

    @Test
    void testGetSmptHost() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String host = "192.168.2.24";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.host", host);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSmtpHost(), equalTo(host));
    }

    @Test
    void testGetSmtpPort() throws IOException {
        // given
        String port = "555";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.port", port);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSmtpPort(), equalTo(Integer.valueOf(port)));
    }

    @Test
    void testIsAuthentication() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String ssl = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.authentication", ssl);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isSmtpAuthentication(), equalTo(Boolean.valueOf(ssl)));
    }

    @Test
    void testGetSmtpUsername() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String username = "smtpuser";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.username", username);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSmtpUsername(), equalTo(username));
    }

    @Test
    void testGetSmtpPassword() throws IOException {
        // given
        String password = "smtppass";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.password", password);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSmtpPassword(), equalTo(password));
    }

    @Test
    void testGetSmtpFrom() throws IOException {
        // given
        String from = "smtpform";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.from", from);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSmtpFrom(), equalTo(from));
    }

    @Test
    void testGetConnectorHttpsHost() throws IOException {
        // given
        String host = "192.168.3.24";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("connector.https.host", host);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getConnectorHttpsHost(), equalTo(host));
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
        Config config = new Config();

        // then
        assertThat(config.getConnectorHttpsPort(), equalTo(Integer.valueOf(port)));
    }

    @Test
    void testGetConnectorHttpHost() throws IOException {
        // given
        String host = "192.168.2.42";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("connector.http.host", host);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getConnectorHttpHost(), equalTo(host));
    }

    @Test
    void testGetConnectorHttpPort() throws IOException {
        // given
        String port = "2442";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("connector.http.port", port);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getConnectorHttpPort(), equalTo(Integer.valueOf(port)));
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
        Config config = new Config();

        // then
        assertThat(config.isMetricsEnable(), equalTo(Boolean.valueOf(enable)));
    }

    @Test
    void testPersistenceEnable() throws IOException {
        // given
        String enable = "false";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.enable", enable);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isPersistenceEnabled(), equalTo(Boolean.valueOf(enable)));
    }

    @Test
    void testAuthenticationLock() throws IOException {
        // given
        String lock = "24";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.lock", lock);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getAuthenticationLock(), equalTo(Integer.valueOf(lock)));
    }

    @Test
    void testAuthenticationLockDuration() throws IOException {
        // given
        String duration = "15";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.lock.duration", duration);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getAuthenticationLockDuration(), equalTo(Integer.valueOf(duration)));
    }

    @Test
    void testGetUndertowMaxEntitySize() throws IOException {
        // given
        String size = "4096";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("undertow.maxentitysize", size);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getUndertowMaxEntitySize(), equalTo(Long.valueOf(size)));
    }

    @Test
    void testGetFormMaxFileSize() throws IOException {
        // given
        String size = "67108864";
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("form.maxfilesize", size);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getFormMaxFileSize(), equalTo(Long.valueOf(size)));
    }

    @Test
    void testGetSessionCookieSecret() throws IOException {
        // given
        String key = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("session.cookie.secret", key);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSessionCookieSecret(), equalTo(key.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testGetSessionCookieSecretDefaultValue() throws IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSessionCookieSecret(), equalTo(secret.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testGetFlashCookieSecret() throws IOException {
        // given
        String key = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("flash.cookie.secret", key);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getFlashCookieSecret(), equalTo(key.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testGetFlashCookieEncryptionKeyDefaultValue() throws IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getFlashCookieSecret(), equalTo(secret.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testGetAuthenticationCookieSecret() throws IOException {
        // given
        String key = UUID.randomUUID().toString();

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.cookie.secret", key);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getAuthenticationCookieSecret(), equalTo(key.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testGetAuthenticationCookieSecretDefaultValue() throws IOException {
        // given
        String secret = UUID.randomUUID().toString();
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());

        // when
        Map<String, String> configValues = ImmutableMap.of("application.secret", secret);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getAuthenticationCookieSecret(), equalTo(secret.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void testGetString() {
        //given
        System.clearProperty(Key.APPLICATION_CONFIG);
        System.setProperty(Key.APPLICATION_MODE, Mode.PROD.toString());
        final Config config = Application.getInstance(Config.class);

        //then
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
    void testIsSmtpDebug() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String debug = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.debug", debug);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isSmtpDebug(), equalTo(Boolean.valueOf(debug)));
    }

    @Test
    void testGetSmtpProtocol() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String protocol = "smtptls";

        // when
        Map<String, String> configValues = ImmutableMap.of("smtp.protocol", protocol);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getSmtpProtocol(), equalTo(protocol));

    }

    @Test
    void testGetMongoAuthDB() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String authDb = "admin";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.authdb", authDb);
        createTempConfig(configValues);
        Config config = new Config();

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
        Config config = new Config();

        // then
        assertThat(config.getMongoDbName("persistence."), equalTo(mongodb));
    }

    @Test
    void testGetMongoHost() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String host = "127.0.0.5";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.host", host);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getMongoHost("persistence."), equalTo(host));
    }

    @Test
    void testGetMongoPort() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String port = "47235";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.port", port);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getMongoPort("persistence."), equalTo(Integer.parseInt(port)));
    }

    @Test
    void testGetMongoPassword() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String password = "thisismypassword";

        // when
        Map<String, String> configValues = ImmutableMap.of("persistence.mongo.password", password);
        createTempConfig(configValues);
        Config config = new Config();

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
        Config config = new Config();

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
        Config config = new Config();

        // then
        assertThat(config.isCorsEnable(), equalTo(Boolean.valueOf(cors)));
    }

    @Test
    void testGetCorsAllowOrigin() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String origin = "^http(s)?://(www.)?example.(com|org)$";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.alloworigin", origin);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCorsAllowOrigin().toString(), equalTo(Pattern.compile(origin).toString()));
    }

    @Test
    void testGetCorsHeadersAllowCredentials() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String credentials = "false";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.allowcredentials", credentials);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCorsHeadersAllowCredentials(), equalTo(credentials));
    }

    @Test
    void testGetCorsHeadersAllowHeaders() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String headers = "Authorization,Content-Type";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.allowheaders", headers);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCorsHeadersAllowHeaders(), equalTo(headers));
    }

    @Test
    void testIsAuthOrigin() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String authOrigin = "true";

        // when
        Map<String, String> configValues = ImmutableMap.of("authentication.origin", authOrigin);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.isAuthOrigin(), equalTo(Boolean.valueOf(authOrigin)));
    }

    @Test
    void testGetApplicationAdminLocale() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String adminLocale = "de_DE";

        // when
        Map<String, String> configValues = ImmutableMap.of("application.admin.locale", adminLocale);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getApplicationAdminLocale(), equalTo(adminLocale));
    }

    @Test
    void testGetCorsHeadersAllowMethods() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String headers = "GET,POST";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.allowmethods", headers);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCorsHeadersAllowMethods(), equalTo(headers));
    }

    @Test
    void testGetCorsHeadersExposeHeaders() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String headers = "Accept-Ranges,Content-Length";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.exposeheaders", headers);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCorsHeadersExposeHeaders(), equalTo(headers));
    }

    @Test
    void testGetCorsHeadersMaxAge() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String maxage = "86400";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.headers.maxage", maxage);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCorsHeadersMaxAge(), equalTo(maxage));
    }

    @Test
    void testGetCorsUrlPattern() throws IOException {
        // given
        System.setProperty(Key.APPLICATION_MODE, Mode.TEST.toString().toLowerCase());
        String pattern = "^https://example\\.com/rest(/.*)?$";

        // when
        Map<String, String> configValues = ImmutableMap.of("cors.urlpattern", pattern);
        createTempConfig(configValues);
        Config config = new Config();

        // then
        assertThat(config.getCorsUrlPattern().toString(), equalTo(Pattern.compile(pattern).toString()));
    }

    private void createTempConfig(Map<String, String> values) {
        Path configTempFile = tempDir.resolve(CommonUtils.uuidV6());

        try {
            Map<String, Object> config = new HashMap<>();

            Map<String, Object> defaultConfig = new HashMap<>();
            config.put("default", defaultConfig);

            values.forEach((key, value) -> {
                addDotSeparatedKey(defaultConfig, key, value);
            });

            Map<String, Object> environments = new HashMap<>();
            config.put("environments", environments);

            Map<String, Object> developmentConfig = new HashMap<>();
            environments.put("test", developmentConfig);
            addDotSeparatedKey(developmentConfig, "application.debug", "true");

            Map<String, Object> productionConfig = new HashMap<>();
            environments.put("prod", productionConfig);
            addDotSeparatedKey(productionConfig, "application.debug", "false");

            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            options.setPrettyFlow(true);

            Yaml yaml = new Yaml(options);

            try (Writer writer = new FileWriter(configTempFile.toAbsolutePath().toString())) {
                yaml.dump(config, writer);
            }

            System.setProperty(Key.APPLICATION_CONFIG, configTempFile.toAbsolutePath().toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}