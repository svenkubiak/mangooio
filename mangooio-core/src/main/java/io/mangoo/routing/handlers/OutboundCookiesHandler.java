package io.mangoo.routing.handlers;

import io.mangoo.constants.ClaimKey;
import io.mangoo.constants.Const;
import io.mangoo.constants.Default;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.routing.Attachment;
import io.mangoo.utils.CommonUtils;
import io.mangoo.utils.DateUtils;
import io.mangoo.utils.JwtUtils;
import io.mangoo.utils.RequestUtils;
import io.mangoo.utils.internal.MangooUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.CookieImpl;
import jakarta.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
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
        this.config = Objects.requireNonNull(config, Required.CONFIG);
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
            return;
        }

        var requestCookie = exchange.getRequestCookie(config.getSessionCookieName());
        if (session.hasChanged() || (requestCookie == null && session.isKept())) {
            try {
                Map<String, String> claims = session.getValues();
                claims.put(Const.CSRF_TOKEN, session.getCsrf());

                LocalDateTime expires = session.getExpires().withNano(0);
                LocalDateTime now = LocalDateTime.now().withNano(0);

                long ttlSeconds = Duration.between(now, expires).getSeconds();
                ttlSeconds = Math.max(0, ttlSeconds);

                var jwtData = JwtUtils.jwtData()
                        .withKey(config.getSessionCookieKey())
                        .withSecret(config.getSessionCookieSecret())
                        .withIssuer(config.getApplicationName())
                        .withAudience(config.getSessionCookieName())
                        .withSubject(CommonUtils.uuidV6())
                        .withTtlSeconds(ttlSeconds)
                        .withClaims(claims);

                var jwt = JwtUtils.createJwt(jwtData);

                var cookie = new CookieImpl(config.getSessionCookieName())
                        .setValue(jwt)
                        .setSameSiteMode(config.getSessionCookieSameSiteMode())
                        .setExpires(Date.from(
                                expires.atZone(config.getApplicationTimeZone()).toInstant()
                        ))
                        .setHttpOnly(true)
                        .setPath("/")
                        .setSecure(config.isSessionCookieSecure());

                exchange.setResponseCookie(cookie);
            } catch (Exception e) {
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
            String id = authentication.getId();
            if (config.isAuthenticationBlacklist() && !CommonUtils.isBlacklisted(id)) {
               CommonUtils.blacklist(id);
            }

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
                var now = LocalDateTime.now();
                if (authentication.isRememberMe()) {
                    authentication.withExpires(now.plusSeconds(config.getAuthenticationCookieRememberExpires()));
                }

                try {
                    var claims = Map.of(
                            ClaimKey.TWO_FACTOR, String.valueOf(authentication.isTwoFactor()),
                            ClaimKey.REMEMBER_ME, String.valueOf(authentication.isRememberMe()));

                    var jwtData = JwtUtils.jwtData()
                            .withKey(config.getAuthenticationCookieKey())
                            .withSecret(config.getAuthenticationCookieSecret())
                            .withIssuer(config.getApplicationName())
                            .withAudience(config.getAuthenticationCookieName())
                            .withSubject(authentication.getSubject())
                            .withTtlSeconds(Duration.between(LocalDateTime.now(), authentication.getExpires()).getSeconds())
                            .withClaims(claims);

                    var jwt = JwtUtils.createJwt(jwtData);

                    var cookie = new CookieImpl(config.getAuthenticationCookieName())
                            .setValue(jwt)
                            .setSecure(config.isAuthenticationCookieSecure())
                            .setExpires(DateUtils.localDateTimeToDate(authentication.getExpires()))
                            .setHttpOnly(true)
                            .setPath("/")
                            .setSameSiteMode(config.getAuthenticationCookieSameSiteMode());

                    exchange.setResponseCookie(cookie);
                } catch (Exception e) {
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
                Map<String, String> claims = new HashMap<>(flash.getValues());
                boolean formKept = form.isKept();
                if (formKept) {
                    claims.put(ClaimKey.FORM, MangooUtils.serializeFlashFormToBase64(form));
                }

                var cookieName = config.getFlashCookieName();
                var jwtData = JwtUtils.jwtData()
                        .withKey(config.getFlashCookieKey())
                        .withSecret(config.getFlashCookieSecret())
                        .withIssuer(config.getApplicationName())
                        .withAudience(cookieName)
                        .withSubject(CommonUtils.uuidV6())
                        .withTtlSeconds(SIXTY)
                        .withClaims(claims);

                var jwt = JwtUtils.createJwt(jwtData);

                // The size limit applies to the finished cookie, not to the serialized form, as
                // signing and encrypting roughly doubles the payload. Measuring the claim alone
                // would either waste the budget or still produce a cookie the client discards.
                // The kept form is best effort and therefore the part that gets dropped, the
                // flash values were set deliberately by the application and are kept.
                if (formKept && exceedsLimit(cookieName, jwt)) {
                    Map<String, String> reduced = new HashMap<>(claims);
                    reduced.remove(ClaimKey.FORM);
                    jwt = JwtUtils.createJwt(jwtData.withClaims(reduced));

                    LOG.warn("Kept form does not fit into the flash cookie limit of {} bytes and has been dropped", Default.FLASH_COOKIE_MAX_SIZE);
                }

                if (exceedsLimit(cookieName, jwt)) {
                    LOG.warn("Flash cookie exceeds the limit of {} bytes and will likely be discarded by the client", Default.FLASH_COOKIE_MAX_SIZE);
                }

                var cookie = new CookieImpl(cookieName)
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
     * Checks if a cookie exceeds the size a client is willing to accept. Following RFC 6265bis,
     * the limit applies to the sum of the length of the cookie name and its value, the
     * attributes do not count towards it.
     *
     * @param cookieName The name of the cookie
     * @param value The value of the cookie
     *
     * @return True if the cookie exceeds the limit, false otherwise
     */
    private boolean exceedsLimit(String cookieName, String value) {
        return cookieName.length() + value.length() > Default.FLASH_COOKIE_MAX_SIZE;
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