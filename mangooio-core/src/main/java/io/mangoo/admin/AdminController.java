package io.mangoo.admin;

import java.lang.management.ManagementFactory;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import com.google.common.cache.CacheStats;
import com.google.inject.Inject;
import com.google.re2j.Pattern;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.mangoo.annotations.FilterWith;
import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheImpl;
import io.mangoo.cache.CacheProvider;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.CacheName;
import io.mangoo.enums.Default;
import io.mangoo.enums.HmacShaAlgorithm;
import io.mangoo.enums.Key;
import io.mangoo.enums.Required;
import io.mangoo.enums.Template;
import io.mangoo.exceptions.MangooEncryptionException;
import io.mangoo.exceptions.MangooTokenException;
import io.mangoo.models.Metrics;
import io.mangoo.routing.Response;
import io.mangoo.routing.Router;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.routes.FileRoute;
import io.mangoo.routing.routes.PathRoute;
import io.mangoo.routing.routes.RequestRoute;
import io.mangoo.routing.routes.ServerSentEventRoute;
import io.mangoo.services.EventBusService;
import io.mangoo.utils.MangooUtils;
import io.mangoo.utils.token.TokenBuilder;
import io.mangoo.utils.totp.TotpUtils;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;
import net.minidev.json.JSONObject;

public class AdminController {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(AdminController.class);
    private static final Pattern PATTERN = Pattern.compile("[^a-zA-Z0-9]");
    private static final String ENABLED = "enabled";
    private static final String ADMIN_INDEX = "/@admin";
    private static final String MANGOOIO_ADMIN_LOCK_COUNT = "mangooio-admin-lock-count";
    private static final String MANGOOIO_ADMIN_LOCKED_UNTIL = "mangooio-admin-locked-until";
    private static final String PERIOD = "30";
    private static final String DIGITS = "6";
    private static final String URL = "url";
    private static final String METHOD = "method";
    private static final String CACHE_ADMINROUTES = "cache_adminroutes";
    private static final String METRICS = "metrics";
    private static final String ROUTES = "routes";
    private static final double HUNDRED_PERCENT = 100.0;
    private static final int ADMIN_LOGIN_MAX_RETRIES = 10;
    private static final long  MEGABYTE = 1048576L;
    private final Cache cache;
    private final CacheProvider cacheProvider;
    private final Config config;
    private final Crypto crypto;
    
    @Inject
    public AdminController(Crypto crypto, Config config, Cache cache, CacheProvider cacheProvider) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
        this.crypto = Objects.requireNonNull(crypto, Required.CRYPTO.toString());
        this.cache = cacheProvider.getCache(CacheName.APPLICATION);
        this.cacheProvider = Objects.requireNonNull(cacheProvider, Required.CACHE_PROVIDER.toString());
    }

    @FilterWith(AdminFilter.class)
    public Response index() {
        var instant = Application.getStart().atZone(ZoneId.systemDefault()).toInstant();
        boolean enabled = config.isMetricsEnable();
        var eventBusService = Application.getInstance(EventBusService.class);
        
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
            
            return Response.withOk()
                    .andContent(ENABLED, Boolean.TRUE)
                    .andContent(METRICS, metrics.getResponseMetrics())
                    .andContent("uptime", Date.from(instant))
                    .andContent("warnings", cache.get(Key.MANGOOIO_WARNINGS.toString()))
                    .andContent("dataSend", MangooUtils.readableFileSize(metrics.getDataSend()))
                    .andContent("totalRequests", totalRequests)
                    .andContent("minRequestTime", metrics.getMinRequestTime())
                    .andContent("avgRequestTime", metrics.getAvgRequestTime())
                    .andContent("maxRequestTime", metrics.getMaxRequestTime())
                    .andContent("errorRate", errorRate)
                    .andContent("events", eventBusService.getNumEvents())
                    .andContent("listeners", eventBusService.getNumListeners())
                    .andTemplate(Template.DEFAULT.adminPath());
        }
        
        return Response.withOk()
                .andContent(ENABLED, Boolean.FALSE)
                .andContent("uptime", Date.from(instant))
                .andContent("events", eventBusService.getNumEvents())
                .andContent("listeners", eventBusService.getNumListeners())
                .andContent("warnings", cache.get(Key.MANGOOIO_WARNINGS.toString()))
                .andTemplate(Template.DEFAULT.adminPath());
    }
    
    @FilterWith(AdminFilter.class)
    public Response logger() {
        var loggerContext = (LoggerContext) LogManager.getContext(false);
        
        return Response.withOk()
                .andContent("loggers", loggerContext.getLoggers())
                .andTemplate(Template.DEFAULT.loggerPath());
    }
    
    @FilterWith(AdminFilter.class)
    public Response cache() {
        Map<String, CacheStats> statistics = new HashMap<>();
        for (Entry<String, Cache> entry : cacheProvider.getCaches().entrySet()) {
            statistics.put(entry.getKey(), ((CacheImpl) entry.getValue()).getStats()); //NOSONAR
        }
        
        return Response.withOk()
                .andContent("statistics", statistics)
                .andTemplate(Template.DEFAULT.cachePath());
    }
    
    public Response login() {
        return Response.withOk()
                .andTemplate(Template.DEFAULT.loginPath());
    }
    
    public Response logout() {
        Cookie cookie = new CookieImpl(Default.ADMIN_COOKIE_NAME.toString())
                .setValue("")
                .setHttpOnly(true)
                .setSecure(Application.inProdMode())
                .setPath("/")
                .setDiscard(true)
                .setExpires(new Date())
                .setSameSite(true)
                .setSameSiteMode("Strict");
        
        return Response.withRedirect(ADMIN_INDEX).andCookie(cookie);
    }
    
    public Response authenticate(Form form) {
        form.expectValue("username");
        form.expectValue("password");
        
        if (isNotLocked() && form.isValid()) {
            if (isValidAuthentication(form)) {
                cache.resetCounter(MANGOOIO_ADMIN_LOCK_COUNT);
                return Response.withRedirect(ADMIN_INDEX).andCookie(getAdminCookie(true));
            } else {
                invalidAuthentication();
            }
        }
        form.invalidate();
        form.keep();
        
        return Response.withRedirect("/@admin/login");
    }

    public Response verify(Form form) {
        form.expectValue("code");
        
        if (isNotLocked() && form.isValid()) {
            if (TotpUtils.verifiedTotp(config.getApplicationAdminSecret(), form.get("code"))) {
                return Response.withRedirect(ADMIN_INDEX).andCookie(getAdminCookie(false));
            } else {
                invalidAuthentication();
            }
        }
        form.invalidate();
        form.keep();
        
        return Response.withRedirect("/@admin/twofactor");
    }
    
    public Response twofactor() {
        return Response.withOk()
                .andTemplate(Template.DEFAULT.twofactorPath());
    }
    
    @FilterWith(AdminFilter.class)
    public Response loggerajax(Request request) {
        Map<String, Object> body = request.getBodyAsJsonMap();
        if (body != null && body.size() > 0) {
            var clazz = body.get("class").toString();
            var level = body.get("level").toString();
            if (StringUtils.isNotBlank(clazz) && StringUtils.isNotBlank(level)) {
                LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
                loggerContext.getLoggers()
                    .stream()
                    .filter(logger -> clazz.equals(logger.getName()))
                    .forEach(logger -> logger.setLevel(Level.getLevel(level)));
            }
        }
        
        return Response.withOk()
                .andEmptyBody();
    }
    
    @FilterWith(AdminFilter.class)
    @SuppressFBWarnings(value = "CE_CLASS_ENVY", justification = "JSONObject creation as intended")
    public Response routes() {
        List<JSONObject> routes = getRoutes();
        
        if (routes.isEmpty()) {
            Router.getFileRoutes().forEach((FileRoute route) -> {
                JSONObject json = new JSONObject();
                json.put(METHOD, "FILE");
                json.put(URL, route.getUrl());
                routes.add(json);
            });
            
            Router.getPathRoutes().forEach((PathRoute route) -> {
                JSONObject json = new JSONObject();
                json.put(METHOD, "PATH");
                json.put(URL, route.getUrl());
                routes.add(json);
            });
            
            Router.getServerSentEventRoutes().forEach((ServerSentEventRoute route) -> {
                JSONObject json = new JSONObject();
                json.put(METHOD, "SSE");
                json.put(URL, route.getUrl());
                routes.add(json);
            });
            
            Router.getRequestRoutes().filter((RequestRoute route) -> !route.getUrl().contains("@admin"))
                    .forEach((RequestRoute route) -> {
                        JSONObject json = new JSONObject();
                        json.put(METHOD, route.getMethod());
                        json.put(URL, route.getUrl());
                        json.put("controllerClass", route.getControllerClass());
                        json.put("controllerMethod", route.getControllerMethod());
                        json.put("basicAuthentication", route.hasBasicAuthentication());
                        json.put("authentication", route.hasAuthentication());
                        json.put("blocking", route.isBlocking());
                        routes.add(json);
                    });
            
            cache.put(CACHE_ADMINROUTES, routes);
        }

        return Response.withOk()
                .andContent(ROUTES, routes)
                .andTemplate(Template.DEFAULT.routesPath());
    }
    
    @FilterWith(AdminFilter.class)
    public Response tools() {
        String secret = config.getApplicationAdminSecret();
        String qrCode = null;
        
        if (StringUtils.isBlank(secret)) {
            secret = TotpUtils.createSecret();
            qrCode = TotpUtils.getQRCode("mangoo_IO_Admin", PATTERN.matcher(config.getApplicationName()).replaceAll(""), secret, HmacShaAlgorithm.HMAC_SHA_512, DIGITS, PERIOD);
        }
        
        return Response.withOk()
                .andContent("qrcode", qrCode)
                .andContent("secret", secret)
                .andTemplate(Template.DEFAULT.toolsPath());
    }

    @FilterWith(AdminFilter.class)
    public Response toolsajax(Request request) {
        Map<String, Object> body = request.getBodyAsJsonMap();
        var value = "";
        
        if (body != null && body.size() > 0) {
            var function = body.get("function").toString();

            if (("keypair").equalsIgnoreCase(function)) {
                var keyPair = crypto.generateKeyPair();
                var publickey = crypto.getKeyAsString(keyPair.getPublic());
                var privatekey = crypto.getKeyAsString(keyPair.getPrivate());
                
                value = "{\"publickey\" : \"" + publickey + "\", \"privatekey\" : \"" + privatekey + "\"}";
            } else if (("encrypt").equalsIgnoreCase(function)) {
                var cleartext = body.get("cleartext").toString();
                var key = body.get("key").toString();
                
                try {
                    var publicKey = crypto.getPublicKeyFromString(key);
                    value = crypto.encrypt(cleartext, publicKey);
                } catch (MangooEncryptionException e) {
                    LOG.error("Failed to encrypt cleartext.", e);
                }
            } else {
                LOG.warn("Invalid or no function selected for AJAX request.");
            }
        }
        
        return Response.withOk().andJsonBody(value);
    }
    
    public Response health(Request request)  {
        if (isValidHeaderToken(request)) {
            JSONObject json = new JSONObject();
            json.put("cpu", getCpu());

            Map<String, Double> memory = getMemory();
            json.putAll(memory);
            
            return Response.withOk().andJsonBody(json);
        }
        
        return Response.withNotFound().andEmptyBody();
    }
    
    private boolean isValidHeaderToken(Request request) {
        var valid = false;
        String token = config.getApplicationAdminHealthToken();
        String header = request.getHeader(Default.APPLICATION_ADMIN_HEALTH_HEADER.toString());
        
        if (StringUtils.isNotBlank(token) && StringUtils.isNotBlank(header) && token.equals(header)) {
            valid = true;
        }
        
        return valid;
    }

    private List<JSONObject> getRoutes() {
        List<JSONObject> routes = cache.get(CACHE_ADMINROUTES);
        
        return (routes == null) ? new ArrayList<>() : routes;
    }
    
    private boolean isValidAuthentication(Form form) {
        var valid = false;

        String username = config.getApplicationAdminUsername();
        String password = config.getApplicationAdminPassword();
        
        if (checkAuthentication(form, username, password)) {
            valid = true;
        }
        
        return valid;
    }

    private boolean checkAuthentication(Form form, String username, String password) {
        return StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password) &&
               username.equals(form.get("username")) && password.equals(form.get("password"));
    }

    private Cookie getAdminCookie(boolean includeTwoFactor) {
        TokenBuilder tokenBuilder = TokenBuilder.create()
                .withSharedSecret(config.getApplicationSecret())
                .withExpires(LocalDateTime.now().plusMinutes(30))
                .withClaim("uuid", MangooUtils.randomString(32));
        
        if (includeTwoFactor && StringUtils.isNotBlank(config.getApplicationAdminSecret())) {
            tokenBuilder.withClaim("twofactor", Boolean.TRUE);
        }
        
        String token = "";
        try {
            token = tokenBuilder.build();
        } catch (MangooTokenException e) {
            LOG.error("Failed to create admin cookie", e);
        }
    
        return new CookieImpl(Default.ADMIN_COOKIE_NAME.toString())
                .setValue(token)
                .setHttpOnly(true)
                .setSecure(Application.inProdMode())
                .setPath("/")
                .setSameSite(true)
                .setSameSiteMode("Strict");
    }
    
    private void invalidAuthentication() {
        AtomicInteger counter = cache.getAndIncrementCounter(MANGOOIO_ADMIN_LOCK_COUNT);
        if (counter.intValue() >= ADMIN_LOGIN_MAX_RETRIES) {
            cache.put(MANGOOIO_ADMIN_LOCKED_UNTIL, LocalDateTime.now().plusMinutes(60));
        }
        
        cache.put(MANGOOIO_ADMIN_LOCK_COUNT, counter);
    }
    
    private boolean isNotLocked() {
        LocalDateTime lockedUntil = cache.get(MANGOOIO_ADMIN_LOCKED_UNTIL);
        return lockedUntil == null || lockedUntil.isBefore(LocalDateTime.now());
    }
    
    private Map<String, Double> getMemory() {
        var memoryMXBean = ManagementFactory.getMemoryMXBean();
        return Map.of("initialMemory", (double) memoryMXBean.getHeapMemoryUsage().getInit() / MEGABYTE,
                      "usedMemory", (double) memoryMXBean.getHeapMemoryUsage().getUsed() / MEGABYTE,
                      "maxHeapMemory", (double) memoryMXBean.getHeapMemoryUsage().getMax() /MEGABYTE,
                      "committedMemory", (double) memoryMXBean.getHeapMemoryUsage().getCommitted() / MEGABYTE);
    }

    private Double getCpu() {
        try {
            var beanServer = ManagementFactory.getPlatformMBeanServer();
            var objectName = ObjectName.getInstance("java.lang:type=OperatingSystem");
            AttributeList list = beanServer.getAttributes(objectName, new String[]{"ProcessCpuLoad"});

            return Optional.ofNullable(list)
                    .map(l -> l.isEmpty() ? null : l)
                    .map(List::iterator)
                    .map(Iterator::next)
                    .map(Attribute.class::cast)
                    .map(Attribute::getValue)
                    .map(Double.class::cast)
                    .orElse(null);

        } catch (Exception e) {
            LOG.error("Failed to get process CPU load", e);
        }
        
        return (double) 0;
    }
}