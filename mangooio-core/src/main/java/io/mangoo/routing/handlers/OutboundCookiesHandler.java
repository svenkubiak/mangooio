package io.mangoo.routing.handlers;

import io.mangoo.constants.ClaimKey;
import io.mangoo.constants.Default;
import io.mangoo.constants.NotNull;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.routing.Attachment;
import io.mangoo.utils.CodecUtils;
import io.mangoo.utils.DateUtils;
import io.mangoo.utils.JwtUtils;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
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
                Map<String, String> claims = session.getValues();
                claims.put(Default.CSRF_TOKEN, session.getCsrf());
                var jwt = JwtUtils.createJwt(
                        config.getSessionCookieSecret(),
                        config.getApplicationName(),
                        config.getSessionCookieName(),
                        CodecUtils.uuidV6(),
                        Duration.between(LocalDateTime.now(), session.getExpires()).getSeconds(),
                        claims);

                var cookie = new CookieImpl(config.getSessionCookieName())
                        .setValue(jwt)
                        .setSameSiteMode(config.getSessionCookieSameSiteMode())
                        .setExpires(DateUtils.localDateTimeToDate(session.getExpires()))
                        .setHttpOnly(true)
                        .setPath("/")
                        .setSecure(config.isSessionCookieSecure());

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
                LocalDateTime now = LocalDateTime.now();
                if (authentication.isRememberMe()) {
                    authentication.withExpires(now.plusSeconds(config.getAuthenticationCookieRememberExpires()));
                }

                try {
                    var claims = Map.of(
                            ClaimKey.TWO_FACTOR, String.valueOf(authentication.isTwoFactor()),
                            ClaimKey.REMEMBER_ME, String.valueOf(authentication.isRememberMe()));

                    var jwt = JwtUtils.createJwt(
                            config.getAuthenticationCookieSecret(),
                            config.getApplicationName(),
                            config.getAuthenticationCookieName(),
                            authentication.getSubject(),
                            Duration.between(LocalDateTime.now(), authentication.getExpires()).getSeconds(),
                            claims
                           );

                    var cookie = new CookieImpl(config.getAuthenticationCookieName())
                            .setValue(jwt)
                            .setSecure(config.isAuthenticationCookieSecure())
                            .setExpires(DateUtils.localDateTimeToDate(authentication.getExpires()))
                            .setHttpOnly(true)
                            .setPath("/")
                            .setSameSiteMode(config.getAuthenticationCookieSameSiteMode());

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
                /*
                var tokenBuilder = PasetoBuilder.create()
                        .withExpires(expires)
                        .withSecret(config.getFlashCookieSecret())
                        .withClaims(flash.getValues());
                */

                Map<String, String> claims = new HashMap<>(flash.getValues());
                if (form.isKept()) {
                    claims.put(ClaimKey.FORM, CodecUtils.serializeToBase64(form));
                }

                var jwt = JwtUtils.createJwt(
                        config.getFlashCookieSecret(),
                        config.getApplicationName(),
                        config.getFlashCookieName(),
                        CodecUtils.uuidV6(),
                        SIXTY,
                        claims);

                var cookie = new CookieImpl(config.getFlashCookieName())
                        .setValue(jwt)
                        .setSecure(config.isFlashCookieSecure())
                        .setHttpOnly(true)
                        .setPath("/")
                        .setSameSiteMode(SAME_SITE_MODE)
                        .setExpires(DateUtils.localDateTimeToDate(LocalDateTime.now().plusSeconds(SIXTY)));
                
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