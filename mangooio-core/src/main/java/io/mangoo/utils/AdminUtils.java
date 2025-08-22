package io.mangoo.utils;

import io.mangoo.cache.Cache;
import io.mangoo.constants.Default;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooJwtException;
import io.mangoo.routing.bindings.Form;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static io.mangoo.core.Application.getInstance;

public final class AdminUtils {
    private static final Logger LOG = LogManager.getLogger(AdminUtils.class);
    private static final int ADMIN_LOGIN_MAX_RETRIES = 10;
    private static final String MANGOOIO_ADMIN_LOCKED_UNTIL = "mangooio-admin-locked-until";
    private static final String MANGOOIO_ADMIN_LOCK_COUNT = "mangooio-admin-lock-count";

    private AdminUtils() {}

    public static boolean isValidAuthentication(Form form) {
        String username = getInstance(Config.class).getApplicationAdminUsername();
        String password = getInstance(Config.class).getApplicationAdminPassword();

        return StringUtils.isNoneBlank(username, password) &&
               username.equals(form.get("username")) && password.equals(form.get("password"));
    }

    public static Cookie getAdminCookie(boolean requireTwoFactor) throws MangooJwtException {
        Config config = getInstance(Config.class);
        Map<String, String> claims = new HashMap<>();
        claims.put("uuid", MangooUtils.randomString(32));

        if (requireTwoFactor && StringUtils.isNotBlank(config.getApplicationAdminSecret())) {
            claims.put("twofactor", "true");
        }

        try {
            var jwtData = JwtUtils.JwtData.create()
                    .withKey(config.getApplicationSecret())
                    .withSecret(config.getApplicationSecret())
                    .withIssuer(config.getApplicationName())
                    .withAudience(Default.APPLICATION_ADMIN_COOKIE_NAME)
                    .withSubject(CodecUtils.uuidV6())
                    .withTtlSeconds(1800)
                    .withClaims(claims);

            var jwt = JwtUtils.createJwt(jwtData);

            return new CookieImpl(Application.inProdMode() ? "__Host-" + Default.APPLICATION_ADMIN_COOKIE_NAME : Default.APPLICATION_ADMIN_COOKIE_NAME)
                    .setValue(jwt)
                    .setHttpOnly(true)
                    .setSecure(Application.inProdMode())
                    .setPath("/")
                    .setSameSiteMode("Strict");
        } catch (MangooJwtException e) {
            LOG.error("Failed to create admin cookie", e);
            throw new MangooJwtException(e);
        }
    }

    public static void invalidAuthentication() {
        AtomicInteger counter = getInstance(Cache.class).getAndIncrementCounter(MANGOOIO_ADMIN_LOCK_COUNT);
        if (counter.intValue() >= ADMIN_LOGIN_MAX_RETRIES) {
            getInstance(Cache.class).put(MANGOOIO_ADMIN_LOCKED_UNTIL, LocalDateTime.now().plusMinutes(60));
        }

        getInstance(Cache.class).put(MANGOOIO_ADMIN_LOCK_COUNT, counter);
    }

    public static boolean isNotLocked() {
        LocalDateTime lockedUntil = getInstance(Cache.class).get(MANGOOIO_ADMIN_LOCKED_UNTIL);
        return lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now());
    }

    public static void resetLockCounter() {
        getInstance(Cache.class).resetCounter(MANGOOIO_ADMIN_LOCK_COUNT);
    }
}
