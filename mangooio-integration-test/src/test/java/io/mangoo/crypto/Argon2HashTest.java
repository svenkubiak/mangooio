package io.mangoo.crypto;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Argon2HashTest {
    private static final Argon2Settings SETTINGS = new Argon2Settings(1024, 1, 1);
    private static final byte[] SALT = "a salt that is long enough".getBytes(StandardCharsets.UTF_8);
    private static final byte[] HASH = "a hash of at least sixteen bytes".getBytes(StandardCharsets.UTF_8);

    @Test
    void testEqualContentIsEqual() {
        //given two instances that do not share their arrays
        var first = new Argon2Hash(SETTINGS, SALT.clone(), HASH.clone());
        var second = new Argon2Hash(SETTINGS, SALT.clone(), HASH.clone());

        //then
        assertThat(first, equalTo(second));
        assertThat(first.hashCode(), equalTo(second.hashCode()));
    }

    @Test
    void testDifferentHashIsNotEqual() {
        //given
        var first = new Argon2Hash(SETTINGS, SALT.clone(), HASH.clone());
        var second = new Argon2Hash(SETTINGS, SALT.clone(), "a hash of at least sixteen byteS".getBytes(StandardCharsets.UTF_8));

        //then
        assertThat(first, not(equalTo(second)));
    }

    @Test
    void testDifferentSaltIsNotEqual() {
        //given
        var first = new Argon2Hash(SETTINGS, SALT.clone(), HASH.clone());
        var second = new Argon2Hash(SETTINGS, "another salt that is long".getBytes(StandardCharsets.UTF_8), HASH.clone());

        //then
        assertThat(first, not(equalTo(second)));
    }

    @Test
    void testDifferentSettingsAreNotEqual() {
        //given
        var first = new Argon2Hash(SETTINGS, SALT.clone(), HASH.clone());
        var second = new Argon2Hash(new Argon2Settings(2048, 1, 1), SALT.clone(), HASH.clone());

        //then
        assertThat(first, not(equalTo(second)));
    }

    @Test
    void testRoundTripThroughPhcFormatIsEqual() {
        //given
        var hash = new Argon2Hash(SETTINGS, SALT.clone(), HASH.clone());

        //when
        var parsed = Argon2Hash.parse(hash.encode());

        //then
        assertThat(parsed, equalTo(hash));
        assertThat(parsed.hashCode(), equalTo(hash.hashCode()));
    }

    @Test
    void testToStringDoesNotLeakCredentialMaterial() {
        //given
        var hash = new Argon2Hash(SETTINGS, SALT.clone(), HASH.clone());

        //when
        String string = hash.toString();

        //then
        assertThat(string, not(containsString(new String(SALT, StandardCharsets.UTF_8))));
        assertThat(string, not(containsString(new String(HASH, StandardCharsets.UTF_8))));
        assertThat(string, containsString(SALT.length + " bytes"));
        assertThat(string, containsString(HASH.length + " bytes"));
    }

    @Test
    void testConstructorCopiesItsArguments() {
        //given
        byte[] salt = SALT.clone();
        byte[] hash = HASH.clone();
        var argon2Hash = new Argon2Hash(SETTINGS, salt, hash);

        //when the caller keeps writing to the arrays it handed over
        salt[0] = 0;
        hash[0] = 0;

        //then
        assertThat(argon2Hash.salt(), equalTo(SALT));
        assertThat(argon2Hash.hash(), equalTo(HASH));
    }

    @Test
    void testAccessorsReturnCopies() {
        //given
        var argon2Hash = new Argon2Hash(SETTINGS, SALT.clone(), HASH.clone());

        //when the caller writes to what it read back
        argon2Hash.salt()[0] = 0;
        argon2Hash.hash()[0] = 0;

        //then
        assertThat(argon2Hash.salt(), equalTo(SALT));
        assertThat(argon2Hash.hash(), equalTo(HASH));
    }

    @Test
    void testParseRejectsNull() {
        //then
        assertThrows(IllegalArgumentException.class, () -> Argon2Hash.parse(null));
    }

    @Test
    void testParseRejectsBlank() {
        //then
        assertThrows(IllegalArgumentException.class, () -> Argon2Hash.parse("   "));
    }
}
