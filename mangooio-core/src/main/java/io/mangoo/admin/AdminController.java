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
import io.mangoo.interfaces.InstanceRegistry;
import io.mangoo.models.InstanceStatus;
import io.mangoo.models.Metrics;
import io.mangoo.routing.Response;
import io.mangoo.routing.Router;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.routes.FileRoute;
import io.mangoo.routing.routes.PathRoute;
import io.mangoo.routing.routes.RequestRoute;
import io.mangoo.scheduler.Scheduler;
import io.mangoo.utils.DateUtils;
import io.mangoo.utils.FileUtils;
import io.mangoo.utils.TotpUtils;
import io.mangoo.utils.internal.MangooUtils;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FilterWith(AdminFilter.class)
public class AdminController {
    private static final Logger LOG = LogManager.getLogger(AdminController.class);
    private static final String ENABLED = "enabled";
    private static final String ADMIN_INDEX = "/@admin";
    private static final String ADMIN_LOGIN = "/@admin/login";
    private static final String ADMIN_CACHE = "/@admin/cache";
    private static final String ADMIN_TWO_FACTOR = "/@admin/twofactor";
    private static final String METRICS = "metrics";
    private static final double HUNDRED_PERCENT = 100.0;
    private static final long STALE_SECONDS = 30;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private final CacheProvider cacheProvider;
    private final AdminActions adminActions;
    private final InstanceHeartbeat instanceHeartbeat;
    private final InstanceRegistry instanceRegistry;
    private final Cache cache;
    private final Config config;

    @Inject
    public AdminController(Config config, CacheProvider cacheProvider, AdminActions adminActions, InstanceHeartbeat instanceHeartbeat, InstanceRegistry instanceRegistry) {
        this.config = Objects.requireNonNull(config, Required.CONFIG);
        this.cache = cacheProvider.getCache(CacheName.APPLICATION);
        this.cacheProvider = Objects.requireNonNull(cacheProvider, Required.CACHE_PROVIDER);
        this.adminActions = Objects.requireNonNull(adminActions, "adminActions can not be null");
        this.instanceHeartbeat = Objects.requireNonNull(instanceHeartbeat, "instanceHeartbeat can not be null");
        this.instanceRegistry = Objects.requireNonNull(instanceRegistry, "instanceRegistry can not be null");
    }

    public Response index() {
        var stream = Application.getInstance(EventBus.class);
        InstanceStatus current = instanceHeartbeat.current();
        List<Map<String, Object>> instances = getInstances(current);
        var response = Response.ok()
                .render(ENABLED, config.isMetricsEnable())
                .render("uptime", DateUtils.getPrettyTime(Application.getStart()))
                .render("events", stream.getHandledEvents())
                .render("failedEvents", stream.getFailedEvents())
                .render("queues", stream.getSubscribers())
                .render("connections", current.connections())
                .render("instances", instances)
                .render("instanceTotals", getTotals(instances))
                .render("shared", config.isPersistenceEnabled())
                .render("otlp", config.isOtlpEnable())
                .render("warnings", cache.get(Key.MANGOOIO_WARNINGS))
                .render("system", getSystem())
                .render("features", getFeatures())
                .render("scope", getScope())
                .template(Template.adminPath());

        if (config.isMetricsEnable()) {
            var metrics = Application.getInstance(Metrics.class);
            Map<String, Long> statusCodes = new TreeMap<>();
            Map<String, Long> statusClasses = new TreeMap<>(Map.of("2xx", 0L, "3xx", 0L, "4xx", 0L, "5xx", 0L));
            for (Entry<Integer, LongAdder> entry : metrics.getResponseMetrics().entrySet()) {
                long count = entry.getValue().longValue();
                statusCodes.put(String.valueOf(entry.getKey()), count);
                statusClasses.merge(entry.getKey() / 100 + "xx", count, Long::sum);
            }

            long totalRequests = statusCodes.values().stream().mapToLong(Long::longValue).sum();
            long errorRequests = statusClasses.get("5xx");

            double errorRate = 0;
            if (errorRequests > 0) {
                errorRate = (HUNDRED_PERCENT / totalRequests) * errorRequests;
            }

            response.render(METRICS, statusCodes)
                    .render("statusClasses", statusClasses)
                    .render("totalRequests", totalRequests)
                    .render("avgRequestTime", metrics.getAvgRequestTime())
                    .render("maxRequestTime", metrics.getMaxRequestTime())
                    .render("errorRate", errorRate);

            Metrics.Window window = metrics.getWindow();
            response.render("recentRequests", window.requests())
                    .render("recentErrorRate", window.requests() == 0 ? 0 : (HUNDRED_PERCENT / window.requests()) * window.errors())
                    .render("recentAvgRequestTime", window.requests() == 0 ? -1 : window.avgRequestTime())
                    .render("recentMaxRequestTime", window.maxRequestTime());
        }

        return response;
    }

    private Map<String, Object> getSystem() {
        var runtime = Runtime.getRuntime();
        long heapUsed = runtime.totalMemory() - runtime.freeMemory();
        long heapMax = runtime.maxMemory() == Long.MAX_VALUE ? runtime.totalMemory() : runtime.maxMemory();

        Map<String, Object> system = new HashMap<>();
        system.put("started", Application.getStart().format(DATE_TIME_FORMATTER));
        system.put("java", Runtime.version().toString());
        system.put("heapUsed", heapUsed);
        system.put("heapMax", heapMax);
        system.put("heapUsedReadable", FileUtils.readableFileSize(heapUsed));
        system.put("heapMaxReadable", FileUtils.readableFileSize(heapMax));

        return system;
    }

    // The status of this instance is always the current one, the others are those of their last heartbeat
    private List<Map<String, Object>> getInstances(InstanceStatus current) {
        Map<String, InstanceStatus> statuses = new TreeMap<>();
        instanceRegistry.getInstances().forEach(status -> statuses.put(status.id(), status));
        statuses.put(current.id(), current);

        var now = Instant.now();
        List<Map<String, Object>> instances = new ArrayList<>();
        for (InstanceStatus status : statuses.values()) {
            long seen = Math.max(0, Duration.between(status.seen(), now).toSeconds());
            Map<String, Object> instance = new HashMap<>();
            instance.put("id", status.id());
            instance.put("current", status.id().equals(current.id()));
            instance.put("version", status.version());
            instance.put("started", DateUtils.getPrettyTime(LocalDateTime.ofInstant(status.started(), ZoneId.systemDefault())));
            instance.put("seen", seen);
            instance.put("stale", seen > STALE_SECONDS);
            instance.put("heap", FileUtils.readableFileSize(status.heapUsed()) + " / " + FileUtils.readableFileSize(status.heapMax()));
            instance.put("connections", status.connections());
            instance.put("requests", status.requests());
            instance.put("errors", status.errors());
            instance.put("avgRequestTime", status.requests() == 0 ? -1 : status.avgRequestTime());
            instance.put("failedEvents", status.failedEvents());
            instances.add(instance);
        }

        return instances;
    }

    private static Map<String, Long> getTotals(List<Map<String, Object>> instances) {
        Map<String, Long> totals = new HashMap<>();
        for (String key : List.of("connections", "requests", "errors", "failedEvents")) {
            totals.put(key, instances.stream().mapToLong(instance -> ((Number) instance.get(key)).longValue()).sum());
        }

        return totals;
    }

    private Map<String, Boolean> getFeatures() {
        Map<String, Boolean> features = new LinkedHashMap<>();
        features.put("Metrics", config.isMetricsEnable());
        features.put("Scheduler", config.isSchedulerEnabled());
        features.put("Persistence", config.isPersistenceEnabled());
        features.put("Embedded MongoDB", config.isPersistenceEnabled() && Boolean.TRUE.equals(config.isMongoEmbedded(Default.PERSISTENCE_PREFIX)));
        features.put("CORS", config.isCorsEnable());
        features.put("OpenTelemetry", config.isOtlpEnable());
        features.put("Proxy headers", config.isConnectorProxy());
        features.put("Token blacklist", config.isAuthenticationBlacklist());
        features.put("Strict parameters", config.isParameterStrict());
        features.put("Secure session cookie", config.isSessionCookieSecure());
        features.put("Secure authentication cookie", config.isAuthenticationCookieSecure());
        features.put("Admin two factor", StringUtils.isNotBlank(config.getApplicationAdminSecret()));

        return features;
    }

    public Response routes() {
        List<Map<String, Object>> requests = Router.getRequestRoutes()
                .filter(route -> route.getControllerClass() != AdminController.class)
                .sorted(Comparator.comparing(RequestRoute::getUrl).thenComparing(AdminController::getMethods))
                .map(route -> Map.<String, Object>of(
                        "methods", getMethods(route),
                        "url", route.getUrl(),
                        "controller", route.getControllerClass().getSimpleName(),
                        "controllerName", route.getControllerClass().getName(),
                        "action", route.getControllerMethod(),
                        "authentication", route.hasAuthentication(),
                        "nonBlocking", route.isBlocking()))
                .toList();

        List<String> files = Stream.concat(
                        Router.getFileRoutes().map(FileRoute::getUrl),
                        Router.getPathRoutes().map(PathRoute::getUrl))
                .sorted()
                .toList();

        return Response.ok()
                .render("requests", requests)
                .render("serverSentEvents", Application.getServerSentEventConnections())
                .render("webSockets", Application.getWebSocketConnections())
                .render("files", files)
                .template(Template.routesPath());
    }

    private static String getMethods(RequestRoute route) {
        if (route.hasMultipleMethods()) {
            return Arrays.stream(route.getMethods()).map(Enum::name).collect(Collectors.joining(", "));
        }

        return route.getMethod().name();
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
                .render("scope", getScope())
                .template(Template.cachePath());
    }

    @FilterWith(CsrfFilter.class)
    public Response clearCache(Form form, Flash flash) {
        String name = form.get("name");
        if (StringUtils.isBlank(name) || !cacheProvider.getCaches().containsKey(name)) {
            flash.setError("The cache does not exist.");
            return Response.redirect(ADMIN_CACHE);
        }

        adminActions.clearCache(name);
        flash.setSuccess("Cache " + name + " is cleared on " + getScope() + ".");

        return Response.redirect(ADMIN_CACHE);
    }

    @FilterWith(CsrfFilter.class)
    public Response resetMetrics(Flash flash) {
        adminActions.resetMetrics();
        flash.setSuccess("Metrics are reset on " + getScope() + ".");

        return Response.redirect(ADMIN_INDEX);
    }

    // Actions reach the other instances through the MessageBroker, which is only shared with persistence
    private String getScope() {
        return config.isPersistenceEnabled() ? "all instances" : "this instance";
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