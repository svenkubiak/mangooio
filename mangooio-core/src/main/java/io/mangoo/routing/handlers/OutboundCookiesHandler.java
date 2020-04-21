package io.mangoo.routing.handlers;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;

import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import dev.paseto.jpaseto.PasetoV1LocalBuilder;
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
import io.mangoo.utils.DateUtils;
import io.mangoo.utils.RequestUtils;
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
    private static final Charset CHARSET = StandardCharsets.UTF_8;
    private static final ZoneOffset ZONE_OFFSET = ZoneOffset.UTC;
    private static final String ALGORITHM = "AES";
    private static final String SAME_SITE_MODE = "Strict";
    private static final int SIXTY = 60;
    private Attachment attachment;
    private Config config;
    
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
        Session session = this.attachment.getSession();
        if (session.isInvalid()) {
            Cookie cookie = new CookieImpl(this.config.getSessionCookieName())
                    .setSecure(this.config.isSessionCookieSecure())
                    .setValue("")
                    .setHttpOnly(true)
                    .setPath("/")
                    .setMaxAge(0)
                    .setSameSite(true)
                    .setSameSiteMode(SAME_SITE_MODE)
                    .setDiscard(true);
            
            exchange.setResponseCookie(cookie);
        } else if (session.hasChanges()) {
            PasetoV1LocalBuilder token = Pasetos.V1.LOCAL.builder()
                    .setExpiration(session.getExpires().toInstant(ZONE_OFFSET))
                    .claim(ClaimKey.AUTHENTICITY.toString(), session.getAuthenticity())
                    .claim(ClaimKey.DATA.toString(), session.getValues()).setSharedSecret(new SecretKeySpec(this.config.getSessionCookieSecret().getBytes(CHARSET), ALGORITHM));
        
            try {
                final Cookie cookie = new CookieImpl(this.config.getSessionCookieName())
                        .setValue(token.compact())
                        .setSameSite(true)
                        .setSameSiteMode(SAME_SITE_MODE)
                        .setHttpOnly(true)
                        .setPath("/")
                        .setSecure(this.config.isSessionCookieSecure());
                
                if (this.config.isSessionCookieExpires()) {
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
        Authentication authentication = this.attachment.getAuthentication();
        if (authentication.isInvalid() || authentication.isLogout()) {
            Cookie cookie = new CookieImpl(this.config.getAuthenticationCookieName())
                    .setSecure(this.config.isAuthenticationCookieSecure())
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
                authentication.withExpires(LocalDateTime.now().plusHours(this.config.getAuthenticationCookieRememberExpires()));
            } 
            
            PasetoV1LocalBuilder token = Pasetos.V1.LOCAL.builder().setSubject(authentication.getSubject())
                    .setExpiration(authentication.getExpires().toInstant(ZONE_OFFSET))
                    .claim(ClaimKey.TWO_FACTOR.toString(), authentication.isTwoFactor())
                    .setSharedSecret(new SecretKeySpec(this.config.getAuthenticationCookieSecret().getBytes(CHARSET), ALGORITHM));
            
            final Cookie cookie = new CookieImpl(this.config.getAuthenticationCookieName())
                    .setValue(token.compact())
                    .setSecure(this.config.isAuthenticationCookieSecure())
                    .setHttpOnly(true)
                    .setSameSite(true)
                    .setPath("/")
                    .setSameSiteMode(SAME_SITE_MODE);
                        
            if (this.config.isAuthenticationCookieExpires()) {
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
     * @throws MangooCookieException 
     */
    protected void setFlashCookie(HttpServerExchange exchange) {
        Flash flash = this.attachment.getFlash();
        Form form = this.attachment.getForm();
        
        if (flash.isDiscard() || flash.isInvalid()) {
            final Cookie cookie = new CookieImpl(this.config.getFlashCookieName())
                    .setHttpOnly(true)
                    .setValue("")
                    .setSecure(this.config.isFlashCookieSecure())
                    .setPath("/")
                    .setSameSite(true)
                    .setSameSiteMode(SAME_SITE_MODE)
                    .setDiscard(true)
                    .setMaxAge(0);

            exchange.setResponseCookie(cookie);
        } else if (flash.hasContent() || form.isKept()) {
            try {
                PasetoV1LocalBuilder token = Pasetos.V1.LOCAL.builder()
                        .claim(ClaimKey.DATA.toString(), flash.getValues())
                        .setSharedSecret(new SecretKeySpec(this.config.getFlashCookieSecret().getBytes(CHARSET), ALGORITHM));
                
                if (form.isKept()) {
                    token.claim(ClaimKey.FORM.toString(), CodecUtils.serializeToBase64(form));
                }
                
                LocalDateTime expires = LocalDateTime.now().plusSeconds(SIXTY);
                token.setExpiration(expires.toInstant(ZONE_OFFSET));

                final Cookie cookie = new CookieImpl(this.config.getFlashCookieName())
                        .setValue(token.compact())
                        .setSecure(this.config.isFlashCookieSecure())
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
        if (this.config.isCorsEnable()) {
            Application.getInstance(CorsHandler.class).handleRequest(exchange);            
        } else {
            Application.getInstance(ResponseHandler.class).handleRequest(exchange);  
        }
    }
}