package io.mangoo.admin;

import io.mangoo.cache.Cache;
import io.mangoo.constants.Default;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.routing.bindings.Form;
import io.mangoo.utils.MangooUtils;
import io.mangoo.utils.token.TokenBuilder;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static io.mangoo.core.Application.getInstance;

public final class AdminUtils {
    private static final Logger LOG = LogManager.getLogger(AdminController.class);
    private static final int ADMIN_LOGIN_MAX_RETRIES = 10;
    private static final String MANGOOIO_ADMIN_LOCKED_UNTIL = "mangooio-admin-locked-until";
    private static final String MANGOOIO_ADMIN_LOCK_COUNT = "mangooio-admin-lock-count";

    private AdminUtils() {}

    public static boolean isValidAuthentication(Form form) {
        var valid = false;

        String username = getInstance(Config.class).getApplicationAdminUsername();
        String password = getInstance(Config.class).getApplicationAdminPassword();

        if (checkAuthentication(form, username, password)) {
            valid = true;
        }

        return valid;
    }

    private static boolean checkAuthentication(Form form, String username, String password) {
        return StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password) &&
                username.equals(form.get("username")) && password.equals(form.get("password"));
    }

    public static Cookie getAdminCookie(boolean includeTwoFactor) {
        var tokenBuilder = TokenBuilder.create()
                .withSharedSecret(getInstance(Config.class).getApplicationSecret())
                .withExpires(LocalDateTime.now().plusMinutes(30))
                .withClaim("uuid", MangooUtils.randomString(32));

        if (includeTwoFactor && StringUtils.isNotBlank(getInstance(Config.class).getApplicationAdminSecret())) {
            tokenBuilder.withClaim("twofactor", Boolean.TRUE);
        }

        var token = "";
        try {
            token = tokenBuilder.build();
        } catch (MangooTokenException e) {
            LOG.error("Failed to create admin cookie", e);
        }

        return new CookieImpl(Default.ADMIN_COOKIE_NAME)
                .setValue(token)
                .setHttpOnly(true)
                .setSecure(Application.inProdMode())
                .setPath("/")
                .setSameSite(true)
                .setSameSiteMode("Strict");
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
