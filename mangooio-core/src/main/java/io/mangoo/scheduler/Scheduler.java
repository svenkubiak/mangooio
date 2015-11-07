package io.mangoo.scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;

/**
 * Convenient class for interacting with the quartz scheduler
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class Scheduler {
    private static final Logger LOG = LogManager.getLogger(Scheduler.class);
    private org.quartz.Scheduler quartzScheduler;

    @Inject
    public Scheduler(Config config) {
        Preconditions.checkNotNull(config, "config can not be null");

        for (Map.Entry<String, String> entry : config.getAllConfigurations().entrySet()) {
            if (entry.getKey().startsWith(Default.SCHEDULER_PREFIX.toString())) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        }
    }
    
    /**
     * Returns the current scheduler instance
     * 
     * @return Scheduler instance, null if scheduler is not initialize or started
     */
    public org.quartz.Scheduler getScheduler() {
        return this.quartzScheduler;
    }

    public void start() {
        initialize();
        try {
            this.quartzScheduler.start();
            if (this.quartzScheduler.isStarted()) {
                LOG.info("Successfully started quartz scheduler");
            } else {
                LOG.error("Scheduler is not started");
            }
        } catch (SchedulerException e) {
            LOG.error("Failed to start scheduler", e);
        }
    }

    public void shutdown() {
        Preconditions.checkNotNull(this.quartzScheduler, "Scheduler is not initialized or started");
        
        try {
            this.quartzScheduler.shutdown();
            if (this.quartzScheduler.isShutdown()) {
                LOG.info("Successfully shutdown quartz scheduler");
            } else {
                LOG.error("Failed to shutdown scheduler");
            }
        } catch (SchedulerException e) {
            LOG.error("Failed to shutdown scheduler", e);
        }
    }

    public void standby() {
        Preconditions.checkNotNull(this.quartzScheduler, "Scheduler is not initialized or started");
        
        try {
            this.quartzScheduler.standby();
            if (this.quartzScheduler.isInStandbyMode()) {
                LOG.info("Scheduler is now in standby");
            } else {
                LOG.error("Failed to put scheduler in standby");
            }
        } catch (SchedulerException e) {
            LOG.error("Failed to put scheduler in standby", e);
        }
    }

    /**
     * Prepares the scheduler for being started by creating a 
     * scheduler instance from quartz scheduler factory
     */
    private void initialize() {
        if (this.quartzScheduler == null) {
            try {
                this.quartzScheduler = new StdSchedulerFactory().getScheduler();
                this.quartzScheduler.setJobFactory(Application.getInstance(MangooJobFactory.class));                
            } catch (SchedulerException e) {
                LOG.error("Failed to initialize scheduler", e);    
            }
        }
    }
    
    /**
     * Adds a new job with a given JobDetail and Trigger to the scheduler
     *
     * @param jobDetail The JobDetail for the Job
     * @param trigger The Trigger for the job
     */
    public void schedule(JobDetail jobDetail, Trigger trigger) {
        Preconditions.checkNotNull(jobDetail, "JobDetail is required for schedule");
        Preconditions.checkNotNull(trigger, "trigger is required for schedule");
        initialize();
        
        try {
            this.quartzScheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            LOG.error("Failed to schedule a new job", e);
        }
    }
    
    /**
     * Creates a new quartz scheduler Trigger, which can be used to
     * schedule a new job by passing it into {@link #schedule(JobDetail, Trigger) schedule}
     * 
     * @param identity The name of the trigger
     * @param groupName The trigger group name
     * @param description The trigger description
     * @param cron The cron expression for the trigger
     * 
     * @return A new Trigger object
     */
    public Trigger createTrigger(String identity, String groupName, String description, String cron) {
        Preconditions.checkNotNull(identity, "Identity is required for creating a new trigger");
        Preconditions.checkNotNull(groupName, "groupName is required for new trigger");
        Preconditions.checkNotNull(cron, "cron is required for new trigger");
        Preconditions.checkArgument(CronExpression.isValidExpression(cron), "cron expression is invalid");

        return newTrigger()
                .withIdentity(identity, groupName)
                .withSchedule(cronSchedule(cron))
                .withDescription(description)
                .build();
    }
    
    /**
     * Creates a new quartz scheduler JobDetail, which can be used to
     * schedule a new job by passing it into {@link #schedule(JobDetail, Trigger) schedule}
     *
     * @param identity The name of the job
     * @param groupName The name of the job Group
     * @param clazz The class where the actual execution takes place
     * @param <T> JavaDoc requires this (just ignore it)
     *
     * @return A new JobDetail object
     */
    public <T extends Job> JobDetail createJobDetail(String identity, String groupName, Class<? extends Job> clazz) {
        Preconditions.checkNotNull(identity, "identity is required for new JobDetail");
        Preconditions.checkNotNull(groupName, "groupName is required for new JobDetail");
        Preconditions.checkNotNull(clazz, "clazz is required for new JobDetail");
        
        return newJob(clazz)
                .withIdentity(identity, groupName)
                .build();
    }
}