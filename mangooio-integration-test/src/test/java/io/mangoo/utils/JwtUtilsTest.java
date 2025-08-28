package io.mangoo.utils;

import com.nimbusds.jwt.JWTClaimsSet;
import io.mangoo.TestExtension;
import io.mangoo.exceptions.MangooJwtException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith({TestExtension.class})
public class JwtUtilsTest {

    @Test
    void testCreate() throws MangooJwtException {
        //given
        String secret = MangooUtils.randomString(64);
        String key = MangooUtils.randomString(64);
        var jwtData = JwtUtils.jwtData()
                .withSecret(secret.getBytes(StandardCharsets.UTF_8))
                .withKey(key)
                .withIssuer("foo")
                .withAudience("bar")
                .withSubject(CodecUtils.uuidV6())
                .withTtlSeconds(120)
                .withClaims(Map.of("foo", "bar"));

        //when
        String jwt = JwtUtils.createJwt(jwtData);

        //then
        assertThat(jwt, not(nullValue()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"iss", "aud", "sub", "iat", "nbf", "exp", "jti"})
    void testReservedException(String reserved) throws MangooJwtException {
        //given
        String secret = MangooUtils.randomString(64);
        String key = MangooUtils.randomString(64);
        var jwtData = JwtUtils.jwtData()
                .withSecret(secret.getBytes(StandardCharsets.UTF_8))
                .withKey(key)
                .withIssuer("foo")
                .withAudience("bar")
                .withSubject(CodecUtils.uuidV6())
                .withTtlSeconds(120)
                .withClaims(Map.of(reserved, CodecUtils.uuidV6()));

        //when
        Exception exception = assertThrows(MangooJwtException.class, () -> {
            JwtUtils.createJwt(jwtData);
        });

        //then
        assertThat(exception.getMessage(), equalTo("Extra claim '" + reserved + "' conflicts with a reserved claim"));
    }

    @Test
    void testCreateReserved() {
        //given
        String secret = MangooUtils.randomString(64);
        String key = MangooUtils.randomString(64);
        List<String> reserved = List.of("iss", "aud", "sub", "iat", "nbf", "exp", "jti");

        //then
        for (String value : reserved) {
            var jwtData = JwtUtils.jwtData()
                    .withSecret(secret.getBytes(StandardCharsets.UTF_8))
                    .withKey(key)
                    .withIssuer("foo")
                    .withAudience("bar")
                    .withSubject(CodecUtils.uuidV6())
                    .withTtlSeconds(120)
                    .withClaims(Map.of(value, CodecUtils.uuidV6()));

            assertThrows(MangooJwtException.class, () -> JwtUtils.createJwt(jwtData));
        }
    }

    @Test
    void testValidate() throws MangooJwtException, ParseException {
        //given
        String secret = MangooUtils.randomString(64);
        String key = MangooUtils.randomString(64);
        var jwtData = JwtUtils.jwtData()
                .withSecret(secret.getBytes(StandardCharsets.UTF_8))
                .withKey(key)
                .withIssuer("foo")
                .withAudience("bar")
                .withSubject(CodecUtils.uuidV6())
                .withTtlSeconds(120)
                .withClaims(Map.of("foo", "bar"));

        //when
        String jwt = JwtUtils.createJwt(jwtData);

        //then
        assertThat(jwt, not(nullValue()));

        //then
        JWTClaimsSet jwtClaimsSet = JwtUtils.parseJwt(jwt, jwtData);

        assertThat(jwtClaimsSet, not(nullValue()));
        assertThat(jwtClaimsSet.getClaimAsString("foo"), equalTo("bar"));
    }
}
