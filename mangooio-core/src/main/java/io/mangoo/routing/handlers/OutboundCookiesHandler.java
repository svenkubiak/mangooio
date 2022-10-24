package io.mangoo.routing.handlers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.enums.ClaimKey;
import io.mangoo.enums.Required;
import io.mangoo.routing.Attachment;
import io.mangoo.utils.CodecUtils;
import io.mangoo.utils.DateUtils;
import io.mangoo.utils.RequestUtils;
import io.mangoo.utils.TokenUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;

/**
 *
 * @author svenkubiak
 *
 */
public class OutboundCookiesHandler implements HttpHandler {
    private static final Logger LOG = LogManager.getLogger(OutboundCookiesHandler.class);
    private static final String SAME_SITE_MODE = "Strict";
    private static final int SIXTY = 60;
    private final Config config;
    private Attachment attachment;

    @Inject
    public OutboundCookiesHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        this.attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());

        setSessionCookie(exchange);
        setFlashCookie(exchange);
        setAuthenticationCookie(exchange);

        nextHandler(exchange);
    }

    /**
     * Sets the session cookie to the current HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected void setSessionCookie(HttpServerExchange exchange) {
        var session = attachment.getSession();
        if (session.isInvalid()) {
            Cookie cookie = new CookieImpl(config.getSessionCookieName())
                    .setSecure(config.isSessionCookieSecure())
                    .setValue("")
                    .setHttpOnly(true)
                    .setPath("/")
                    .setMaxAge(0)
                    .setSameSite(true)
                    .setSameSiteMode(SAME_SITE_MODE)
                    .setDiscard(true);
            
            exchange.setResponseCookie(cookie);
        } else if (session.hasChanges()) {
            String token = TokenUtils.getToken(session.getExpires(), config.getSessionCookieSecret(), Map.of(ClaimKey.DATA, session.getValues()), null);
        
            try {
                final Cookie cookie = new CookieImpl(config.getSessionCookieName())
                        .setValue(token)
                        .setSameSite(true)
                        .setSameSiteMode(SAME_SITE_MODE)
                        .setHttpOnly(true)
                        .setPath("/")
                        .setSecure(config.isSessionCookieSecure());
                
                if (config.isSessionCookieExpires()) {
                    cookie.setExpires(DateUtils.localDateTimeToDate(session.getExpires()));
                }

                exchange.setResponseCookie(cookie);
            } catch (Exception e) { //NOSONAR Intentionally catching exception here
                LOG.error("Failed to generate session cookie", e);
            }
        } else {
            //Ignore and send no cookie to the client
        }
    }

    /**
     * Sets the authentication cookie to the current HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected void setAuthenticationCookie(HttpServerExchange exchange) {
        var authentication = attachment.getAuthentication();
        if (authentication.isInvalid() || authentication.isLogout()) {
            Cookie cookie = new CookieImpl(config.getAuthenticationCookieName())
                    .setSecure(config.isAuthenticationCookieSecure())
                    .setValue("")
                    .setHttpOnly(true)
                    .setPath("/")
                    .setMaxAge(0)
                    .setSameSite(true)
                    .setSameSiteMode(SAME_SITE_MODE)
                    .setDiscard(true);
            
            exchange.setResponseCookie(cookie);
        } else if (authentication.isValid()) {
            if (authentication.isRememberMe()) {
                authentication.withExpires(LocalDateTime.now().plusHours(config.getAuthenticationCookieRememberExpires()));
            } 
            
            String token = TokenUtils.getToken(authentication.getExpires(), config.getAuthenticationCookieSecret(), Map.of(ClaimKey.TWO_FACTOR, String.valueOf(authentication.isTwoFactor())), authentication.getSubject());
            
            final Cookie cookie = new CookieImpl(config.getAuthenticationCookieName())
                    .setValue(token)
                    .setSecure(config.isAuthenticationCookieSecure())
                    .setHttpOnly(true)
                    .setSameSite(true)
                    .setPath("/")
                    .setSameSiteMode(SAME_SITE_MODE);
                        
            if (config.isAuthenticationCookieExpires()) {
                cookie.setExpires(DateUtils.localDateTimeToDate(authentication.getExpires()));
            } 
            
            exchange.setResponseCookie(cookie);
        } else {
            //Ignore and send no cookie to the client
        }
    }

    /**
     * Sets the flash cookie to current HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected void setFlashCookie(HttpServerExchange exchange) {
        var flash = attachment.getFlash();
        var form = attachment.getForm();
        
        if (flash.isDiscard() || flash.isInvalid()) {
            final Cookie cookie = new CookieImpl(config.getFlashCookieName())
                    .setHttpOnly(true)
                    .setValue("")
                    .setSecure(config.isFlashCookieSecure())
                    .setPath("/")
                    .setSameSite(true)
                    .setSameSiteMode(SAME_SITE_MODE)
                    .setDiscard(true)
                    .setMaxAge(0);

            exchange.setResponseCookie(cookie);
        } else if (flash.hasContent() || form.isKept()) {
            try {
                Map<ClaimKey, Object> claims = new HashMap<>();
                claims.put(ClaimKey.DATA, flash.getValues());

                if (form.isKept()) {
                    claims.put(ClaimKey.FORM, CodecUtils.serializeToBase64(form));
                }
                
                LocalDateTime expires = LocalDateTime.now().plusSeconds(SIXTY);
                String token = TokenUtils.getToken(expires, config.getFlashCookieSecret(), claims, null);

                final Cookie cookie = new CookieImpl(config.getFlashCookieName())
                        .setValue(token)
                        .setSecure(config.isFlashCookieSecure())
                        .setHttpOnly(true)
                        .setSameSite(true)
                        .setPath("/")
                        .setSameSiteMode(SAME_SITE_MODE)
                        .setExpires(DateUtils.localDateTimeToDate(expires));
                
                exchange.setResponseCookie(cookie);
            } catch (Exception e) { //NOSONAR Intentionally catching exception here
                LOG.error("Failed to generate flash cookie", e); 
            }
        } else {
            //Ignore and send no cookie to the client
        }
    }

    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        if (config.isCorsEnable()) {
            Application.getInstance(CorsHandler.class).handleRequest(exchange);            
        } else {
            Application.getInstance(ResponseHandler.class).handleRequest(exchange);  
        }
    }
}