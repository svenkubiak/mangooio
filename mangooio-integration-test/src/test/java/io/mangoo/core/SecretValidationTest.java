package io.mangoo.core;

import io.mangoo.exceptions.MangooJwtException;
import io.mangoo.utils.JwtUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void testSecretWithNonAsciiCharacter() {
        //given
        String secret = "ä" + "a".repeat(63);

        //then
        assertThat(secret.length(), equalTo(64));
        assertThat(Application.isValidSecret(bytes(secret)), equalTo(false));
    }

    @Test
    void testKeyLength() {
        assertThat(Application.isValidKey(bytes("a".repeat(63))), equalTo(false));
        assertThat(Application.isValidKey(bytes("a".repeat(64))), equalTo(true));
        assertThat(Application.isValidKey(bytes("a".repeat(80))), equalTo(true));
    }

    @Test
    void testValidSecretCanBeUsedForEncryption() throws MangooJwtException {
        //given
        byte[] secret = bytes("a".repeat(64));

        //when
        String jwt = JwtUtils.createJwt(jwtData(secret));

        //then
        assertThat(Application.isValidSecret(secret), equalTo(true));
        assertThat(jwt, not(emptyOrNullString()));
    }

    @Test
    void testInvalidSecretCanNotBeUsedForEncryption() {
        //given
        byte[] secret = bytes("a".repeat(80));

        //then
        assertThat(Application.isValidSecret(secret), equalTo(false));
        assertThrows(MangooJwtException.class, () -> JwtUtils.createJwt(jwtData(secret)));
    }

    private static JwtUtils.JwtData jwtData(byte[] secret) {
        return JwtUtils.JwtData.create()
                .withSecret(secret)
                .withKey(bytes("b".repeat(64)))
                .withIssuer("issuer")
                .withAudience("audience")
                .withSubject("subject")
                .withTtlSeconds(60);
    }

    private static byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }
}
