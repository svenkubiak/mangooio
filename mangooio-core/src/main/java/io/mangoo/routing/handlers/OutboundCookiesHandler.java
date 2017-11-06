package io.mangoo.routing.handlers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.inject.Inject;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.ClaimKey;
import io.mangoo.enums.Required;
import io.mangoo.helpers.RequestHelper;
import io.mangoo.helpers.cookie.CookieBuilder;
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
                    .signWith(SignatureAlgorithm.HS512, this.config.getApplicationSecret())
                    .compact();

            if (this.config.isSessionCookieEncrypt()) {
                jwt = this.attachment.getCrypto().encrypt(jwt);
            }

            final Cookie cookie = CookieBuilder.create()
                .name(this.config.getSessionCookieName())
                .value(jwt)
                .secure(this.config.isSessionCookieSecure())
                .httpOnly(true)
                .expires(expires)
                .build();

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
                cookie = exchange.getRequestCookies().get(cookieName);
                cookie.setSecure(this.config.isAuthenticationCookieSecure());
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                cookie.setDiscard(true);
            } else {
                Map<String, Object> claims = new HashMap<>();
                claims.put(ClaimKey.VERSION.toString(), this.config.getAuthenticationCookieVersion());
                claims.put(ClaimKey.TWO_FACTOR.toString(), authentication.isTwoFactor());
                
                final LocalDateTime expires = authentication.isRememberMe() ? LocalDateTime.now().plusHours(this.config.getAuthenticationRememberExpires()) : authentication.getExpires();
                String jwt = Jwts.builder()
                        .setClaims(claims)
                        .setSubject(authentication.getAuthenticatedUser())
                        .setExpiration(DateUtils.localDateTimeToDate(expires))
                        .signWith(SignatureAlgorithm.HS512, this.config.getApplicationSecret())
                        .compact();
                
                if (this.config.isAuthenticationCookieEncrypt()) {
                    jwt = this.attachment.getCrypto().encrypt(jwt);
                }

                cookie = CookieBuilder.create()
                        .name(cookieName)
                        .value(jwt)
                        .secure(this.config.isAuthenticationCookieSecure())
                        .httpOnly(true)
                        .expires(expires)
                        .build();
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
                    .signWith(SignatureAlgorithm.HS512, this.config.getApplicationSecret())
                    .compact();
            
            final Cookie cookie = CookieBuilder.create()
                    .name(this.config.getFlashCookieName())
                    .value(jwt)
                    .secure(this.config.isFlashCookieSecure())
                    .httpOnly(true)
                    .expires(expires)
                    .build();

            exchange.setResponseCookie(cookie);
        } else {
            final Cookie cookie = exchange.getRequestCookies().get(this.config.getFlashCookieName());
            if (cookie != null) {
                cookie.setHttpOnly(true)
                .setSecure(this.config.isFlashCookieSecure())
                .setPath("/")
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