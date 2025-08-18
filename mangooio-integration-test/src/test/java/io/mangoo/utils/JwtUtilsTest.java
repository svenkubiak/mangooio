package io.mangoo.utils;

import com.nimbusds.jwt.JWTClaimsSet;
import io.mangoo.TestExtension;
import io.mangoo.exceptions.MangooJwtExeption;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.text.ParseException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
public class JwtUtilsTest {
    @Test
    void testCreate() throws MangooJwtExeption {
        //given
        String secret = MangooUtils.randomString(64);
        String key = MangooUtils.randomString(64);
        var jwtData = JwtUtils.jwtData()
                .withSecret(secret)
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

    @Test
    void testValidate() throws MangooJwtExeption, ParseException {
        //given
        String secret = MangooUtils.randomString(64);
        String key = MangooUtils.randomString(64);
        var jwtData = JwtUtils.jwtData()
                .withSecret(secret)
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
