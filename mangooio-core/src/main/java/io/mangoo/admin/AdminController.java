package io.mangoo.admin;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ocpsoft.prettytime.PrettyTime;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;

import io.mangoo.annotations.FilterWith;
import io.mangoo.cache.Cache;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Template;
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
    private static final String JOBS = "jobs";
    private static final String STATS = "stats";
    private static final String SPACE = "space";
    private static final String VERSION = "version";
    private static final int MB = 1024*1024;
    private final Map<String, String> properties = new HashMap<>();
    
    public AdminController() {
        System.getProperties().entrySet().forEach(
                entry -> this.properties.put(entry.getKey().toString(), entry.getValue().toString())
        );
    }
    
    public Response index(String space) {
        if (StringUtils.isBlank(space)) {
            return dashboard();
        } else if (("routes").equals(space)) {
            return routes(space);
        } else if (("cache").equals(space)) {
            return cache(space);
        }  else if (("metrics").equals(space)) {
            return metrics(space);
        } else if (("scheduler").equals(space)) {
            return scheduler(space);
        }
        
        return Response.withNotFound().andEmptyBody();
    }
    
    private Response dashboard() {
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
    
    private Response routes(String space) {
        return Response.withOk()
                .andContent(SPACE, space)
                .andContent(VERSION, BootstrapUtils.getVersion())
                .andContent("routes", Router.getRoutes())
                .andTemplate(Template.DEFAULT.routesPath());
    }

    private Response cache(String space) {
        Map<String, Object> stats = Application.getInstance(Cache.class).getStats();

        return Response.withOk()
                .andContent(SPACE, space)
                .andContent(VERSION, BootstrapUtils.getVersion())
                .andContent(STATS, stats)
                .andTemplate(Template.DEFAULT.cachePath());
    }

    private Response metrics(String space) {
        Metrics metrics = Application.getInstance(Metrics.class);

        return Response.withOk()
                .andContent(SPACE, space)
                .andContent(VERSION, BootstrapUtils.getVersion())
                .andContent("metrics", metrics.getMetrics())
                .andTemplate(Template.DEFAULT.metricsPath());
    }

    private Response scheduler(String space)  {
        List<Job> jobs = new ArrayList<>();
        try {
            org.quartz.Scheduler scheduler = Application.getInstance(Scheduler.class).getScheduler();
            if (scheduler != null) {
                Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(Default.SCHEDULER_JOB_GROUP.toString()));
                for (JobKey jobKey : jobKeys) {
                    List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                    Trigger trigger = triggers.get(0);
                    TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    jobs.add(new Job(TriggerState.PAUSED.equals(triggerState) ? false : true, jobKey.getName(), trigger.getDescription(), trigger.getNextFireTime(), trigger.getPreviousFireTime()));
                }
            }
 
        } catch (SchedulerException e) {
            LOG.error("Failed to retrieve jobs from scheduler", e);
        }

        return Response.withOk()
                .andContent(SPACE, space)
                .andContent(VERSION, BootstrapUtils.getVersion())
                .andContent(JOBS, jobs)
                .andTemplate(Template.DEFAULT.schedulerPath());
    }
}