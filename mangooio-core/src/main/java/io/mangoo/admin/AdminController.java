package io.mangoo.admin;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.LongAdder;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ocpsoft.prettytime.PrettyTime;

import com.google.inject.Inject;

import io.mangoo.annotations.FilterWith;
import io.mangoo.cache.Cache;
import io.mangoo.core.Application;
import io.mangoo.enums.Template;
import io.mangoo.exceptions.MangooSchedulerException;
import io.mangoo.models.Job;
import io.mangoo.models.Metrics;
import io.mangoo.routing.Response;
import io.mangoo.routing.Router;
import io.mangoo.scheduler.Scheduler;
import io.mangoo.utils.BootstrapUtils;

/**
 * Controller class for administrative URLs
 *
 * @author svenkubiak
 *
 */
@FilterWith(AdminFilter.class)
public class AdminController {
    private static final Logger LOG = LogManager.getLogger(AdminController.class);
    private static final String SCHEDULER = "scheduler";
    private static final String METRICS = "metrics"; //NOSONAR
    private static final String CACHE = "cache";  //NOSONAR
    private static final String ROUTES = "routes"; //NOSONAR
    private static final String JOBS = "jobs";
    private static final String STATS = "stats";
    private static final String SPACE = "space";
    private static final String VERSION = "version";
    private static final int MB = 1024*1024;
    private final Map<String, String> properties = new HashMap<>();
    private final Scheduler scheduler; //NOSONAR
    
    @Inject
    public AdminController(Scheduler scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler can not be null");
        
        System.getProperties().entrySet().forEach(
                entry -> this.properties.put(entry.getKey().toString(), entry.getValue().toString())
        );
    }
    
    public Response index() {
        Runtime runtime = Runtime.getRuntime();
        double maxMemory = runtime.maxMemory() / MB;
        
        Instant instant = Application.getStart().atZone(ZoneId.systemDefault()).toInstant();
        PrettyTime prettyTime = new PrettyTime(Locale.ENGLISH);
        
        return Response.withOk()
                .andContent(VERSION, BootstrapUtils.getVersion())
                .andContent(SPACE, null)
                .andContent("uptime", prettyTime.format(Date.from(instant)))
                .andContent("started", Application.getStart())
                .andContent("properties", this.properties)
                .andContent("maxMemory", maxMemory)
                .andTemplate(Template.DEFAULT.adminPath());
    }
    
    public Response execute(String name) {
        try {
            this.scheduler.executeJob(name);
        } catch (MangooSchedulerException e) {
            LOG.error("Failed to execute job with name: " + name, e);
        }
        
        return Response.withRedirect("/@admin/scheduler");
    }
    
    public Response state(String name) {
        try {
            this.scheduler.changeState(name);
        } catch (MangooSchedulerException e) {
            LOG.error("Failed to change the state of job with name: " + name, e);
        }
        
        return Response.withRedirect("/@admin/scheduler");
    }
    
    public Response routes() {
        return Response.withOk()
                .andContent(SPACE, ROUTES)
                .andContent(VERSION, BootstrapUtils.getVersion())
                .andContent(ROUTES, Router.getRoutes())
                .andTemplate(Template.DEFAULT.routesPath());
    }

    public Response cache() {
        return Response.withOk()
                .andContent(SPACE, CACHE)
                .andContent(VERSION, BootstrapUtils.getVersion())
                .andContent(STATS, Application.getInstance(Cache.class).getStats())
                .andTemplate(Template.DEFAULT.cachePath());
    }

    public Response metrics() {
        Metrics metrics = Application.getInstance(Metrics.class);
        long totalRequests = 0;
        long errorRequests = 0;
        double errorRate = 0;
        
        for (Entry<Integer, LongAdder> entry :  metrics.getMetrics().entrySet()) {
            if (String.valueOf(entry.getKey()).charAt(0) == '5') {
                errorRequests = errorRequests + entry.getValue().longValue();
            }
            totalRequests = totalRequests + entry.getValue().longValue();
        }
        
        if (errorRequests > 0) {
            errorRate = totalRequests / errorRequests;
        }

        return Response.withOk()
                .andContent(SPACE, METRICS)
                .andContent(VERSION, BootstrapUtils.getVersion())
                .andContent(METRICS, metrics.getMetrics())
                .andContent("totalRequests", totalRequests)
                .andContent("minRequestTime", metrics.getMinRequestTime())
                .andContent("avgRequestTime", metrics.getAvgRequestTime())
                .andContent("maxRequestTime", metrics.getMaxRequestTime())
                .andContent("errorRate", errorRate)
                .andTemplate(Template.DEFAULT.metricsPath());
    }

    public Response scheduler()  {
        List<Job> jobs = new ArrayList<>();
        try {
            jobs = this.scheduler.getAllJobs();
        } catch (MangooSchedulerException e) {
            LOG.error("Failed to retrieve jobs from scheduler", e);
        }

        return Response.withOk()
                .andContent(SPACE, SCHEDULER)
                .andContent(VERSION, BootstrapUtils.getVersion())
                .andContent(JOBS, jobs)
                .andTemplate(Template.DEFAULT.schedulerPath());
    }
}