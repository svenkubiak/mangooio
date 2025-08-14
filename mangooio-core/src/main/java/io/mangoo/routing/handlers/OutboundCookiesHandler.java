package io.mangoo.routing.handlers;

import io.mangoo.constants.ClaimKey;
import io.mangoo.constants.Default;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.routing.Attachment;
import io.mangoo.utils.CodecUtils;
import io.mangoo.utils.DateUtils;
import io.mangoo.utils.RequestUtils;
import io.mangoo.utils.paseto.PasetoBuilder;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.Objects;

public class OutboundCookiesHandler implements HttpHandler {
    private static final Logger LOG = LogManager.getLogger(OutboundCookiesHandler.class);
    private static final String SAME_SITE_MODE = "Strict";
    private static final int SIXTY = 60;
    private final Config config;
    private Attachment attachment;

    @Inject
    public OutboundCookiesHandler(Config config) {
        this.config = Objects.requireNonNull(config, NotNull.CONFIG);
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
            var cookie = new CookieImpl(config.getSessionCookieName())
                    .setSecure(config.isSessionCookieSecure())
                    .setValue("")
                    .setHttpOnly(true)
                    .setPath("/")
                    .setMaxAge(0)
                    .setSameSiteMode(config.getSessionCookieSameSiteMode())
                    .setDiscard(true);
            
            exchange.setResponseCookie(cookie);
        } else if (session.hasChanged() || session.isKept()) {
            try {
                String token = PasetoBuilder.create()
                        .withExpires(session.getExpires())
                        .withSecret(config.getSessionCookieSecret())
                        .withClaim(Default.CSRF_TOKEN, session.getCsrf())
                        .withClaims(session.getValues())
                        .build();

                var cookie = new CookieImpl(config.getSessionCookieName())
                        .setValue(token)
                        .setSameSiteMode(config.getSessionCookieSameSiteMode())
                        .setHttpOnly(true)
                        .setPath("/")
                        .setSecure(config.isSessionCookieSecure());
                
                if (Boolean.TRUE.equals(config.isSessionCookieExpires())) {
                    cookie.setExpires(DateUtils.localDateTimeToDate(session.getExpires()));
                }

                exchange.setResponseCookie(cookie);
            } catch (Exception e) { //NOSONAR Intentionally catching exception here
                LOG.error("Failed to generate session cookie", e);
            }
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
            var cookie = new CookieImpl(config.getAuthenticationCookieName())
                    .setSecure(config.isAuthenticationCookieSecure())
                    .setValue("")
                    .setHttpOnly(true)
                    .setPath("/")
                    .setMaxAge(0)
                    .setSameSiteMode(config.getAuthenticationCookieSameSiteMode())
                    .setDiscard(true);
            
            exchange.setResponseCookie(cookie);
        } else if (authentication.isValid()) {
            var authCookie = exchange.getRequestCookie(config.getAuthenticationCookieName());
            if (authCookie == null || authentication.isUpdate()) {
                if (authentication.isRememberMe()) {
                    authentication.withExpires(LocalDateTime.now().plusHours(config.getAuthenticationCookieRememberExpires()));
                }

                try {
                    String token = PasetoBuilder.create()
                            .withExpires(authentication.getExpires())
                            .withSecret(config.getAuthenticationCookieSecret())
                            .withClaim(ClaimKey.TWO_FACTOR, String.valueOf(authentication.isTwoFactor()))
                            .withClaim(ClaimKey.REMEMBER_ME, String.valueOf(authentication.isRememberMe()))
                            .withSubject(authentication.getSubject())
                            .build();

                    var cookie = new CookieImpl(config.getAuthenticationCookieName())
                            .setValue(token)
                            .setSecure(config.isAuthenticationCookieSecure())
                            .setHttpOnly(true)
                            .setPath("/")
                            .setSameSiteMode(config.getAuthenticationCookieSameSiteMode());

                    if (authentication.isRememberMe() || config.isAuthenticationCookieExpires()) {
                        cookie.setExpires(DateUtils.localDateTimeToDate(authentication.getExpires()));
                    }

                    exchange.setResponseCookie(cookie);
                } catch (Exception e) { //NOSONAR Intentionally catching exception here
                    LOG.error("Failed to generate authentication cookie", e);
                }
            }
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
            var cookie = new CookieImpl(config.getFlashCookieName())
                    .setHttpOnly(true)
                    .setValue("")
                    .setSecure(config.isFlashCookieSecure())
                    .setPath("/")
                    .setSameSiteMode(SAME_SITE_MODE)
                    .setDiscard(true)
                    .setMaxAge(0);

            exchange.setResponseCookie(cookie);
        } else if (flash.hasContent() || form.isKept()) {
            try {
                LocalDateTime expires = LocalDateTime.now().plusSeconds(SIXTY);
                var tokenBuilder = PasetoBuilder.create()
                        .withExpires(expires)
                        .withSecret(config.getFlashCookieSecret())
                        .withClaims(flash.getValues());
                
                if (form.isKept()) {
                    tokenBuilder.withClaim(ClaimKey.FORM, CodecUtils.serializeToBase64(form));
                }
                
                String token = tokenBuilder.build();
                var cookie = new CookieImpl(config.getFlashCookieName())
                        .setValue(token)
                        .setSecure(config.isFlashCookieSecure())
                        .setHttpOnly(true)
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