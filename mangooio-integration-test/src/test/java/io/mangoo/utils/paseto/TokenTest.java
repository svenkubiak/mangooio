package io.mangoo.utils.paseto;

import io.mangoo.TestExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Map;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith({TestExtension.class})
public class TokenTest {
    @Test
    void testGetClaim() {
        //given
        String key = "foo";
        String value = UUID.randomUUID().toString();

        //when
        Token token = new Token();
        token.setClaims(Map.of(key, value));

        //then
        assertThat(token.getClaim("foo"), equalTo(value));
    }

    @Test
    void testContainsClaim() {
        //given
        String key = "foo";
        String value = UUID.randomUUID().toString();

        //when
        Token token = new Token();
        token.setClaims(Map.of(key, value));

        //then
        assertThat(token.containsClaim(key), equalTo(true));
        assertThat(token.containsClaim("bar"), equalTo(false));
    }

    @Test
    void testGetAsBoolean() {
        //given
        String key = "foo";
        String value = "true";

        //when
        Token token = new Token();
        token.setClaims(Map.of(key, value));

        //then
        assertThat(token.containsClaim(key), equalTo(true));
        assertThat(token.getClaimAsBoolean(key), equalTo(true));
    }
}
