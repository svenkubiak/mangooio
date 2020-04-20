package io.mangoo.routing.handlers;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import dev.paseto.jpaseto.Paseto;
import dev.paseto.jpaseto.PasetoException;
import dev.paseto.jpaseto.Pasetos;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.enums.ClaimKey;
import io.mangoo.enums.Required;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.utils.CodecUtils;
import io.mangoo.utils.MangooUtils;
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
            .withAuthenticity(MangooUtils.randomString(STRING_LENGTH));
    
        if (this.config.getSessionCookieExpires() > 0) {
            session.withExpires(LocalDateTime.now().plusSeconds(this.config.getSessionCookieExpires()));
        }
        
        String cookieValue = getCookieValue(exchange, this.config.getSessionCookieName());
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                Paseto paseto = Pasetos.parserBuilder()
                        .setSharedSecret(this.config.getSessionCookieSignKey().getBytes(StandardCharsets.UTF_8))
                        .build()
                        .parse(cookieValue);
                
//                String decryptedValue = Application.getInstance(Crypto.class).decrypt(cookieValue, this.config.getSessionCookieEncryptionKey());
//                JwtConsumer jwtConsumer = new JwtConsumerBuilder()
//                        .setVerificationKey(new HmacKey(this.config.getSessionCookieSignKey().getBytes(StandardCharsets.UTF_8)))
//                        .setJwsAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.HMAC_SHA512))
//                        .build();
                
//                JwtClaims jwtClaims = jwtConsumer.processToClaims(decryptedValue);
                String expiresClaim = paseto.getClaims().getExpiration().toString();//pasteo.getClaims.jwtClaims.getClaimValue(ClaimKey.EXPIRES.toString(), String.class);
                
                if (("-1").equals(expiresClaim)) {
                    session = Session.create()
                            .withContent(MangooUtils.copyMap(paseto.getClaims().get(ClaimKey.DATA.toString(), Map.class)))
                            .withAuthenticity(paseto.getClaims().get(ClaimKey.AUTHENTICITY.toString(), String.class));
                } else if (LocalDateTime.ofInstant(paseto.getClaims().getExpiration(), ZoneOffset.UTC).isAfter(LocalDateTime.now())) {
                    session = Session.create()
                            .withContent(MangooUtils.copyMap(paseto.getClaims().get(ClaimKey.DATA.toString(), Map.class)))
                            .withAuthenticity(paseto.getClaims().get(ClaimKey.AUTHENTICITY.toString(), String.class))
                            .withExpires(LocalDateTime.ofInstant(paseto.getClaims().getExpiration(), ZoneOffset.UTC));
                } else {
                    //Ignore this and use default session
                }
            } catch (PasetoException e) {
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
                //String decryptedValue = Application.getInstance(Crypto.class).decrypt(cookieValue, this.config.getAuthenticationCookieEncryptionKey());
                
                Paseto paseto = Pasetos.parserBuilder()
                        .setSharedSecret(this.config.getAuthenticationCookieSignKey().getBytes(StandardCharsets.UTF_8))
                        .build()
                        .parse(cookieValue);
                
                
//                JwtConsumer jwtConsumer = new JwtConsumerBuilder()
//                        .setRequireSubject()
//                        .setVerificationKey(new HmacKey(this.config.getAuthenticationCookieSignKey().getBytes(StandardCharsets.UTF_8)))
//                        .setJwsAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.HMAC_SHA512))
//                        .build();
                
//                JwtClaims jwtClaims = jwtConsumer.processToClaims(decryptedValue);
                String expiresClaim = paseto.getClaims().getExpiration().toString();
                
                if (("-1").equals(expiresClaim)) {
                    authentication = Authentication.create()
                            .withSubject(paseto.getClaims().getSubject())
                            .twoFactorAuthentication(paseto.getClaims().get(ClaimKey.TWO_FACTOR.toString(), Boolean.class));
                } else if (LocalDateTime.ofInstant(paseto.getClaims().getExpiration(), ZoneOffset.UTC).isAfter(LocalDateTime.now())) {
                    authentication = Authentication.create()
                            .withExpires(LocalDateTime.ofInstant(paseto.getClaims().getExpiration(), ZoneOffset.UTC))
                            .withSubject(paseto.getClaims().getSubject())
                            .twoFactorAuthentication(paseto.getClaims().get(ClaimKey.TWO_FACTOR.toString(), Boolean.class));
                } else {
                    //Ignore this and use default authentication
                }
            } catch (PasetoException e) {
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
                //String decryptedValue = Application.getInstance(Crypto.class).decrypt(cookieValue, this.config.getFlashCookieEncryptionKey());
//                JwtConsumer jwtConsumer = new JwtConsumerBuilder()
//                    .setVerificationKey(new HmacKey(this.config.getFlashCookieSignKey().getBytes(StandardCharsets.UTF_8)))
//                    .setJwsAlgorithmConstraints(new AlgorithmConstraints(ConstraintType.WHITELIST, AlgorithmIdentifiers.HMAC_SHA512))
//                    .build();
                
                Paseto paseto = Pasetos.parserBuilder()
                        .setSharedSecret(this.config.getFlashCookieSignKey().getBytes(StandardCharsets.UTF_8))
                        .build()
                        .parse(cookieValue);
                
                //JwtClaims jwtClaims = jwtConsumer.processToClaims(decryptedValue);
                LocalDateTime expires = LocalDateTime.ofInstant(paseto.getClaims().getExpiration(), ZoneOffset.UTC);
                
                if (expires.isAfter(LocalDateTime.now())) {
                    if (paseto.getClaims().containsKey(ClaimKey.FORM.toString())) {
                        this.form = CodecUtils.deserializeFromBase64(paseto.getClaims().get(ClaimKey.FORM.toString(), String.class));
                    } 
                    
                    flash = Flash.create()
                            .withContent(MangooUtils.copyMap(paseto.getClaims().get(ClaimKey.DATA.toString(), Map.class))).setDiscard(true);
                }
            } catch (PasetoException e) {
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
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(AuthenticationHandler.class).handleRequest(exchange);
    }
}