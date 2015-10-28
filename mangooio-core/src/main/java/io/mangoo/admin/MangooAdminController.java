package io.mangoo.admin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;

import com.google.common.cache.CacheStats;

import io.mangoo.annotations.FilterWith;
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
 * Controller class for administrative URLs
 * 
 * @author svenkubiak
 *
 */
@FilterWith(MangooAdminFilter.class)
public class MangooAdminController {
    public Response health() {
        return Response.withOk()
                .andTextBody("alive");
    }

    public Response routes() {
        return Response.withOk()
                .andContent("routes", Router.getRoutes())
                .andTemplate(Template.DEFAULT.routesTemplate());
    }

    public Response cache() {
        CacheStats cacheStats = Application.getInstance(Cache.class).getStats();

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
                .andTemplate(Template.DEFAULT.cacheTemplate());
    }

    public Response config() {
        Map<String, String> configurations = Application.getInstance(Config.class).getAllConfigurations();
        configurations.remove(Key.APPLICATION_SECRET.toString());

        return Response.withOk()
                .andContent("configuration", configurations)
                .andTemplate(Template.DEFAULT.configTemplate());
    }

    public Response metrics() {
        Metrics metrics = Application.getInstance(Metrics.class);
        
        return Response.withOk()
                .andContent("metrics", metrics.getMetrics())
                .andTemplate(Template.DEFAULT.metricsTemplate());
    }
    
    public Response scheduler() throws SchedulerException {
        List<Job> jobs = new ArrayList<Job>();        
        Scheduler scheduler = Application.getInstance(MangooScheduler.class).getScheduler();
        if (scheduler != null) {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Default.SCHEDULER_JOB_GROUP.toString()));
            for (JobKey jobKey : jobKeys) {
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                Trigger trigger = triggers.get(0);  
                TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                jobs.add(new Job(TriggerState.PAUSED.equals(triggerState) ? false : true, jobKey.getName(), trigger.getDescription(), trigger.getNextFireTime(), trigger.getPreviousFireTime()));
            }
        }
        
        return Response.withOk()
                .andContent("jobs", jobs)
                .andTemplate(Template.DEFAULT.schedulerTemplate());
    }
}