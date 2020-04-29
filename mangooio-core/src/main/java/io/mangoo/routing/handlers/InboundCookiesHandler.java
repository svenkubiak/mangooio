package io.mangoo.routing.handlers;

import java.nio.charset.Charset;
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
    private static final Logger LOG = LogManager.getLogger(InboundCookiesHandler.class);
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;
    private static final int STRING_LENGTH = 32;
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
            .withAuthenticity(MangooUtils.randomString(STRING_LENGTH))
            .withExpires(LocalDateTime.now().plusMinutes(this.config.getSessionCookieTokenExpires()));
        
        String cookieValue = getCookieValue(exchange, this.config.getSessionCookieName());
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                Paseto paseto = Pasetos.parserBuilder()
                        .setSharedSecret(this.config.getSessionCookieSecret().getBytes(CHARSET))
                        .build()
                        .parse(cookieValue);

                LocalDateTime expiration = LocalDateTime.ofInstant(paseto.getClaims().getExpiration(), ZONE_OFFSET);

                if (expiration.isAfter(LocalDateTime.now())) {
                    session = Session.create()
                            .withContent(MangooUtils.copyMap(paseto.getClaims().get(ClaimKey.DATA.toString(), Map.class))) // FIX ME
                            .withAuthenticity(paseto.getClaims().get(ClaimKey.AUTHENTICITY.toString(), String.class))
                            .withExpires(LocalDateTime.ofInstant(paseto.getClaims().getExpiration(), ZONE_OFFSET)); 
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
        Authentication authentication = Authentication.create()
                .withSubject(null)
                .withExpires(LocalDateTime.now().plusMinutes(this.config.getAuthenticationCookieTokenExpires()));
        
        String cookieValue = getCookieValue(exchange, this.config.getAuthenticationCookieName());
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                Paseto paseto = Pasetos.parserBuilder()
                        .setSharedSecret(this.config.getAuthenticationCookieSecret().getBytes(CHARSET))
                        .build()
                        .parse(cookieValue);
                
                LocalDateTime expiration = LocalDateTime.ofInstant(paseto.getClaims().getExpiration(), ZONE_OFFSET);
                
                if (expiration.isAfter(LocalDateTime.now())) {
                    authentication = Authentication.create()
                            .withExpires(expiration)
                            .withSubject(paseto.getClaims().getSubject())
                            .twoFactorAuthentication(Boolean.valueOf(paseto.getClaims().get(ClaimKey.TWO_FACTOR.toString(), String.class)));
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
                Paseto paseto = Pasetos.parserBuilder()
                        .setSharedSecret(this.config.getFlashCookieSecret().getBytes(CHARSET))
                        .build()
                        .parse(cookieValue);
                
                LocalDateTime expiration = LocalDateTime.ofInstant(paseto.getClaims().getExpiration(), ZONE_OFFSET);
                
                if (expiration.isAfter(LocalDateTime.now())) {
                    if (paseto.getClaims().containsKey(ClaimKey.FORM.toString())) {
                        this.form = CodecUtils.deserializeFromBase64(paseto.getClaims().get(ClaimKey.FORM.toString(), String.class));
                    } 
                    
                    flash = Flash.create()
                            .withContent(MangooUtils.copyMap(paseto.getClaims().get(ClaimKey.DATA.toString(), Map.class))) //FIX ME
                            .setDiscard(true);
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