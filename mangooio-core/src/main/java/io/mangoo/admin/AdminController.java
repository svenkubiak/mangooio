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
import io.mangoo.routing.Response;
import io.mangoo.routing.Router;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.routes.FileRoute;
import io.mangoo.routing.routes.PathRoute;
import io.mangoo.routing.routes.RequestRoute;
import io.mangoo.scheduler.Scheduler;
import io.mangoo.utils.TotpUtils;
import io.mangoo.utils.internal.MangooUtils;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@FilterWith(AdminFilter.class)
public class AdminController {
    private static final Logger LOG = LogManager.getLogger(AdminController.class);
    private static final String ADMIN_INDEX = "/@admin";
    private static final String ADMIN_LOGIN = "/@admin/login";
    private static final String ADMIN_CACHE = "/@admin/cache";
    private static final String ADMIN_TWO_FACTOR = "/@admin/twofactor";
    private final CacheProvider cacheProvider;
    private final AdminActions adminActions;
    private final Dashboard dashboard;
    private final Cache cache;
    private final Config config;

    @Inject
    public AdminController(Config config, CacheProvider cacheProvider, AdminActions adminActions, Dashboard dashboard) {
        this.config = Objects.requireNonNull(config, Required.CONFIG);
        this.cache = cacheProvider.getCache(CacheName.APPLICATION);
        this.cacheProvider = Objects.requireNonNull(cacheProvider, Required.CACHE_PROVIDER);
        this.adminActions = Objects.requireNonNull(adminActions, "adminActions can not be null");
        this.dashboard = Objects.requireNonNull(dashboard, "dashboard can not be null");
    }

    public Response index() {
        return Response.ok()
                .render(dashboard.getContent())
                .render("queues", Application.getInstance(EventBus.class).getSubscribers())
                .render("otlp", config.isOtlpEnable())
                .render("warnings", cache.get(Key.MANGOOIO_WARNINGS))
                .render("features", getFeatures(Application.inProdMode()))
                .render("scope", getScope())
                .template(Template.adminPath());
    }

    // Groups of label, value and tone (on, off, value, warn); warn marks a risky setting in prod mode only
    Map<String, List<Map<String, String>>> getFeatures(boolean prod) {
        Map<String, List<Map<String, String>>> features = new LinkedHashMap<>();
        features.put("Security", getSecurityFeatures(prod));
        features.put("Persistence", getPersistenceFeatures(prod));
        features.put("Connectivity", getConnectivityFeatures());
        features.put("Operations", List.of(
                toggle("Metrics", config.isMetricsEnable(), false),
                toggle("Scheduler", config.isSchedulerEnabled(), false),
                toggle("OpenTelemetry", config.isOtlpEnable(), false),
                value("Shutdown timeout", duration(config.getApplicationShutdownTimeout()), false),
                value("Timezone", config.getApplicationTimezone(), false),
                value("Language", config.getApplicationLanguage(), false)));

        return features;
    }

    private List<Map<String, String>> getSecurityFeatures(boolean prod) {
        Map<String, Boolean> secure = new LinkedHashMap<>();
        secure.put("Session", config.isSessionCookieSecure());
        secure.put("Authentication", config.isAuthenticationCookieSecure());
        secure.put("Flash", config.isFlashCookieSecure());

        // A cookie is dedicated if neither secret nor key falls back to application.secret, as the startup warning checks it
        var applicationSecret = config.getApplicationSecret().getBytes(StandardCharsets.UTF_8);
        Map<String, Boolean> dedicated = new LinkedHashMap<>();
        dedicated.put("Session", !Arrays.equals(config.getSessionCookieSecret(), applicationSecret) && !Arrays.equals(config.getSessionCookieKey(), applicationSecret));
        dedicated.put("Authentication", !Arrays.equals(config.getAuthenticationCookieSecret(), applicationSecret) && !Arrays.equals(config.getAuthenticationCookieKey(), applicationSecret));
        dedicated.put("Flash", !Arrays.equals(config.getFlashCookieSecret(), applicationSecret) && !Arrays.equals(config.getFlashCookieKey(), applicationSecret));

        var sessionSameSite = config.getSessionCookieSameSiteMode();
        var authenticationSameSite = config.getAuthenticationCookieSameSiteMode();
        var session = Boolean.TRUE.equals(config.isSessionCookieExpires())
                ? "Expires after " + duration(config.getSessionCookieTokenExpires())
                : "Browser session";

        return List.of(
                toggle("Admin two factor", StringUtils.isNotBlank(config.getApplicationAdminSecret()), prod),
                value("Secure cookies", count(secure), prod && secure.containsValue(false)),
                value("Dedicated cookie secrets", count(dedicated), prod && dedicated.containsValue(false)),
                value("SameSite session / auth", sessionSameSite + " / " + authenticationSameSite,
                        prod && Strings.CI.equalsAny("None", sessionSameSite, authenticationSameSite)),
                value("Session cookie", session, false),
                value("Authentication token", duration(config.getAuthenticationCookieTokenExpires()), false),
                value("Login lock", config.getAuthenticationLock() + " attempts, " + config.getAuthenticationLockDuration() + " min", false),
                toggle("Token blacklist", config.isAuthenticationBlacklist(), false),
                toggle("Strict parameters", config.isParameterStrict(), false),
                toggle("API key", StringUtils.isNotBlank(config.getString(Key.APPLICATION_API_KEY)), false));
    }

    private List<Map<String, String>> getPersistenceFeatures(boolean prod) {
        if (!config.isPersistenceEnabled()) {
            return List.of(toggle("MongoDB", false, false));
        }

        boolean embedded = Boolean.TRUE.equals(config.isMongoEmbedded(Default.PERSISTENCE_PREFIX));
        String mongo;
        if (embedded) {
            mongo = "Embedded";
        } else if (StringUtils.isNotBlank(config.getMongoUrl(Default.PERSISTENCE_PREFIX))) {
            // The connection string is not shown, it names the hosts of the cluster
            mongo = "Connection string";
        } else {
            mongo = config.getMongoHost(Default.PERSISTENCE_PREFIX) + ":" + config.getMongoPort(Default.PERSISTENCE_PREFIX);
        }

        List<Map<String, String>> features = new ArrayList<>();
        features.add(value("MongoDB", mongo, prod && embedded));
        features.add(value("Database", config.getMongoDbName(Default.PERSISTENCE_PREFIX), false));
        if (!embedded) {
            features.add(toggle("Authentication", Boolean.TRUE.equals(config.isMongoAuth(Default.PERSISTENCE_PREFIX)), prod));
        }
        features.add(value("Schema", config.getMongoSchema(), false));

        return features;
    }

    private List<Map<String, String>> getConnectivityFeatures() {
        var smtp = config.getSmtpHost() + ":" + config.getSmtpPort() + " · " + config.getSmtpProtocol()
                + (config.isSmtpAuthentication() ? " · auth" : "");

        return List.of(
                value("HTTP", connector(config.getConnectorHttpHost(), config.getConnectorHttpPort()), false),
                value("HTTPS", connector(config.getConnectorHttpsHost(), config.getConnectorHttpsPort()), false),
                toggle("Proxy headers", config.isConnectorProxy(), false),
                config.isCorsEnable() ? value("CORS", config.getCorsAllowOrigin().pattern(), false) : toggle("CORS", false, false),
                value("SMTP", smtp, false));
    }

    private static Map<String, String> toggle(String label, boolean active, boolean warnIfOff) {
        String tone = active ? "on" : "off";
        return feature(label, active ? "On" : "Off", !active && warnIfOff ? "warn" : tone);
    }

    private static Map<String, String> value(String label, String value, boolean warn) {
        return feature(label, value, warn ? "warn" : "value");
    }

    private static Map<String, String> feature(String label, String value, String tone) {
        Map<String, String> feature = new LinkedHashMap<>();
        feature.put("label", label);
        feature.put("value", value);
        feature.put("tone", tone);

        return feature;
    }

    // "All", "None" or the missing ones: "Not Session, Flash"
    static String count(Map<String, Boolean> values) {
        var missing = values.entrySet().stream()
                .filter(entry -> !entry.getValue())
                .map(Entry::getKey)
                .toList();

        if (missing.isEmpty()) {
            return "All";
        }

        return missing.size() == values.size() ? "None" : "Not " + String.join(", ", missing);
    }

    static String connector(String host, int port) {
        return StringUtils.isBlank(host) || port <= 0 ? "Off" : host + ":" + port;
    }

    static String duration(long seconds) {
        if (seconds > 0 && seconds % 3600 == 0) {
            return seconds / 3600 + " h";
        }
        if (seconds > 0 && seconds % 60 == 0) {
            return seconds / 60 + " min";
        }

        return seconds + " s";
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