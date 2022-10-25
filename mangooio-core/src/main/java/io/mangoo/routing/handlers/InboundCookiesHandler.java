package io.mangoo.routing.handlers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.enums.ClaimKey;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.utils.CodecUtils;
import io.mangoo.utils.MangooUtils;
import io.mangoo.utils.RequestUtils;
import io.mangoo.utils.token.Token;
import io.mangoo.utils.token.TokenParser;
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
    private final Config config;
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
        attachment.setForm(form);

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
        var session = Session.create()
            .withContent(new HashMap<>())
            .withExpires(LocalDateTime.now().plusMinutes(config.getSessionCookieTokenExpires()));
        
        String cookieValue = getCookieValue(exchange, config.getSessionCookieName());
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                Token token = TokenParser.create()
                        .withSharedSecret(config.getSessionCookieSecret())
                        .withCookieValue(cookieValue)
                        .parse();
                
                if (token.expirationIsAfter(LocalDateTime.now())) {
                    session = Session.create()
                            .withContent(MangooUtils.copyMap(token.getClaim(ClaimKey.DATA, Map.class)))
                            .withExpires(token.getExpiration()); 
                }
            } catch (MangooTokenException e) {
                LOG.debug("Failed to parse session cookie", e);
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
        var authentication = Authentication.create()
                .withSubject(null)
                .withExpires(LocalDateTime.now().plusMinutes(config.getAuthenticationCookieTokenExpires()));
        
        String cookieValue = getCookieValue(exchange, config.getAuthenticationCookieName());
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                Token token = TokenParser.create()
                        .withSharedSecret(config.getAuthenticationCookieSecret())
                        .withCookieValue(cookieValue)
                        .parse();
                
                if (token.expirationIsAfter(LocalDateTime.now())) {
                    authentication = Authentication.create()
                            .withExpires(token.getExpiration())
                            .withSubject(token.getSubject())
                            .twoFactorAuthentication(Boolean.parseBoolean(token.getClaim(ClaimKey.TWO_FACTOR, String.class)));
                }
            } catch (MangooTokenException e) {
                LOG.debug("Failed to parse authentication cookie", e);
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
        var flash = Flash.create();
        
        final String cookieValue = getCookieValue(exchange, config.getFlashCookieName());
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                Token token = TokenParser.create()
                        .withSharedSecret(config.getFlashCookieSecret())
                        .withCookieValue(cookieValue)
                        .parse();
                
                if (token.expirationIsAfter(LocalDateTime.now())) {
                    if (token.containsClaim(ClaimKey.FORM)) {
                        form = CodecUtils.deserializeFromBase64(token.getClaim(ClaimKey.FORM, String.class));
                    } 
                    
                    flash = Flash.create()
                            .withContent(MangooUtils.copyMap(token.getPaseto().getClaims().get(ClaimKey.DATA.toString(), Map.class)))
                            .setDiscard(true);
                }
            } catch (MangooTokenException e) {
                LOG.debug("Failed to parse flash cookie", e);
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
        Cookie cookie = exchange.getRequestCookie(cookieName);
        if (cookie != null) {
            value = cookie.getValue();
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