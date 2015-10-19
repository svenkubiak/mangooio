package io.mangoo.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheStats;
import com.google.inject.Inject;

import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Key;
import io.mangoo.enums.Template;
import io.mangoo.models.Job;
import io.mangoo.models.Metrics;
import io.mangoo.routing.Response;
import io.mangoo.routing.Router;
import io.mangoo.scheduler.MangooScheduler;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooAdminController {
    private static final Logger LOG = LoggerFactory.getLogger(MangooAdminController.class);
    private Config config;
    private MangooScheduler mangooScheduler;
    private Cache cache;

    @Inject
    public MangooAdminController(Config config, Cache cache, MangooScheduler mangooScheduler) {
        this.config = Objects.requireNonNull(config, "config is required for mangooAdminController");
        this.cache = Objects.requireNonNull(cache, "cache is required for mangooAdminController");
        this.mangooScheduler = Objects.requireNonNull(mangooScheduler, "mangooScheduler is required for mangooAdminController");
    }

    public Response health() {
        if (!Application.inDevMode() && !config.isAdminHealthEnabled()) {
            return notFound();
        }

        return Response.withOk()
                .andTextBody("alive");
    }

    public Response routes() {
        if (!Application.inDevMode() && !config.isAdminRoutesEnabled()) {
            return notFound();
        }

        return Response.withOk()
                .andContent("routes", Router.getRoutes())
                .andTemplate("defaults/routes.ftl");
    }

    public Response cache() {
        if (!Application.inDevMode() && !config.isAdminCacheEnabled()) {
            return notFound();
        }

        CacheStats cacheStats = cache.getStats();

        Map<String, Object> stats = new HashMap<String, Object>();
        stats.put("Average load penalty", cacheStats.averageLoadPenalty());
        stats.put("Eviction count", cacheStats.evictionCount());
        stats.put("Hit count", cacheStats.hitCount());
        stats.put("Hit rate", cacheStats.hitRate());
        stats.put("Load count", cacheStats.loadCount());
        stats.put("Load exception count", cacheStats.loadExceptionCount());
        stats.put("Load exception rate", cacheStats.loadExceptionRate());
        stats.put("Load success rate", cacheStats.loadSuccessCount());
        stats.put("Miss count", cacheStats.missCount());
        stats.put("Request count", cacheStats.requestCount());
        stats.put("Total load time in ns", cacheStats.totalLoadTime());

        return Response.withOk()
                .andContent("stats", stats)
                .andTemplate("defaults/cache.ftl");
    }

    public Response config() {
        if (!Application.inDevMode() && !config.isAdminConfigEnabled()) {
            return notFound();
        }

        Map<String, String> configurations = config.getAllConfigurations();
        configurations.remove(Key.APPLICATION_SECRET.toString());

        return Response.withOk()
                .andContent("configuration", configurations)
                .andTemplate("defaults/config.ftl");
    }

    public Response metrics() {
        if (!Application.inDevMode() && !config.isAdminConfigEnabled()) {
            return notFound();
        }

        Metrics metrics = Application.getInjector().getInstance(Metrics.class);
        return Response.withOk()
                .andContent("metrics", metrics.getMetrics())
                .andTemplate("defaults/metrics.ftl");
    }
    
    @SuppressWarnings("unchecked")
    public Response scheduler() {
        if (!Application.inDevMode() && !config.isAdminSchedulerEnabled()) {
            return notFound();
        }

        Scheduler scheduler = this.mangooScheduler.getScheduler();
        List<Job> jobs = new ArrayList<Job>();
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Default.SCHEDULER_JOB_GROUP.toString()));
            for (JobKey jobKey : jobKeys) {
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                Trigger trigger = triggers.get(0);  
                TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                jobs.add(new Job((TriggerState.PAUSED.equals(triggerState)) ? false : true, jobKey.getName(), trigger.getDescription(), trigger.getNextFireTime(), trigger.getPreviousFireTime()));
            }
        } catch (SchedulerException e) {
            LOG.error("Failed to get jobs from scheduler", e);
        }
        
        return Response.withOk()
                .andContent("jobs", jobs)
                .andTemplate("defaults/scheduler.ftl");
    }

    private Response notFound() {
        return Response.withNotFound().andBody(Template.DEFAULT.notFound());
    }
}