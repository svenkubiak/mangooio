package io.mangoo.routing.handlers;

import io.mangoo.constants.ClaimKey;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.utils.CodecUtils;
import io.mangoo.utils.MangooUtils;
import io.mangoo.utils.RequestUtils;
import io.mangoo.utils.paseto.PasetoParser;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;

public class InboundCookiesHandler implements HttpHandler {
    private static final Logger LOG = LogManager.getLogger(InboundCookiesHandler.class);
    private final Config config;
    private Form form;

    @Inject
    public InboundCookiesHandler(Config config) {
        this.config = Objects.requireNonNull(config, NotNull.CONFIG);
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
    protected Session getSessionCookie(HttpServerExchange exchange) {
        var session = Session.create()
            .withContent(new HashMap<>())
            .withExpires(LocalDateTime.now().plusMinutes(config.getSessionCookieTokenExpires()));
        
        String cookieValue = getCookieValue(exchange, config.getSessionCookieName());
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                var token = PasetoParser.create()
                        .withSecret(config.getSessionCookieSecret())
                        .withValue(cookieValue)
                        .parse();
                
                if (token.getExpires().isAfter(LocalDateTime.now())) {
                    session = Session.create()
                            .withContent(MangooUtils.copyMap(token.getClaims()))
                            .withExpires(token.getExpires());
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
                var token = PasetoParser.create()
                        .withSecret(config.getAuthenticationCookieSecret())
                        .withValue(cookieValue)
                        .parse();
                
                if (token.getExpires().isAfter(LocalDateTime.now())) {
                    authentication = Authentication.create()
                            .rememberMe(token.getClaimAsBoolean(ClaimKey.REMEMBER_ME))
                            .withExpires(token.getExpires())
                            .withSubject(token.getSubject())
                            .twoFactorAuthentication(token.getClaimAsBoolean(ClaimKey.TWO_FACTOR));
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
    protected Flash getFlashCookie(HttpServerExchange exchange) {
        var flash = Flash.create();
        
        final String cookieValue = getCookieValue(exchange, config.getFlashCookieName());
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                var token = PasetoParser.create()
                        .withSecret(config.getFlashCookieSecret())
                        .withValue(cookieValue)
                        .parse();
                
                if (token.getExpires().isAfter(LocalDateTime.now())) {
                    if (token.containsClaim(ClaimKey.FORM)) {
                        form = CodecUtils.deserializeFromBase64(token.getClaim(ClaimKey.FORM));
                    } 
                    
                    flash = Flash.create()
                            .withContent(MangooUtils.copyMap(token.getClaims()))
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
        var cookie = exchange.getRequestCookie(cookieName);
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