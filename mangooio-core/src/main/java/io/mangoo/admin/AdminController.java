package io.mangoo.admin;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;

import com.google.inject.Inject;

import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.enums.CacheName;
import io.mangoo.enums.Key;
import io.mangoo.enums.Required;
import io.mangoo.enums.Template;
import io.mangoo.exceptions.MangooSchedulerException;
import io.mangoo.models.Job;
import io.mangoo.models.Metrics;
import io.mangoo.providers.CacheProvider;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import io.mangoo.scheduler.Scheduler;
import io.mangoo.utils.BootstrapUtils;
import io.mangoo.utils.MetricsUtils;

/**
 * Controller class for administrative area
 *
 * @author svenkubiak
 *
 */
public class AdminController {
    private static final org.apache.logging.log4j.Logger LOG = LogManager.getLogger(AdminController.class);
    private static final String JOBS = "jobs";
    private static final String LOGGER = "logger";
    private static final String METRICS = "metrics"; //NOSONAR
    private static final String ROUTES = "routes"; //NOSONAR
    private static final String SCHEDULER = "scheduler";    
    private static final String SPACE = "space";
    private static final String TOOLS = "tools";
    private static final String VERSION = "version";
    private static final String VERSION_TAG = BootstrapUtils.getVersion();
    private final Cache cache; //NOSONAR
    private final Config config; //NOSONAR
    private final Crypto crypto; //NOSONAR
    private final Scheduler scheduler; //NOSONAR
    
    @Inject
    public AdminController(Scheduler scheduler, Crypto crypto, Config config, CacheProvider cacheProvider) {
        this.config = Objects.requireNonNull(config, Required.CONFIG.toString());
        this.scheduler = Objects.requireNonNull(scheduler, Required.SCHEDULER.toString());
        this.crypto = Objects.requireNonNull(crypto, Required.CRYPTO.toString());
        this.cache = cacheProvider.getCache(CacheName.APPLICATION);
    }
    
    public Response execute(String name) {
        try {
            this.scheduler.executeJob(name);
        } catch (MangooSchedulerException e) {
            LOG.error("Failed to execute job with name: " + name, e);
        }
        
        return Response.withRedirect("/@admin/scheduler");
    }
    
    public Response index() {
        Runtime runtime = Runtime.getRuntime();
        long allocatedMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        Instant instant = Application.getStart().atZone(ZoneId.systemDefault()).toInstant();
        
        return Response.withOk()
                .andContent(VERSION, BootstrapUtils.getVersion())
                .andContent(SPACE, null)
                .andContent("uptime", Date.from(instant))
                .andContent("started", Application.getStart())
                .andContent("allocatedMemory", FileUtils.byteCountToDisplaySize(allocatedMemory))
                .andContent("freeMemory", FileUtils.byteCountToDisplaySize(freeMemory))
                .andContent("warnings", this.cache.get(Key.MANGOOIO_WARNINGS.toString()))
                .andTemplate(Template.DEFAULT.adminPath());
    }
    
    public Response logger() {
        LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
        return Response.withOk()
                .andContent(SPACE, LOGGER)
                .andContent(VERSION, VERSION_TAG)
                .andContent("loggers", loggerContext.getLoggers())
                .andTemplate(Template.DEFAULT.loggerPath());
    }
    
    public Response loggerajax(Request request) {
        Map<String, Object> body = request.getBodyAsJsonMap();
        if (body != null && body.size() > 0) {
            String clazz = body.get("class").toString();
            String level = body.get("level").toString();
            if (StringUtils.isNotBlank(clazz) && StringUtils.isNotBlank(level)) {
                LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
                for (Logger logger : loggerContext.getLoggers()) { //NOSONAR
                    if (clazz.equals(logger.getName())) {
                        logger.setLevel(Level.getLevel(level));
                        break;
                    }
                }
            }
        }
        
        return Response.withOk()
                .andEmptyBody();
    }
    
    public Response metrics() {
        boolean enabled = this.config.isMetricsEnable();
        if (enabled) {
            Metrics metrics = Application.getInstance(Metrics.class);
            long totalRequests = 0;
            long errorRequests = 0;
            double errorRate = 0;
            
            for (Entry<Integer, LongAdder> entry :  metrics.getResponseMetrics().entrySet()) {
                if (String.valueOf(entry.getKey()).charAt(0) == '5') {
                    errorRequests = errorRequests + entry.getValue().longValue();
                }
                totalRequests = totalRequests + entry.getValue().longValue();
            }
            
            if (errorRequests > 0) {
                errorRate = totalRequests / (double) errorRequests;
            }

            return Response.withOk()
                    .andContent(SPACE, METRICS)
                    .andContent(VERSION, VERSION_TAG)
                    .andContent(METRICS, metrics.getResponseMetrics())
                    .andContent("dataSend", MetricsUtils.readableFileSize(metrics.getDataSend()))
                    .andContent("totalRequests", totalRequests)
                    .andContent("minRequestTime", metrics.getMinRequestTime())
                    .andContent("avgRequestTime", metrics.getAvgRequestTime())
                    .andContent("maxRequestTime", metrics.getMaxRequestTime())
                    .andContent("errorRate", errorRate)
                    .andContent("enabled", enabled)
                    .andTemplate(Template.DEFAULT.metricsPath());
        }
        
        return Response.withOk()
                .andContent(SPACE, METRICS)
                .andContent(VERSION, VERSION_TAG)
                .andContent("enabled", enabled)
                .andTemplate(Template.DEFAULT.metricsPath());
    }
    
    public Response routes() {
        //FIX ME
        //Set<MangooRoute> routes = Router.getRoutes();
        
        return Response.withOk()
                .andContent(SPACE, ROUTES)
                .andContent(VERSION, VERSION_TAG)
                .andContent(ROUTES, new ArrayList<>())
                .andTemplate(Template.DEFAULT.routesPath());
    }
    
    public Response resetMetrics() {
        Application.getInstance(Metrics.class).reset();
        return Response.withRedirect("/@admin/metrics");
    }
    
    public Response scheduler()  {
        List<Job> jobs = new ArrayList<>();
        if (this.scheduler.isInitialize()) {
            try {
                jobs = this.scheduler.getAllJobs();
            } catch (MangooSchedulerException e) {
                LOG.error("Failed to retrieve jobs from scheduler", e);
            }   
        }

        return Response.withOk()
                .andContent(SPACE, SCHEDULER)
                .andContent(VERSION, VERSION_TAG)
                .andContent(JOBS, jobs)
                .andTemplate(Template.DEFAULT.schedulerPath());
    }
    
    public Response state(String name) {
        try {
            this.scheduler.changeState(name);
        } catch (MangooSchedulerException e) {
            LOG.error("Failed to change the state of job with name: " + name, e);
        }
        
        return Response.withRedirect("/@admin/scheduler");
    }

    public Response health() {
        if (this.config.isMetricsEnable()) {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long allocatedMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            
            Metrics metrics = Application.getInstance(Metrics.class);
            long totalRequests = 0;
            long errorRequests = 0;
            double errorRate = 0;
            
            for (Entry<Integer, LongAdder> entry :  metrics.getResponseMetrics().entrySet()) {
                if (String.valueOf(entry.getKey()).charAt(0) == '5') {
                    errorRequests = errorRequests + entry.getValue().longValue();
                }
                totalRequests = totalRequests + entry.getValue().longValue();
            }
            
            if (errorRequests > 0) {
                errorRate = totalRequests / (double) errorRequests;
            }
            
            Map<String, Object> json = new HashMap<>();
            json.put("started", Application.getStart().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            json.put("uptime in seconds", Application.getUptime().getSeconds());
            json.put(VERSION, VERSION_TAG);
            json.put("maxMemory", FileUtils.byteCountToDisplaySize(maxMemory));
            json.put("allocatedMemory", FileUtils.byteCountToDisplaySize(allocatedMemory));
            json.put("freeMemory", FileUtils.byteCountToDisplaySize(freeMemory));
            json.put("totalFreeMemory", FileUtils.byteCountToDisplaySize(freeMemory + (maxMemory - allocatedMemory)));
            json.put(METRICS, metrics);
            json.put("totalRequests", totalRequests);
            json.put("errorRate", errorRate);
            
            return Response.withOk().andJsonBody(json);
        }
        
        return Response.withNotFound();
    }
    
    public Response tools() {
        return Response.withOk()
                .andContent(SPACE, TOOLS)
                .andContent(VERSION, VERSION_TAG)
                .andTemplate(Template.DEFAULT.toolsPath());
    }

    public Response toolsajax(Request request) {
        Map<String, Object> body = request.getBodyAsJsonMap();
        String value = "";
        
        if (body != null && body.size() > 0) {
            String function = body.get("function").toString();

            if (("keypair").equalsIgnoreCase(function)) {
                String publickey = null;
                String privatekey = null;
                
                try {
                    KeyPair keyPair = this.crypto.generateKeyPair();
                    publickey = this.crypto.getKeyAsString(keyPair.getPublic());
                    privatekey = this.crypto.getKeyAsString(keyPair.getPrivate());
                } catch (NoSuchAlgorithmException e) {
                    LOG.error("Failed to create public/private key pair.", e);
                }
                
                value = "{\"publickey\" : \"" + publickey + "\", \"privatekey\" : \"" + privatekey + "\"}";
            } else if (("encrypt").equalsIgnoreCase(function)) {
                String cleartext = body.get("cleartext").toString();
                String key = body.get("key").toString();
                try {
                    PublicKey publicKey = this.crypto.getPublicKeyFromString(key);
                    value = this.crypto.encrypt(cleartext, publicKey);
                } catch (Exception e) {
                    LOG.error("Failed to encrypt cleartext.", e);
                }

            } else {
                LOG.warn("Invalid or no function selected for AJAX request. Either choose 'hash' order 'encrypt'.");
            }
        }
        
        return Response.withOk()
               .andJsonBody(value);
    }
}