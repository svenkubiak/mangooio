package io.mangoo.utils.cookie;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.utils.cookie.CookieParser;

/**
 *
 * @author svenkubiak
 *
 */
public class CookieParserTest {
    private final static String sessionCookie = "eyJhbGciOiJIUzUxMiJ9.eyJkYXRhIjp7ImZvbyI6InRoaXMgaXMgYSBzZXNzaW9uIHZhbHVlIn0sImV4cCI6MzI0NzIyMjU3MjAsInZlcnNpb24iOiIwIiwiYXV0aGVudGljaXR5VG9rZW4iOiJJckpDc1FYNmRBTFBkUlVkIn0.H829037BVU-NZdT_vVVEUjhX4QCmxkpGJEMLr1-qT_shd-saaaHbDIMSd3ulU1v2ceTqhJqoyNIAP748KC1f9g";
    private final static String authenticationCookie = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJmb29iYXIiLCJleHAiOjMyNDcyMjI1NzIwLCJ2ZXJzaW9uIjoiMCJ9.EeI2mIdqR1vGKSGJhnOqv7OgJSzU77cY0A43UCplkKg4jV5QLlvl9yXZoYvKvHWDuY_zQgjmm3OnwDAFHHT2sQ";
    private String secret;
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
        final CookieParser cookieParser = CookieParser.build()
                .withContent(sessionCookie)
                .withSecret(this.secret)
                .isEncrypted(false);

        //then
        assertThat(cookieParser.hasValidSessionCookie(), equalTo(true));
    }

    @Test
    public void testValidSessionWithEncryption() {
        //given
        final CookieParser cookieParser = CookieParser.build()
                .withContent(sessionCookieEncrypted)
                .withSecret(this.secret)
                .isEncrypted(true);

        //then
        assertThat(cookieParser.hasValidSessionCookie(), equalTo(true));
    }
    
    @Test
    public void testValidAuthentication() {
        //given
        final CookieParser cookieParser = CookieParser.build()
                .withContent(authenticationCookie)
                .withSecret(this.secret)
                .isEncrypted(false);

        //then
        assertThat(cookieParser.hasValidAuthenticationCookie(), equalTo(true));
    }   

    @Test
    public void testValidAuthenticationWithEncryption() {
        //given
        final CookieParser cookieParser = CookieParser.build()
                .withContent(authenticationCookieEncrypted)
                .withSecret(this.secret)
                .isEncrypted(true);
        
        //then
        assertThat(cookieParser.hasValidAuthenticationCookie(), equalTo(true));
    }
}