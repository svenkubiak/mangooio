package io.mangoo.routing.handlers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.ClaimKey;
import io.mangoo.models.Subject;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.utils.CodecUtils;
import io.mangoo.utils.RequestUtils;
import io.mangoo.utils.cookie.CookieParser;
import io.mangoo.utils.cookie.CookieUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

/**
 *
 * @author svenkubiak
 *
 */
public class InboundCookiesHandler implements HttpHandler {
    private static final Logger LOG = LogManager.getLogger(InboundCookiesHandler.class);
    private static final Config CONFIG = Application.getConfig();
    private static final int TOKEN_LENGTH = 16;
    private Subject subject;
    private Form form = null;
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Attachment attachment = exchange.getAttachment(RequestUtils.ATTACHMENT_KEY);
        attachment.setSession(getSessionCookie(exchange));
        attachment.setAuthentication(getAuthenticationCookie(exchange));
        attachment.setSubject(this.subject);
        attachment.setFlash(getFlashCookie(exchange));
        attachment.setForm(this.form);

        exchange.putAttachment(RequestUtils.ATTACHMENT_KEY, attachment);
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
                .withContent(CookieUtils.getCookieValue(exchange, CONFIG.getSessionCookieName()))
                .withSecret(CONFIG.getApplicationSecret())
                .isEncrypted(CONFIG.isSessionCookieEncrypt());

        if (cookieParser.hasValidSessionCookie()) {
            session = Session.build()
                    .withContent(cookieParser.getSessionValues())
                    .withAuthenticity(cookieParser.getAuthenticity())
                    .withExpires(cookieParser.getExpiresDate());
        } else {
            session = Session.build()
                    .withContent(new HashMap<>())
                    .withAuthenticity(RandomStringUtils.randomAlphanumeric(TOKEN_LENGTH))
                    .withExpires(LocalDateTime.now().plusSeconds(CONFIG.getSessionExpires()));
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
                .withContent(CookieUtils.getCookieValue(exchange, CONFIG.getAuthenticationCookieName()))
                .withSecret(CONFIG.getApplicationSecret())
                .isEncrypted(CONFIG.isAuthenticationCookieEncrypt());
        
        if (cookieParser.hasValidAuthenticationCookie()) {
            authentication = Application.getInstance(Authentication.class)
                    .withExpires(cookieParser.getExpiresDate())
                    .withAuthenticatedUser(cookieParser.getAuthenticatedUser());
            
            this.subject = new Subject(cookieParser.getAuthenticatedUser(), true);
        } else {
            authentication = Application.getInstance(Authentication.class)
                    .withExpires(LocalDateTime.now().plusSeconds(CONFIG.getAuthenticationExpires()))
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
        final String cookieValue = CookieUtils.getCookieValue(exchange, CONFIG.getFlashCookieName());
        
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                Jws<Claims> jwsClaims = Jwts.parser()
                        .setSigningKey(CONFIG.getApplicationSecret())
                        .parseClaimsJws(cookieValue);

                Claims claims = jwsClaims.getBody();
                final Map<String, String> values = claims.get(ClaimKey.DATA.toString(), Map.class);

                if (claims.containsKey(ClaimKey.FORM.toString())) {
                    this.form = CodecUtils.deserializeFromString(claims.get(ClaimKey.FORM.toString(), String.class));
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
}