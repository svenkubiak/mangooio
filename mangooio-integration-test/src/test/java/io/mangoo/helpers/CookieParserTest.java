package io.mangoo.helpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.cactoos.matchers.RunsInThreads;
import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.ClaimKey;
import io.mangoo.utils.CryptoUtils;
import io.mangoo.utils.DateUtils;

/**
 *
 * @author svenkubiak
 *
 */
public class CookieParserTest {
    private static int THREADS = 100;
    private static String sessionCookie = "";
    private static String authenticationCookie = "";
    private static String sessionCookieEncrypted = "";
    private static String authenticationCookieEncrypted = "";

    @Before
    public void init() {
        Config config = Application.getInstance(Config.class);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put(ClaimKey.VERSION.toString(), config.getAuthenticationCookieVersion());
        claims.put(ClaimKey.TWO_FACTOR.toString(), false);
        
        authenticationCookie = Jwts.builder()
                .setClaims(claims)
                .setSubject("sven")
                .setExpiration(DateUtils.localDateTimeToDate(LocalDateTime.now().plusYears(240)))
                .signWith(SignatureAlgorithm.HS512, config.getAuthenticationCookieSignKey().getBytes())
                .compact();

        authenticationCookieEncrypted = Application.getInstance(Crypto.class).encrypt(authenticationCookie);
        
        Map<String, String> values = new HashMap<>();
        
        claims = new HashMap<>();
        claims.put(ClaimKey.AUTHENTICITY.toString(), "foobar");
        claims.put(ClaimKey.VERSION.toString(), config.getAuthenticationCookieVersion());
        claims.put(ClaimKey.DATA.toString(), values);
        
        sessionCookie = Jwts.builder()
                .setClaims(claims)
                .setExpiration(DateUtils.localDateTimeToDate(LocalDateTime.now().plusYears(240)))
                .signWith(SignatureAlgorithm.HS512, config.getSessionCookieSignKey().getBytes())
                .compact();
        
        sessionCookieEncrypted = Application.getInstance(Crypto.class).encrypt(sessionCookie);
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
        MatcherAssert.assertThat(t -> {
            //given
            final CookieParser cookieParser = CookieParser.build()
                    .withContent(sessionCookie)
                    .isEncrypted(false);
            
            // then
            return cookieParser.hasValidSessionCookie();
        }, new RunsInThreads<>(new AtomicInteger(), THREADS));
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
        MatcherAssert.assertThat(t -> {
            //given
            final CookieParser cookieParser = CookieParser.build()
                    .withContent(sessionCookieEncrypted)
                    .isEncrypted(true);
            
            // then
            return cookieParser.hasValidSessionCookie();
        }, new RunsInThreads<>(new AtomicInteger(), THREADS));
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
    
    @Test
    public void testJwtSigning() {
        //given
        Config config = Application.getInstance(Config.class);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put(ClaimKey.VERSION.toString(), config.getAuthenticationCookieVersion());
        claims.put(ClaimKey.TWO_FACTOR.toString(), false);
        
        String key24 = CryptoUtils.randomString(24);
        String key48 = CryptoUtils.randomString(48);
        String key96 = CryptoUtils.randomString(96);
        String key128 = CryptoUtils.randomString(128);
        String key256 = CryptoUtils.randomString(256);
        
        Jwts.builder()
                .setClaims(claims)
                .setSubject("sven")
                .setExpiration(DateUtils.localDateTimeToDate(LocalDateTime.now().plusYears(240)))
                .signWith(SignatureAlgorithm.HS512, key24.getBytes())
                .compact();
        
        Jwts.builder()
                .setClaims(claims)
                .setSubject("sven")
                .setExpiration(DateUtils.localDateTimeToDate(LocalDateTime.now().plusYears(240)))
                .signWith(SignatureAlgorithm.HS512, key48.getBytes())
                .compact();

        Jwts.builder()
                .setClaims(claims)
                .setSubject("sven")
                .setExpiration(DateUtils.localDateTimeToDate(LocalDateTime.now().plusYears(240)))
                .signWith(SignatureAlgorithm.HS512, key96.getBytes())
                .compact();

        Jwts.builder()
                .setClaims(claims)
                .setSubject("sven")
                .setExpiration(DateUtils.localDateTimeToDate(LocalDateTime.now().plusYears(240)))
                .signWith(SignatureAlgorithm.HS512, key128.getBytes())
                .compact();
        
        Jwts.builder()
                .setClaims(claims)
                .setSubject("sven")
                .setExpiration(DateUtils.localDateTimeToDate(LocalDateTime.now().plusYears(240)))
                .signWith(SignatureAlgorithm.HS512, key256.getBytes())
                .compact();
    }
}