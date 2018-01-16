package io.mangoo.utils.cookie;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Before;
import org.junit.Test;

import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.helpers.cookie.CookieParser;
import io.mangoo.test.utils.ConcurrentTester;

/**
 *
 * @author svenkubiak
 *
 */
public class CookieParserTest {
    private final static String sessionCookie = "eyJhbGciOiJIUzUxMiJ9.eyJkYXRhIjp7ImZvbyI6InRoaXMgaXMgYSBzZXNzaW9uIHZhbHVlIn0sImV4cCI6MzI0NzIyMjU3MjAsInZlcnNpb24iOiIwIiwiYXV0aGVudGljaXR5VG9rZW4iOiJJckpDc1FYNmRBTFBkUlVkIn0.H829037BVU-NZdT_vVVEUjhX4QCmxkpGJEMLr1-qT_shd-saaaHbDIMSd3ulU1v2ceTqhJqoyNIAP748KC1f9g";
    private final static String authenticationCookie = "eyJhbGciOiJIUzUxMiJ9.eyJ0d29GYWN0b3IiOmZhbHNlLCJzdWIiOiJmb28iLCJleHAiOjQ2NDUzNjgyMjMsInZlcnNpb24iOiIxIn0.bCVh6yEWwtptlBbQZK_zfcqXKX0wXfO3tlkYGfIZXsy2IwCHnDaIT3bE5w4iA3xaDw5iuTdFqCndvvHdKBdwsQ";
    private static String sessionCookieEncrypted = "";
    private static String authenticationCookieEncrypted = "";

    @Before
    public void init() {
        sessionCookieEncrypted = Application.getInstance(Crypto.class).encrypt(sessionCookie);
        authenticationCookieEncrypted = Application.getInstance(Crypto.class).encrypt(authenticationCookie);
    }

    @Test
    public void testValidSession() {
        //given
        final CookieParser cookieParser = CookieParser.build()
                .withContent(sessionCookie)
                .isEncrypted(false);

        //then
        assertThat(cookieParser.hasValidSessionCookie(), equalTo(true));
    }
    
    @Test
    public void testValidSessionConcurrent() throws InterruptedException {
        Runnable runnable = () -> {
            //given
            final CookieParser cookieParser = CookieParser.build()
                    .withContent(sessionCookie)
                    .isEncrypted(false);

            //then
            assertThat(cookieParser.hasValidSessionCookie(), equalTo(true));
        };
        
        ConcurrentTester.create()
        .withRunnable(runnable)
        .withThreads(50)
        .run();
    }

    @Test
    public void testValidSessionWithEncryption() {
        //given
        final CookieParser cookieParser = CookieParser.build()
                .withContent(sessionCookieEncrypted)
                .isEncrypted(true);

        //then
        assertThat(cookieParser.hasValidSessionCookie(), equalTo(true));
    }
    
    @Test
    public void testValidSessionWithEncryptionConcurrent() throws InterruptedException {
        Runnable runnable = () -> {
            //given
            final CookieParser cookieParser = CookieParser.build()
                    .withContent(sessionCookieEncrypted)
                    .isEncrypted(true);

            //then
            assertThat(cookieParser.hasValidSessionCookie(), equalTo(true));
        };
        
        ConcurrentTester.create()
        .withRunnable(runnable)
        .withThreads(50)
        .run();
    }
    
    @Test
    public void testValidAuthentication() {
        //given
        final CookieParser cookieParser = CookieParser.build()
                .withContent(authenticationCookie)
                .isEncrypted(false);

        //then
        assertThat(cookieParser.hasValidAuthenticationCookie(), equalTo(true));
    }   

    @Test
    public void testValidAuthenticationWithEncryption() {
        //given
        final CookieParser cookieParser = CookieParser.build()
                .withContent(authenticationCookieEncrypted)
                .isEncrypted(true);
        
        //then
        assertThat(cookieParser.hasValidAuthenticationCookie(), equalTo(true));
    }
}