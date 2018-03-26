package io.mangoo.routing.handlers;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.common.base.Charsets;
import com.google.inject.Inject;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.ClaimKey;
import io.mangoo.enums.Required;
import io.mangoo.helpers.RequestHelper;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.utils.CodecUtils;
import io.mangoo.utils.DateUtils;
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
    private Attachment attachment;
    private Config config;
    
    @Inject
    public OutboundCookiesHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        this.attachment = exchange.getAttachment(RequestHelper.ATTACHMENT_KEY);

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
        
        if (session != null && session.hasChanges()) {
            Map<String, Object> claims = new HashMap<>();
            claims.put(ClaimKey.AUTHENTICITY.toString(), session.getAuthenticity());
            claims.put(ClaimKey.VERSION.toString(), this.config.getAuthenticationCookieVersion());
            claims.put(ClaimKey.DATA.toString(), session.getValues());

            final LocalDateTime expires = session.getExpires();
            String jwt = Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(DateUtils.localDateTimeToDate(expires))
                    .signWith(SignatureAlgorithm.HS512, this.config.getSessionCookieSignKey().getBytes(Charsets.UTF_8))
                    .compact();

            if (this.config.isSessionCookieEncrypt()) {
                jwt = this.attachment.getCrypto().encrypt(jwt, this.config.getSessionCookieEncryptionKey());
            }
            
            final Cookie cookie = new CookieImpl(this.config.getSessionCookieName())
                .setValue(jwt)
                .setSameSite(true)
                .setSameSiteMode("Strict")
                .setHttpOnly(true)
                .setExpires(Date.from(expires.atZone(ZoneId.systemDefault()).toInstant()))
                .setSecure(this.config.isSessionCookieSecure());

            exchange.setResponseCookie(cookie);
        }
    }

    /**
     * Sets the authentication cookie to the current HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected void setAuthenticationCookie(HttpServerExchange exchange) {
        Authentication authentication = this.attachment.getAuthentication();
        
        if (authentication != null && authentication.hasAuthenticatedUser()) {
            Cookie cookie;
            final String cookieName = this.config.getAuthenticationCookieName();
            if (authentication.isLogout()) {
                cookie = exchange.getRequestCookies().get(cookieName)
                    .setSecure(this.config.isAuthenticationCookieSecure())
                    .setHttpOnly(true)
                    .setPath("/")
                    .setMaxAge(0)
                    .setSameSite(true)
                    .setSameSiteMode("Strict")
                    .setDiscard(true);
            } else {
                Map<String, Object> claims = new HashMap<>();
                claims.put(ClaimKey.VERSION.toString(), this.config.getAuthenticationCookieVersion());
                claims.put(ClaimKey.TWO_FACTOR.toString(), authentication.isTwoFactor());
                
                final LocalDateTime expires = authentication.isRememberMe() ? LocalDateTime.now().plusHours(this.config.getAuthenticationRememberExpires()) : authentication.getExpires();
                String jwt = Jwts.builder()
                        .setClaims(claims)
                        .setSubject(authentication.getAuthenticatedUser())
                        .setExpiration(DateUtils.localDateTimeToDate(expires))
                        .signWith(SignatureAlgorithm.HS512, this.config.getAuthenticationCookieSignKey().getBytes(Charsets.UTF_8))
                        .compact();
                
                if (this.config.isAuthenticationCookieEncrypt()) {
                    jwt = this.attachment.getCrypto().encrypt(jwt, this.config.getAuthenticationCookieEncryptionKey());
                }

                cookie = new CookieImpl(cookieName)
                        .setValue(jwt)
                        .setSecure(this.config.isAuthenticationCookieSecure())
                        .setHttpOnly(true)
                        .setSameSite(true)
                        .setSameSiteMode("Strict")
                        .setExpires(Date.from(expires.atZone(ZoneId.systemDefault()).toInstant()));
            }

            exchange.setResponseCookie(cookie);
        }
    }

    /**
     * Sets the flash cookie to current HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected void setFlashCookie(HttpServerExchange exchange) {
        Flash flash = this.attachment.getFlash();
        Form form = this.attachment.getForm();
        
        if (flash != null && !flash.isDiscard() && (flash.hasContent() || form.flashify())) {
            Map<String, Object> claims = new HashMap<>();
            claims.put(ClaimKey.DATA.toString(), flash.getValues());
            
            if (form.flashify()) {
                claims.put(ClaimKey.FORM.toString(), CodecUtils.serializeToBase64(form));
            }
            
            final LocalDateTime expires = LocalDateTime.now().plusSeconds(60);
            String jwt = Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(DateUtils.localDateTimeToDate(expires))
                    .signWith(SignatureAlgorithm.HS512, this.config.getApplicationSecret().getBytes(Charsets.UTF_8))
                    .compact();
            
            final Cookie cookie = new CookieImpl(this.config.getFlashCookieName())
                    .setValue(jwt)
                    .setSecure(this.config.isFlashCookieSecure())
                    .setHttpOnly(true)
                    .setSameSite(true)
                    .setSameSiteMode("Strict")
                    .setExpires(Date.from(expires.atZone(ZoneId.systemDefault()).toInstant()));
            
            exchange.setResponseCookie(cookie);
        } else {
            final Cookie cookie = exchange.getRequestCookies().get(this.config.getFlashCookieName());
            if (cookie != null) {
                cookie.setHttpOnly(true)
                    .setSecure(this.config.isFlashCookieSecure())
                    .setPath("/")
                    .setSameSite(true)
                    .setSameSiteMode("Strict")
                    .setMaxAge(0);

                exchange.setResponseCookie(cookie);
            }
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