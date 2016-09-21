package io.mangoo.routing.handlers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Session;
import io.mangoo.utils.DateUtils;
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
    private static final Config CONFIG = Application.getConfig();
    private static final int TOKEN_LENGTH = 16;

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Attachment attachment = exchange.getAttachment(RequestUtils.ATTACHMENT_KEY);
        attachment.setSession(getSessionCookie(exchange));
        attachment.setAuthentication(getAuthenticationCookie(exchange));
        attachment.setFlash(getFlashCookie(exchange));

        exchange.putAttachment(RequestUtils.ATTACHMENT_KEY, attachment);
        nextHandler(exchange);
    }

    /**
     * Retrieves the current session from the HttpServerExchange
     *
     * @param exchange The Undertow HttpServerExchange
     */
    protected Session getSessionCookie(HttpServerExchange exchange) {
        Session session = null;

        CookieParser cookieParser = CookieParser.build()
                .withContent(CookieUtils.getCookieValue(exchange, CONFIG.getSessionCookieName()))
                .withSecret(CONFIG.getApplicationSecret())
                .isEncrypted(CONFIG.isSessionCookieEncrypt());

        if (cookieParser.hasValidSessionCookie()) {
            session = Session.build()
                    .withContent(cookieParser.getSessionValues())
                    .withAuthenticityToken(cookieParser.getAuthenticityToken())
                    .withExpires(cookieParser.getExpiresDate());
        } else {
            session = Session.build()
                    .withContent(new HashMap<>())
                    .withAuthenticityToken(RandomStringUtils.randomAlphanumeric(TOKEN_LENGTH))
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
        Authentication authentication = null;

        final CookieParser cookieParser = CookieParser.build()
                .withContent(CookieUtils.getCookieValue(exchange, CONFIG.getAuthenticationCookieName()))
                .withSecret(CONFIG.getApplicationSecret())
                .isEncrypted(CONFIG.isAuthenticationCookieEncrypt());
        
        if (cookieParser.hasValidAuthenticationCookie()) {
            authentication = Authentication.build()
                    .withExpires(cookieParser.getExpiresDate())
                    .withAuthenticatedUser(cookieParser.getAuthenticatedUser());
        } else {
            authentication = Authentication.build()
                    .withExpires(LocalDateTime.now().plusSeconds(CONFIG.getAuthenticationExpires()))
                    .withAuthenticatedUser(null);
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
            Jws<Claims> jwsClaims = Jwts.parser()
                .setSigningKey(CONFIG.getApplicationSecret())
                .parseClaimsJws(cookieValue);

            Claims claims = jwsClaims.getBody();
            LocalDateTime expiration = DateUtils.dateToLocalDateTime(claims.getExpiration());
            if (LocalDateTime.now().isBefore(expiration)) {
                final Map<String, String> values = claims.get("data", Map.class);
                flash = new Flash(values);
                flash.setDiscard(true);
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