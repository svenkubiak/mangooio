package io.mangoo.admin;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.mangoo.annotations.FilterWith;
import io.mangoo.async.EventBus;
import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheImpl;
import io.mangoo.cache.CacheProvider;
import io.mangoo.constants.*;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooHashingException;
import io.mangoo.exceptions.MangooJwtException;
import io.mangoo.filters.AdminFilter;
import io.mangoo.filters.CsrfFilter;
import io.mangoo.models.Metrics;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Request;
import io.mangoo.scheduler.Scheduler;
import io.mangoo.utils.DateUtils;
import io.mangoo.utils.FileUtils;
import io.mangoo.utils.TotpUtils;
import io.mangoo.utils.internal.MangooUtils;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

@FilterWith(AdminFilter.class)
public class AdminController {
    private static final Logger LOG = LogManager.getLogger(AdminController.class);
    private static final String ENABLED = "enabled";
    private static final String ADMIN_INDEX = "/@admin";
    private static final String ADMIN_LOGIN = "/@admin/login";
    private static final String ADMIN_TWO_FACTOR = "/@admin/twofactor";
    private static final String METRICS = "metrics";
    private static final double HUNDRED_PERCENT = 100.0;
    private final CacheProvider cacheProvider;
    private final Cache cache;
    private final Config config;

    @Inject
    public AdminController(Config config, CacheProvider cacheProvider) {
        this.config = Objects.requireNonNull(config, Required.CONFIG);
        this.cache = cacheProvider.getCache(CacheName.APPLICATION);
        this.cacheProvider = Objects.requireNonNull(cacheProvider, Required.CACHE_PROVIDER);
    }

    public Response index() {
        var stream = Application.getInstance(EventBus.class);
        if (config.isMetricsEnable()) {
            var metrics = Application.getInstance(Metrics.class);
            long totalRequests = 0;
            long errorRequests = 0;
            
            for (Entry<Integer, LongAdder> entry :  metrics.getResponseMetrics().entrySet()) {
                if (String.valueOf(entry.getKey()).charAt(0) == '5') {
                    errorRequests = errorRequests + entry.getValue().longValue();
                }
                totalRequests = totalRequests + entry.getValue().longValue();
            }

            double errorRate = 0;
            if (errorRequests > 0) {
                errorRate = (HUNDRED_PERCENT / totalRequests) * errorRequests;
            }
            
            return Response.ok()
                    .render(ENABLED, Boolean.TRUE)
                    .render(METRICS, metrics.getResponseMetrics())
                    .render("uptime", DateUtils.getPrettyTime(Application.getStart()))
                    .render("warnings", cache.get(Key.MANGOOIO_WARNINGS))
                    .render("dataSend", FileUtils.readableFileSize(metrics.getDataSend()))
                    .render("totalRequests", totalRequests)
                    .render("minRequestTime", metrics.getMinRequestTime())
                    .render("avgRequestTime", metrics.getAvgRequestTime())
                    .render("maxRequestTime", metrics.getMaxRequestTime())
                    .render("errorRate", errorRate)
                    .render("mode", Application.getMode())
                    .render("instance", Application.getInstanceId())
                    .render("events", stream.getHandledEvents())
                    .render("subscribers", stream.getNumberOfSubscribers())
                    .template(Template.adminPath());
        }
        
        return Response.ok()
                .render(ENABLED, Boolean.FALSE)
                .render("mode", Application.getMode())
                .render("instance", Application.getInstanceId())
                .render("uptime", DateUtils.getPrettyTime(Application.getStart()))
                .render("events", stream.getHandledEvents())
                .render("subscribers", stream.getNumberOfSubscribers())
                .render("warnings", cache.get(Key.MANGOOIO_WARNINGS))
                .template(Template.adminPath());
    }

    public Response cache() {
        Map<String, CacheStats> statistics = new HashMap<>();
        for (Entry<String, Cache> entry : cacheProvider.getCaches().entrySet()) {
            if (entry.getValue() instanceof CacheImpl cacheImpl) {
                statistics.put(entry.getKey(), cacheImpl.getStats());
            }
        }
        
        return Response.ok()
                .render("statistics", statistics)
                .template(Template.cachePath());
    }

    public Response scheduler() {
        Scheduler scheduler = Application.getInstance(Scheduler.class);
        return Response.ok().render("scheduler", scheduler).template(Template.schedulerPath());
    }

    public Response security() {
        String secret = config.getApplicationAdminSecret();
        String qrCode = null;

        if (StringUtils.isBlank(secret)) {
            secret = TotpUtils.createSecret();
            qrCode = TotpUtils.getQRCode(
                    "mangoo_IO_Admin",
                    Const.NAME_PATTERN.matcher(config.getApplicationName()).replaceAll(""),
                    secret);
        }

        return Response.ok()
                .render("qrcode", qrCode)
                .render("secret", secret)
                .template(Template.toolsPath());
    }

    public Response login() {
        return Response.ok().template(Template.loginPath());
    }
    
    public Response logout() {
        return Response.redirect(ADMIN_INDEX)
                .disposeCookie(MangooUtils.getAdminCookieName(), Application.inProdMode());
    }

    @FilterWith(CsrfFilter.class)
    public Response authenticate(Form form) {
        form.expectValue("username");
        form.expectValue("password");
        
        // The attempt is reserved before the password is verified, so concurrent attempts can not exceed the budget
        if (form.isValid() && MangooUtils.acquireAdminAttempt(MangooUtils.ADMIN_PASSWORD)) {
            boolean valid = false;
            try {
                valid = MangooUtils.isValidAuthentication(form);
            } catch (MangooHashingException e) {
                MangooUtils.releaseAdminAttempt(MangooUtils.ADMIN_PASSWORD);
                LOG.error("Failed to check admin credentials", e);
            }

            if (valid) {
                MangooUtils.resetAdminAttempts(MangooUtils.ADMIN_PASSWORD);
                try {
                    return Response.redirect(ADMIN_INDEX).cookie(MangooUtils.getAdminCookie(true));
                } catch (MangooJwtException e) {
                    LOG.error("Failed to create the admin cookie", e);
                }
            }
        }
        form.invalidate();
        form.keep();

        return Response.redirect(ADMIN_LOGIN);
    }

    @FilterWith(CsrfFilter.class)
    public Response verify(Request request, Form form) {
        form.expectValue("code");
        form.expectNumeric("code");
        form.expectMaxLength("code", 6);
        form.expectMinLength("code", 6);

        // The second factor is only accepted for a caller that passed the first factor
        var claims = MangooUtils.parseAdminCookie(request)
                .filter(MangooUtils::isTwoFactorPending)
                .orElse(null);

        if (claims == null || StringUtils.isBlank(config.getApplicationAdminSecret())) {
            return Response.redirect(ADMIN_LOGIN);
        }

        if (form.isValid()
                && MangooUtils.acquireAdminAttempt(MangooUtils.ADMIN_SECOND_FACTOR)
                && MangooUtils.isValidAdminSecondFactor(config.getApplicationAdminSecret(), form.get("code"))) {

            if (!MangooUtils.consumePreAuthentication(claims)) {
                return Response.redirect(ADMIN_LOGIN);
            }

            MangooUtils.resetAdminAttempts(MangooUtils.ADMIN_SECOND_FACTOR);
            try {
                return Response.redirect(ADMIN_INDEX).cookie(MangooUtils.getAdminCookie(false));
            } catch (MangooJwtException e) {
                LOG.error("Failed to create the admin cookie", e);
            }
        }
        form.invalidate();
        form.keep();

        return Response.redirect(ADMIN_TWO_FACTOR);
    }
    
    public Response twofactor() {
        return Response.ok().template(Template.twoFactorPath());
    }
}