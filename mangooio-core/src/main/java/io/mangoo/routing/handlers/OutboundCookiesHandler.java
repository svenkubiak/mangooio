package io.mangoo.routing.handlers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.ClaimKey;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.utils.CodecUtils;
import io.mangoo.utils.DateUtils;
import io.mangoo.utils.RequestUtils;
import io.mangoo.utils.cookie.CookieBuilder;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.Cookie;

/**
 *
 * @author svenkubiak
 *
 */
public class OutboundCookiesHandler implements HttpHandler {
    private static final Config CONFIG = Application.getConfig();
    private Attachment attachment;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        this.attachment = exchange.getAttachment(RequestUtils.ATTACHMENT_KEY);

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
            claims.put(ClaimKey.AUHTNETICITY.toString(), session.getAuthenticity());
            claims.put(ClaimKey.VERSION.toString(), CONFIG.getCookieVersion());
            claims.put(ClaimKey.DATA.toString(), session.getValues());
            
            final LocalDateTime expires = session.getExpires();
            String jwt = Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(DateUtils.localDateTimeToDate(expires))
                    .signWith(SignatureAlgorithm.HS512, CONFIG.getApplicationSecret())
                    .compact();

            if (CONFIG.isSessionCookieEncrypt()) {
                jwt = this.attachment.getCrypto().encrypt(jwt);
            }

            final Cookie cookie = CookieBuilder.create()
                .name(CONFIG.getSessionCookieName())
                .value(jwt)
                .secure(CONFIG.isSessionCookieSecure())
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
            final String cookieName = CONFIG.getAuthenticationCookieName();
            if (authentication.isLogout()) {
                cookie = exchange.getRequestCookies().get(cookieName);
                cookie.setSecure(CONFIG.isAuthenticationCookieSecure());
                cookie.setHttpOnly(true);
                cookie.setPath("/");
                cookie.setMaxAge(0);
                cookie.setDiscard(true);
            } else {
                Map<String, Object> claims = new HashMap<>();
                claims.put(ClaimKey.VERSION.toString(), CONFIG.getAuthCookieVersion());
                
                final LocalDateTime expires = authentication.isRemember() ? LocalDateTime.now().plusHours(CONFIG.getAuthenticationRememberExpires()) : authentication.getExpires();
                String jwt = Jwts.builder()
                        .setClaims(claims)
                        .setSubject(authentication.getAuthenticatedUser())
                        .setExpiration(DateUtils.localDateTimeToDate(expires))
                        .signWith(SignatureAlgorithm.HS512, CONFIG.getApplicationSecret())
                        .compact();
                
                if (CONFIG.isAuthenticationCookieEncrypt()) {
                    jwt = this.attachment.getCrypto().encrypt(jwt);
                }

                cookie = CookieBuilder.create()
                        .name(cookieName)
                        .value(jwt)
                        .secure(CONFIG.isAuthenticationCookieSecure())
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
                claims.put(ClaimKey.FORM.toString(), CodecUtils.serializeToString(form));
            }
            
            final LocalDateTime expires = LocalDateTime.now().plusSeconds(60);
            String jwt = Jwts.builder()
                    .setClaims(claims)
                    .setExpiration(DateUtils.localDateTimeToDate(expires))
                    .signWith(SignatureAlgorithm.HS512, CONFIG.getApplicationSecret())
                    .compact();
            
            final Cookie cookie = CookieBuilder.create()
                    .name(CONFIG.getFlashCookieName())
                    .value(jwt)
                    .secure(CONFIG.isFlashCookieSecure())
                    .httpOnly(true)
                    .expires(expires)
                    .build();

            exchange.setResponseCookie(cookie);
        } else {
            final Cookie cookie = exchange.getRequestCookies().get(CONFIG.getFlashCookieName());
            if (cookie != null) {
                cookie.setHttpOnly(true)
                .setSecure(CONFIG.isFlashCookieSecure())
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