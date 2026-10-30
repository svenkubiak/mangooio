package io.mangoo.core;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class SecretValidationTest {

    @Test
    void testSecretWithExactly64Bytes() {
        assertThat(Application.isValidSecret(bytes("a".repeat(64))), equalTo(true));
    }

    @Test
    void testSecretTooShort() {
        assertThat(Application.isValidSecret(bytes("a".repeat(63))), equalTo(false));
    }

    @Test
    void testSecretTooLong() {
        assertThat(Application.isValidSecret(bytes("a".repeat(80))), equalTo(false));
    }

    @Test
    void testKeyLength() {
        assertThat(Application.isValidKey(bytes("a".repeat(63))), equalTo(false));
        assertThat(Application.isValidKey(bytes("a".repeat(64))), equalTo(true));
        assertThat(Application.isValidKey(bytes("a".repeat(80))), equalTo(true));
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
