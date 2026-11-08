package io.mangoo.maven;

import io.mangoo.crypto.Argon2Settings;
import io.mangoo.utils.CommonUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertThrows;

class HashingSettingsTest {
    @Test
    void testDevUsesTheDefaultWhenTheEnvironmentDoesNotOverrideIt() {
        //given
        String yaml = """
                default:
                  authentication:
                    hashing:
                      memory: 16384
                      iterations: 4
                      parallelism: 1
                environments:
                  dev: {}
                """;

        //when
        Argon2Settings settings = HashingSettings.read(stream(yaml), "dev");

        //then
        assertThat(settings, equalTo(new Argon2Settings(16384, 4, 1)));
    }

    @Test
    void testProdOverridesTheMemoryCost() {
        //given
        String yaml = """
                default:
                  authentication:
                    hashing:
                      memory: 16384
                      iterations: 4
                      parallelism: 1
                environments:
                  prod:
                    authentication:
                      hashing:
                        memory: 65536
                """;

        //when
        Argon2Settings settings = HashingSettings.read(stream(yaml), "prod");

        //then
        assertThat(settings, equalTo(new Argon2Settings(65536, 4, 1)));
    }

    @Test
    void testMissingHashingKeysUseTheFrameworkDefaults() {
        //given
        String yaml = """
                default: {}
                environments:
                  dev: {}
                """;

        //when
        Argon2Settings settings = HashingSettings.read(stream(yaml), "dev");

        //then
        assertThat(settings, equalTo(new Argon2Settings(32768, 3, 1)));
    }

    @Test
    void testMissingEnvironmentIsRejected() {
        //given
        String yaml = """
                default: {}
                environments:
                  dev: {}
                """;

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> HashingSettings.read(stream(yaml), "prod"));

        //then
        assertThat(exception.getMessage(), equalTo("Active environment 'prod' not found in config.yaml"));
    }

    @Test
    void testSettingsBelowTheMinimumAreRejected() {
        //given
        String yaml = """
                default:
                  authentication:
                    hashing:
                      memory: 1024
                environments:
                  dev: {}
                """;

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> HashingSettings.read(stream(yaml), "dev"));

        //then
        assertThat(exception.getMessage(), equalTo("authentication.hashing.memory must be at least 8192 KB, but is 1024"));
    }

    @Test
    void testANonNumericValueIsRejected() {
        //given
        String yaml = """
                default:
                  authentication:
                    hashing:
                      memory: abc
                environments:
                  dev: {}
                """;

        //when
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> HashingSettings.read(stream(yaml), "dev"));

        //then
        assertThat(exception.getMessage(), equalTo("authentication.hashing.memory must be a number, but is abc"));
    }

    @Test
    void testHashArgon2WithExplicitSettingsDoesNotNeedTheApplication() {
        //when
        String hash = CommonUtils.hashArgon2("secret", "salt", new Argon2Settings(8192, 2, 1));

        //then
        assertThat(hash, startsWith("$argon2id$v=19$m=8192,t=2,p=1$"));
    }

    @Test
    void testMatchingPasswordsAreAccepted() {
        assertThat(HashingSettings.cleartext("secret".toCharArray(), "secret".toCharArray()), equalTo("secret"));
    }

    @Test
    void testAnEmptyPasswordIsRejected() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> HashingSettings.cleartext(new char[0], new char[0]));

        assertThat(exception.getMessage(), equalTo("Password can not be empty"));
    }

    @Test
    void testDifferentPasswordsAreRejected() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> HashingSettings.cleartext("one".toCharArray(), "two".toCharArray()));

        assertThat(exception.getMessage(), equalTo("Passwords do not match"));
    }

    private static ByteArrayInputStream stream(String yaml) {
        return new ByteArrayInputStream(yaml.getBytes(StandardCharsets.UTF_8));
    }
}
