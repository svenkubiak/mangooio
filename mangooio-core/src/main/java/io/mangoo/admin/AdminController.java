package io.mangoo.admin;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import com.google.re2j.Pattern;
import io.mangoo.annotations.FilterWith;
import io.mangoo.async.EventBus;
import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheImpl;
import io.mangoo.cache.CacheProvider;
import io.mangoo.constants.*;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooJwtExeption;
import io.mangoo.filters.AdminFilter;
import io.mangoo.models.Metrics;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Form;
import io.mangoo.scheduler.Scheduler;
import io.mangoo.utils.AdminUtils;
import io.mangoo.utils.DateUtils;
import io.mangoo.utils.MangooUtils;
import io.mangoo.utils.totp.TotpUtils;
import io.undertow.server.handlers.CookieImpl;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

import static io.mangoo.utils.AdminUtils.resetLockCounter;

@FilterWith(AdminFilter.class)
public class AdminController {
    private static final Logger LOG = LogManager.getLogger(AdminController.class);
    private static final Pattern PATTERN = Pattern.compile("[^a-zA-Z0-9]");
    private static final String ENABLED = "enabled";
    private static final String ADMIN_INDEX = "/@admin";
    private static final String PERIOD = "30";
    private static final String DIGITS = "6";
    private static final String METRICS = "metrics";
    private static final double HUNDRED_PERCENT = 100.0;
    private final CacheProvider cacheProvider;
    private final Cache cache;
    private final Config config;

    @Inject
    public AdminController(Config config, CacheProvider cacheProvider) {
        this.config = Objects.requireNonNull(config, NotNull.CONFIG);
        this.cache = cacheProvider.getCache(CacheName.APPLICATION);
        this.cacheProvider = Objects.requireNonNull(cacheProvider, NotNull.CACHE_PROVIDER);
    }

    public Response index() {
        boolean enabled = config.isMetricsEnable();
        var stream = Application.getInstance(EventBus.class);

        
        if (enabled) {
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
                    .render("dataSend", MangooUtils.readableFileSize(metrics.getDataSend()))
                    .render("totalRequests", totalRequests)
                    .render("minRequestTime", metrics.getMinRequestTime())
                    .render("avgRequestTime", metrics.getAvgRequestTime())
                    .render("maxRequestTime", metrics.getMaxRequestTime())
                    .render("errorRate", errorRate)
                    .render("mode", Application.getMode())
                    .render("events", stream.getHandledEvents())
                    .render("subscribers", stream.getNumberOfSubscribers())
                    .template(Template.adminPath());
        }
        
        return Response.ok()
                .render(ENABLED, Boolean.FALSE)
                .render("uptime", DateUtils.getPrettyTime(Application.getStart()))
                .render("events", stream.getHandledEvents())
                .render("subscribers", stream.getNumberOfSubscribers())
                .render("warnings", cache.get(Key.MANGOOIO_WARNINGS))
                .template(Template.adminPath());
    }

    public Response cache() {
        Map<String, CacheStats> statistics = new HashMap<>();
        for (Entry<String, Cache> entry : cacheProvider.getCaches().entrySet()) {
            statistics.put(entry.getKey(), ((CacheImpl) entry.getValue()).getStats()); //NOSONAR
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
            qrCode = TotpUtils.getQRCode("mangoo_IO_Admin", PATTERN.matcher(config.getApplicationName()).replaceAll(""), secret, Hmac.SHA512, DIGITS, PERIOD);
        }

        return Response.ok()
                .render("qrcode", qrCode)
                .render("secret", secret)
                .template(Template.toolsPath());
    }

    public Response login() {
        return Response.ok()
                .template(Template.loginPath());
    }
    
    public Response logout() {
        var cookie = new CookieImpl(Default.APPLICATION_ADMIN_COOKIE_NAME)
                .setValue(Strings.EMPTY)
                .setHttpOnly(true)
                .setSecure(Application.inProdMode())
                .setPath("/")
                .setDiscard(true)
                .setExpires(new Date())
                .setSameSiteMode("Strict");
        
        return Response.redirect(ADMIN_INDEX).cookie(cookie);
    }
    
    public Response authenticate(Form form) {
        form.expectValue("username");
        form.expectValue("password");
        
        if (AdminUtils.isNotLocked() && form.isValid()) {
            if (AdminUtils.isValidAuthentication(form)) {
                resetLockCounter();
                try {
                    return Response.redirect(ADMIN_INDEX).cookie(AdminUtils.getAdminCookie(true));
                } catch (MangooJwtExeption e) {
                    AdminUtils.invalidAuthentication();
                }
            } else {
                AdminUtils.invalidAuthentication();
            }
        }
        form.invalidate();
        form.keep();

        return Response.redirect("/@admin/login");
    }

    public Response verify(Form form) {
        form.expectValue("code");
        
        if (AdminUtils.isNotLocked() && form.isValid()) {
            if (TotpUtils.verifiedTotp(config.getApplicationAdminSecret(), form.get("code"))) {
                try {
                    return Response.redirect(ADMIN_INDEX).cookie(AdminUtils.getAdminCookie(false));
                } catch (MangooJwtExeption e) {
                    AdminUtils.invalidAuthentication();
                }
            } else {
                AdminUtils.invalidAuthentication();
            }
        }
        form.invalidate();
        form.keep();
        
        return Response.redirect("/@admin/twofactor");
    }
    
    public Response twofactor() {
        return Response.ok().template(Template.twoFactorPath());
    }
}