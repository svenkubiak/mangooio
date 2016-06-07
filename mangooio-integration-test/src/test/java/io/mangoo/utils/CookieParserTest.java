package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;

/**
 *
 * @author svenkubiak
 *
 */
public class CookieParserTest {
    private String secret;
    private final static String sessionCookie = "04d62cafb17b81dc563037d1c23cba9cee83f52f2eeac0ef5e303572ba720977c29fae25b54ec3f480c9f84178fdac96b1c202259abd2252ed053b8541301f65|IrJCsQX6dALPdRUd|2999-01-01T23:42:00.00|0#foo:this is a session value";
    private final static String authenticationCookie = "f4dddaf1e3f806ec48090404c5d92be55f29b12dde4e9e86c3029745d881313bdae6d629adc424bbfc31ddc0b83b3532f9f09affa79ff717446c9d213701e43d|2999-01-01T23:42:00.00|0#foobar";
    private static String sessionCookieEncrypted = "";
    private static String authenticationCookieEncrypted = "";

    @Before
    public void init() {
        this.secret = Application.getInstance(Config.class).getApplicationSecret();
        sessionCookieEncrypted = Application.getInstance(Crypto.class).encrypt(sessionCookie);
        authenticationCookieEncrypted = Application.getInstance(Crypto.class).encrypt(authenticationCookie);
    }

    @Test
    public void testValidSession() {
        //given
        final CookieParser cookieParser = new CookieParser(sessionCookie, this.secret, false);

        //then
        assertThat(cookieParser.hasValidSessionCookie(), equalTo(true));
    }

    @Test
    public void testValidSessionWithEncryption() {
        //given
        final CookieParser cookieParser = new CookieParser(sessionCookieEncrypted, this.secret, true);

        //then
        assertThat(cookieParser.hasValidSessionCookie(), equalTo(true));
    }
    
    @Test
    public void testValidAuthentication() {
        //given
        final CookieParser cookieParser = new CookieParser(authenticationCookie, this.secret, false);

        //then
        assertThat(cookieParser.hasValidAuthenticationCookie(), equalTo(true));
    }   

    @Test
    public void testValidAuthenticationWithEncryption() {
        //given
        final CookieParser cookieParser = new CookieParser(authenticationCookieEncrypted, this.secret, true);

        //then
        assertThat(cookieParser.hasValidAuthenticationCookie(), equalTo(true));
    }
}