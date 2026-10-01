package io.mangoo.routing.handlers;

import io.mangoo.constants.ClaimKey;
import io.mangoo.constants.Const;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooJwtException;
import io.mangoo.interfaces.TokenBlacklist;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.utils.CommonUtils;
import io.mangoo.utils.JwtUtils;
import io.mangoo.utils.internal.MangooUtils;
import io.mangoo.utils.RequestUtils;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Objects;

public class InboundCookiesHandler implements HttpHandler {
    private static final Logger LOG = LogManager.getLogger(InboundCookiesHandler.class);
    private final Config config;
    private final TokenBlacklist tokenBlacklist;
    private Form form;

    @Inject
    public InboundCookiesHandler(Config config, TokenBlacklist tokenBlacklist) {
        this.config = Objects.requireNonNull(config, Required.CONFIG);
        this.tokenBlacklist = Objects.requireNonNull(tokenBlacklist, "tokenBlacklist can not be null");
    }
    
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        Attachment attachment = exchange.getAttachment(RequestUtils.getAttachmentKey());
        attachment.setSession(getSessionCookie(exchange));
        attachment.setAuthentication(getAuthenticationCookie(exchange));
        attachment.setFlash(getFlashCookie(exchange));
        attachment.setForm(form);

        exchange.putAttachment(RequestUtils.getAttachmentKey(), attachment);
        nextHandler(exchange);
    }

    protected Session getSessionCookie(HttpServerExchange exchange) {
        var session = Session.create()
                .withContent(new HashMap<>())
                .withCsrf(CommonUtils.randomString(32))
                .withExpires(LocalDateTime.now()
                        .plusSeconds(config.getSessionCookieTokenExpires())
                        .withNano(0));

        String cookieValue = getCookieValue(exchange, config.getSessionCookieName());

        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                var jwtData = JwtUtils.jwtData()
                        .withKey(config.getSessionCookieKey())
                        .withSecret(config.getSessionCookieSecret())
                        .withIssuer(config.getApplicationName())
                        .withAudience(config.getSessionCookieName())
                        .withTtlSeconds(config.getSessionCookieTokenExpires());

                var jwtClaimsSet = JwtUtils.parseJwt(cookieValue, jwtData);

                LocalDateTime expires = LocalDateTime.ofInstant(
                        jwtClaimsSet.getExpirationTime().toInstant(),
                        config.getApplicationTimeZone()
                ).withNano(0);

                session = Session.create()
                        .withContent(CommonUtils.toStringMap(JwtUtils.extractCustomClaims(jwtClaimsSet).getClaims()))
                        .withCsrf(jwtClaimsSet.getClaimAsString(Const.CSRF_TOKEN))
                        .withExpires(expires);
            } catch (ParseException | MangooJwtException e) {
                LOG.warn("Failed to parse session cookie", e);
            }
        }

        return session;
    }

    protected Authentication getAuthenticationCookie(HttpServerExchange exchange) {
        var authentication = Authentication.create()
                .withSubject(null)
                .withExpires(LocalDateTime.now().plusSeconds(config.getAuthenticationCookieTokenExpires()));
        
        String cookieValue = getCookieValue(exchange, config.getAuthenticationCookieName());
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                var jwtData = JwtUtils.jwtData()
                        .withKey(config.getAuthenticationCookieKey())
                        .withSecret(config.getAuthenticationCookieSecret())
                        .withIssuer(config.getApplicationName())
                        .withAudience(config.getAuthenticationCookieName())
                        .withTtlSeconds(config.getAuthenticationCookieRememberExpires());

                var jwtClaimsSet = JwtUtils.parseJwt(cookieValue, jwtData);

                if (!(config.isAuthenticationBlacklist()
                        && tokenBlacklist.isRevoked(jwtClaimsSet.getJWTID(), jwtClaimsSet.getSubject(), jwtClaimsSet.getIssueTime().toInstant()))) {

                    authentication = Authentication.create()
                            .rememberMe(Boolean.parseBoolean(jwtClaimsSet.getClaimAsString(ClaimKey.REMEMBER_ME)))
                            .withSubject(jwtClaimsSet.getSubject())
                            .withId(jwtClaimsSet.getJWTID())
                            .twoFactorAuthentication(Boolean.parseBoolean(jwtClaimsSet.getClaimAsString(ClaimKey.TWO_FACTOR)))
                            .withExpires(LocalDateTime.ofInstant(
                                    jwtClaimsSet.getExpirationTime().toInstant(),
                                    config.getApplicationTimeZone()
                            ));
                }
            } catch (ParseException | MangooJwtException e) {
                LOG.warn("Failed to parse authentication cookie", e);
            }
        }

        return authentication;
    }

    protected Flash getFlashCookie(HttpServerExchange exchange) {
        var flash = Flash.create();
        
        final String cookieValue = getCookieValue(exchange, config.getFlashCookieName());
        if (StringUtils.isNotBlank(cookieValue)) {
            try {
                var jwtData = JwtUtils.jwtData()
                        .withKey(config.getFlashCookieKey())
                        .withSecret(config.getFlashCookieSecret())
                        .withIssuer(config.getApplicationName())
                        .withAudience(config.getFlashCookieName())
                        .withTtlSeconds(60);

                var jwtClaimSet = JwtUtils.parseJwt(cookieValue, jwtData);

                var formClaim = jwtClaimSet.getClaimAsString(ClaimKey.FORM);
                if (StringUtils.isNotBlank(formClaim)) {
                    form = MangooUtils.deserializeFlashFormFromBase64(formClaim);
                }

                flash = Flash.create()
                        .withContent(CommonUtils.toStringMap(jwtClaimSet.getClaims()))
                        .setDiscard(true);
            } catch (ParseException | MangooJwtException e) {
                LOG.warn("Failed to parse flash cookie", e);
            }
        }
        
        return flash;
    }
    
    private String getCookieValue(HttpServerExchange exchange, String cookieName) {
        String value = null;
        var cookie = exchange.getRequestCookie(cookieName);
        if (cookie != null) {
            value = cookie.getValue();
        }  

        return value;
    }

    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        Application.getInstance(AuthenticationHandler.class).handleRequest(exchange);
    }
}