package io.mangoo.routing.handlers;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.ClaimKey;
import io.mangoo.enums.Required;
import io.mangoo.helpers.RequestHelper;
import io.mangoo.helpers.cookie.CookieParser;
import io.mangoo.models.Subject;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.utils.CodecUtils;
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
    private final SecureRandom secureRandom = new SecureRandom();
    private Config config;
    private Subject subject;
    private Form form;

    @Inject
    public InboundCookiesHandler(Config config) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Attachment attachment = exchange.getAttachment(RequestHelper.ATTACHMENT_KEY);
        attachment.setSession(getSessionCookie(exchange));
        attachment.setAuthentication(getAuthenticationCookie(exchange));
        attachment.setSubject(this.subject);
        attachment.setFlash(getFlashCookie(exchange));
        attachment.setForm(this.form);

        exchange.putAttachment(RequestHelper.ATTACHMENT_KEY, attachment);
        nextHandler(exchange);
    }

    /**
     * Retrieves the current session from the HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected Session getSessionCookie(HttpServerExchange exchange) {
        Session session;

        CookieParser cookieParser = CookieParser.build()
                .withContent(getCookieValue(exchange, this.config.getSessionCookieName()))
                .withSecret(this.config.getApplicationSecret())
                .isEncrypted(this.config.isSessionCookieEncrypt());

        if (cookieParser.hasValidSessionCookie()) {
            session = Session.build()
                    .withContent(cookieParser.getSessionValues())
                    .withAuthenticity(cookieParser.getAuthenticity())
                    .withExpires(cookieParser.getExpiresDate());
        } else {
            session = Session.build()
                    .withContent(new HashMap<>())
                    .withAuthenticity(new BigInteger(80, this.secureRandom).toString(32))
                    .withExpires(LocalDateTime.now().plusSeconds(this.config.getSessionExpires()));
        }

        return session;
    }

    /**
     * Retrieves the current authentication from the HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected Authentication getAuthenticationCookie(HttpServerExchange exchange) {
        Authentication authentication;

        final CookieParser cookieParser = CookieParser.build()
                .withContent(getCookieValue(exchange, this.config.getAuthenticationCookieName()))
                .withSecret(this.config.getApplicationSecret())
                .isEncrypted(this.config.isAuthenticationCookieEncrypt());
        
        if (cookieParser.hasValidAuthenticationCookie()) {
            authentication = Application.getInstance(Authentication.class)
                    .withExpires(cookieParser.getExpiresDate())
                    .withAuthenticatedUser(cookieParser.getAuthenticatedUser())
                    .twoFactorAuthentication(cookieParser.isTwoFactor());

            this.subject = new Subject(cookieParser.getAuthenticatedUser(), true);
        } else {
            authentication = Application.getInstance(Authentication.class)
                    .withExpires(LocalDateTime.now().plusSeconds(this.config.getAuthenticationExpires()))
                    .withAuthenticatedUser(null);
            
            this.subject = new Subject("", false);
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
        Flash flash = null;
        final String cookieValue = getCookieValue(exchange, this.config.getFlashCookieName());
        
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                Jws<Claims> jwsClaims = Jwts.parser()
                        .setSigningKey(this.config.getApplicationSecret())
                        .parseClaimsJws(cookieValue);

                Claims claims = jwsClaims.getBody();
                final Map<String, String> values = claims.get(ClaimKey.DATA.toString(), Map.class);

                if (claims.containsKey(ClaimKey.FORM.toString())) {
                    this.form = CodecUtils.deserializeFromBase64(claims.get(ClaimKey.FORM.toString(), String.class));
                } 
                
                flash = new Flash(values);
                flash.setDiscard(true); 
            } catch (Exception e) { //NOSONAR
                LOG.error("Failed to parse JWT for flash cookie", e);
            }
        }
        
        return flash == null ? new Flash() : flash;
    }

    /**
     * Handles the next request in the handler chain
     *
     * @param exchange The HttpServerExchange
     * @throws Exception Thrown when an exception occurs
     */
    @SuppressWarnings("all")
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(FormHandler.class).handleRequest(exchange);
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
        final Cookie cookie = exchange.getRequestCookies().get(cookieName);
        if (cookie != null) {
            value = cookie.getValue();
        }

        return value;
    }
}