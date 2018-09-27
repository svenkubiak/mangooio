package io.mangoo.routing.handlers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;

import com.google.common.base.Charsets;
import com.google.inject.Inject;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.ClaimKey;
import io.mangoo.enums.Required;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.utils.ByteUtils;
import io.mangoo.utils.CodecUtils;
import io.mangoo.utils.CryptoUtils;
import io.mangoo.utils.DateUtils;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;

/**
 *
 * @author svenkubiak
 *
 */
public class InboundCookiesHandler implements HttpHandler {
    private static final int STRING_LENGTH = 32;
    private static final Logger LOG = LogManager.getLogger(InboundCookiesHandler.class);
    private Config config;
    private Form form;

    @Inject
    public InboundCookiesHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Attachment attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        attachment.setSession(getSessionCookie(exchange));
        attachment.setAuthentication(getAuthenticationCookie(exchange));
        attachment.setFlash(getFlashCookie(exchange));
        attachment.setForm(this.form);

        exchange.putAttachment(RequestUtils.getAttachmentKey(), attachment);
        nextHandler(exchange);
    }

    /**
     * Retrieves the current session from the HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    @SuppressWarnings("unchecked")
    protected Session getSessionCookie(HttpServerExchange exchange) {
        Session session = Session.create()
            .withContent(new HashMap<>())
            .withAuthenticity(CryptoUtils.randomString(STRING_LENGTH));
    
        if (this.config.getSessionCookieExpires() > 0) {
            session.withExpires(LocalDateTime.now().plusSeconds(this.config.getSessionCookieExpires()));
        }
        
        String cookieValue = getCookieValue(exchange, this.config.getSessionCookieName());
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                String decryptedValue = Application.getInstance(Crypto.class).decrypt(cookieValue, this.config.getSessionCookieEncryptionKey());
                JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                        .setVerificationKey(new HmacKey(this.config.getSessionCookieSignKey().getBytes(Charsets.UTF_8)))
                        .setJwsAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.HMAC_SHA512))
                        .build();
                
                JwtClaims jwtClaims = jwtConsumer.processToClaims(decryptedValue);
                String expiresClaim = jwtClaims.getClaimValue(ClaimKey.EXPIRES.toString(), String.class);
                
                if (("-1").equals(expiresClaim)) {
                    session = Session.create()
                            .withContent(ByteUtils.copyMap(jwtClaims.getClaimValue(ClaimKey.DATA.toString(), Map.class)))
                            .withAuthenticity(jwtClaims.getClaimValue(ClaimKey.AUTHENTICITY.toString(), String.class));
                } else if (LocalDateTime.parse(jwtClaims.getClaimValue(ClaimKey.EXPIRES.toString(), String.class), DateUtils.formatter).isAfter(LocalDateTime.now())) {
                    session = Session.create()
                            .withContent(ByteUtils.copyMap(jwtClaims.getClaimValue(ClaimKey.DATA.toString(), Map.class)))
                            .withAuthenticity(jwtClaims.getClaimValue(ClaimKey.AUTHENTICITY.toString(), String.class))
                            .withExpires(LocalDateTime.parse(jwtClaims.getClaimValue(ClaimKey.EXPIRES.toString(), String.class), DateUtils.formatter));
                } else {
                    //Ignore this and use default session
                }
            } catch (Exception e) { //NOSONAR
                LOG.error("Failed to parse session cookie", e);
                session.invalidate();
            }
        }

        return session;
    }

    /**
     * Retrieves the current authentication from the HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected Authentication getAuthenticationCookie(HttpServerExchange exchange) {
        Authentication authentication = Authentication.create().withSubject(null);
        
        String cookieValue = getCookieValue(exchange, this.config.getAuthenticationCookieName());
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                String decryptedValue = Application.getInstance(Crypto.class).decrypt(cookieValue, this.config.getAuthenticationCookieEncryptionKey());
                
                JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                        .setRequireSubject()
                        .setVerificationKey(new HmacKey(this.config.getAuthenticationCookieSignKey().getBytes(Charsets.UTF_8)))
                        .setJwsAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.HMAC_SHA512))
                        .build();
                
                JwtClaims jwtClaims = jwtConsumer.processToClaims(decryptedValue);
                String expiresClaim = jwtClaims.getClaimValue(ClaimKey.EXPIRES.toString(), String.class);
                
                if (("-1").equals(expiresClaim)) {
                    authentication = Authentication.create()
                            .withSubject(jwtClaims.getSubject())
                            .twoFactorAuthentication(jwtClaims.getClaimValue(ClaimKey.TWO_FACTOR.toString(), Boolean.class));
                } else if (LocalDateTime.parse(jwtClaims.getClaimValue(ClaimKey.EXPIRES.toString(), String.class), DateUtils.formatter).isAfter(LocalDateTime.now())) {
                    authentication = Authentication.create()
                            .withExpires(LocalDateTime.parse(jwtClaims.getClaimValue(ClaimKey.EXPIRES.toString(), String.class), DateUtils.formatter))
                            .withSubject(jwtClaims.getSubject())
                            .twoFactorAuthentication(jwtClaims.getClaimValue(ClaimKey.TWO_FACTOR.toString(), Boolean.class));
                } else {
                    //Ignore this and use default authentication
                }
            } catch (Exception e) { //NOSONAR
                LOG.error("Failed to parse authentication cookie", e);
                authentication.invalidate();
            }
        }

        return authentication;
    }

    /**
     * Retrieves the flash cookie from the current
     *
     * @param exchange The Undertow HttpServerExchange
     */
    @SuppressWarnings("unchecked")
    protected Flash getFlashCookie(HttpServerExchange exchange) {
        Flash flash = Flash.create();
        
        final String cookieValue = getCookieValue(exchange, this.config.getFlashCookieName());
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                String decryptedValue = Application.getInstance(Crypto.class).decrypt(cookieValue, this.config.getFlashCookieEncryptionKey());
                JwtConsumer jwtConsumer = new JwtConsumerBuilder()
                    .setVerificationKey(new HmacKey(this.config.getFlashCookieSignKey().getBytes(Charsets.UTF_8)))
                    .setJwsAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.HMAC_SHA512))
                    .build();
                
                JwtClaims jwtClaims = jwtConsumer.processToClaims(decryptedValue);
                LocalDateTime expires = LocalDateTime.parse(jwtClaims.getClaimValue(ClaimKey.EXPIRES.toString(), String.class), DateUtils.formatter);
                
                if (expires.isAfter(LocalDateTime.now())) {
                    if (jwtClaims.hasClaim(ClaimKey.FORM.toString())) {
                        this.form = CodecUtils.deserializeFromBase64(jwtClaims.getClaimValue(ClaimKey.FORM.toString(), String.class));
                    } 
                    
                    flash = Flash.create()
                            .withContent(ByteUtils.copyMap(jwtClaims.getClaimValue(ClaimKey.DATA.toString(), Map.class))).setDiscard(true);
                }
            } catch (Exception e) { //NOSONAR
                LOG.error("Failed to parse flash cookie", e);
                flash.invalidate();
            } 
        }
        
        return flash;
    }
    
    /**
     * Retrieves the value of a cookie with a given name from a HttpServerExchange
     * 
     * @param exchange The exchange containing the cookie
     * @param cookieName The name of the cookie
     * 
     * @return The value of the cookie or null if none found
     */
    private String getCookieValue(HttpServerExchange exchange, String cookieName) {
        String value = null;
        Map<String, Cookie> requestCookies = exchange.getRequestCookies();
        if (requestCookies != null) {
            Cookie cookie = exchange.getRequestCookies().get(cookieName);
            if (cookie != null) {
                value = cookie.getValue();
            }  
        }

        return value;
    }

    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    @SuppressWarnings("all")
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(AuthenticationHandler.class).handleRequest(exchange);
    }
}