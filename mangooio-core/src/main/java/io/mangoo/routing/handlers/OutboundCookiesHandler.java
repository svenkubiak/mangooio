package io.mangoo.routing.handlers;

import java.time.LocalDateTime;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jws.JsonWebSignature;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.keys.HmacKey;
import org.jose4j.lang.JoseException;

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
    private static final int SIXTY = 60;
    private static final String SAME_SITE_MODE = "Strict";
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
                    .setHttpOnly(true)
                    .setPath("/")
                    .setMaxAge(0)
                    .setSameSite(true)
                    .setSameSiteMode(SAME_SITE_MODE)
                    .setDiscard(true);
            
            exchange.setResponseCookie(cookie);
        } else if (session.hasChanges()) {
            JwtClaims jwtClaims = new JwtClaims();
            jwtClaims.setClaim(ClaimKey.AUTHENTICITY.toString(), session.getAuthenticity());
            jwtClaims.setClaim(ClaimKey.DATA.toString(), session.getValues());
            
            if (session.getExpires() == null) {
                jwtClaims.setClaim(ClaimKey.EXPIRES.toString(), "-1");
            } else {
                jwtClaims.setClaim(ClaimKey.EXPIRES.toString(), session.getExpires().format(DateUtils.formatter));        
            }
            
            JsonWebSignature jsonWebSignature = new JsonWebSignature();
            jsonWebSignature.setKey(new HmacKey(this.config.getSessionCookieSignKey().getBytes(Charsets.UTF_8)));
            jsonWebSignature.setPayload(jwtClaims.toJson());
            jsonWebSignature.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);
            
            try {
                String encryptedValue = Application.getInstance(Crypto.class).encrypt(jsonWebSignature.getCompactSerialization(), this.config.getSessionCookieEncryptionKey());
                Cookie cookie = new CookieImpl(this.config.getSessionCookieName())
                        .setValue(encryptedValue)
                        .setSameSite(true)
                        .setSameSiteMode(SAME_SITE_MODE)
                        .setHttpOnly(true)
                        .setPath("/")
                        .setSecure(this.config.isSessionCookieSecure());
                
                if (session.getExpires() != null) {
                    cookie.setExpires(DateUtils.localDateTimeToDate(session.getExpires()));
                }

                exchange.setResponseCookie(cookie);
            } catch (Exception e) { //NOSONAR
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
            
            JwtClaims jwtClaims = new JwtClaims();
            jwtClaims.setSubject(authentication.getSubject());
            jwtClaims.setClaim(ClaimKey.TWO_FACTOR.toString(), authentication.isTwoFactor());
            
            if (authentication.getExpires() == null) {
                jwtClaims.setClaim(ClaimKey.EXPIRES.toString(), "-1");    
            } else {
                jwtClaims.setClaim(ClaimKey.EXPIRES.toString(), authentication.getExpires().format(DateUtils.formatter));   
            }
            
            JsonWebSignature jsonWebSignature = new JsonWebSignature();
            jsonWebSignature.setKey(new HmacKey(this.config.getAuthenticationCookieSignKey().getBytes(Charsets.UTF_8)));
            jsonWebSignature.setPayload(jwtClaims.toJson());
            jsonWebSignature.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);
            
            try {
                String encryptedValue = Application.getInstance(Crypto.class).encrypt(jsonWebSignature.getCompactSerialization(), this.config.getAuthenticationCookieEncryptionKey());
                final Cookie cookie = new CookieImpl(this.config.getAuthenticationCookieName())
                        .setValue(encryptedValue)
                        .setSecure(this.config.isAuthenticationCookieSecure())
                        .setHttpOnly(true)
                        .setSameSite(true)
                        .setPath("/")
                        .setSameSiteMode(SAME_SITE_MODE);
                        
                if (authentication.getExpires() != null) {
                    cookie.setExpires(DateUtils.localDateTimeToDate(authentication.getExpires()));
                } 
                
                exchange.setResponseCookie(cookie);
            } catch (JoseException e) { //NOSONAR
                LOG.error("Failed to generate authentication cookie", e);
            }
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
                    .setSecure(this.config.isFlashCookieSecure())
                    .setPath("/")
                    .setSameSite(true)
                    .setSameSiteMode(SAME_SITE_MODE)
                    .setDiscard(true)
                    .setMaxAge(0);

            exchange.setResponseCookie(cookie);
        } else if (flash.hasContent() || form.flashify()) {
            try {
                JwtClaims jwtClaims = new JwtClaims();
                jwtClaims.setClaim(ClaimKey.DATA.toString(), flash.getValues());
                
                if (form.flashify()) {
                    jwtClaims.setClaim(ClaimKey.FORM.toString(), CodecUtils.serializeToBase64(form));
                }
                
                LocalDateTime expires = LocalDateTime.now().plusSeconds(SIXTY);
                jwtClaims.setClaim(ClaimKey.EXPIRES.toString(), expires.format(DateUtils.formatter));
                
                JsonWebSignature jsonWebSignature = new JsonWebSignature();
                jsonWebSignature.setKey(new HmacKey(this.config.getFlashCookieSignKey().getBytes(Charsets.UTF_8)));
                jsonWebSignature.setPayload(jwtClaims.toJson());
                jsonWebSignature.setAlgorithmHeaderValue(AlgorithmIdentifiers.HMAC_SHA512);
                
                String encryptedValue = Application.getInstance(Crypto.class).encrypt(jsonWebSignature.getCompactSerialization(), this.config.getFlashCookieEncryptionKey());
                final Cookie cookie = new CookieImpl(this.config.getFlashCookieName())
                        .setValue(encryptedValue)
                        .setSecure(this.config.isFlashCookieSecure())
                        .setHttpOnly(true)
                        .setSameSite(true)
                        .setPath("/")
                        .setSameSiteMode(SAME_SITE_MODE)
                        .setExpires(DateUtils.localDateTimeToDate(expires));
                
                exchange.setResponseCookie(cookie);
            } catch (Exception e) { //NOSONAR
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
    @SuppressWarnings("all")
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(ResponseHandler.class).handleRequest(exchange);
    }
}