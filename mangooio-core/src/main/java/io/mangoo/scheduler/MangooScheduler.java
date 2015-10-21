package io.mangoo.scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Map;
import java.util.Objects;

import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.enums.Default;

/**
 * Convenient class for interacting with quartz scheduler
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class MangooScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(MangooScheduler.class);
    private Scheduler scheduler;
    private MangooJobFactory mangooJobFactory;

    @Inject
    public MangooScheduler(MangooJobFactory mangooJobFactory, Config config) {
        this.mangooJobFactory = Objects.requireNonNull(mangooJobFactory, "quartzJobFactory can not be null");
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
     * @return Scheduler instance, null if no jobs are scheduled or scheduler is not started
     */
    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public void start() {
        initialize();
        try {
            this.scheduler.start();
            if (this.scheduler.isStarted()) {
                LOG.info("Successfully started quartz scheduler");
            } else {
                LOG.error("Scheduler is not started");
            }
        } catch (SchedulerException e) {
            LOG.error("Failed to start scheduler", e);
        }
    }

    public void shutdown() {
        try {
            this.scheduler.shutdown();
            if (this.scheduler.isShutdown()) {
                LOG.info("Successfully shutdown quartz scheduler");
            } else {
                LOG.error("Failed to shutdown scheduler");
            }
        } catch (SchedulerException e) {
            LOG.error("Failed to shutdown scheduler", e);
        }
    }

    public void standby() {
        try {
            this.scheduler.standby();
            if (this.scheduler.isInStandbyMode()) {
                LOG.info("Scheduler is now in standby");
            } else {
                LOG.error("Failed to put scheduler in standby");
            }
        } catch (SchedulerException e) {
            LOG.error("Failed to put scheduler in standby", e);
        }
    }

    /**
     * Prepares the scheduler for being starting by create a 
     * scheduler instance from scheduler factory
     */
    private void initialize() {
        if (this.scheduler == null) {
            SchedulerFactory schedulerFactory = new StdSchedulerFactory();
            try {
                this.scheduler = schedulerFactory.getScheduler();
                this.scheduler.setJobFactory(this.mangooJobFactory);                
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
            this.scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            LOG.error("Failed to schedule a new job", e);
        }
    }
    
    /**
     * Creates a new Trigger
     *
     * @param identity The name of the job
     * @param cronExpression The cron expression for executing the job
     * @param triggerGroupName The group name to store the job
     * @param triggerDescription The trigger description for the job
     *
     * @return A new trigger object
     */
    public Trigger getTrigger(String identity, String cronExpression, String triggerGroupName, String triggerDescription) {
        Preconditions.checkNotNull(identity, "Identity is required for creating a new trigger");
        Preconditions.checkNotNull(cronExpression, "CronExpression is required for new trigger");
        Preconditions.checkNotNull(triggerGroupName, "TriggerGroupName is required for new trigger");

        return newTrigger()
                .withIdentity(identity, triggerGroupName)
                .withSchedule(cronSchedule(cronExpression))
                .withDescription(triggerDescription)
                .build();
    }

    /**
     * Creates a new JobDetail
     *
     * @param clazz The class where the actual execution takes place
     * @param identity The name of the job
     * @param jobGroupName The name of the job Group
     *
     * @return A new JobDetail object
     */
    public <T extends Job> JobDetail getJobDetail(Class<? extends Job> clazz, String identity, String jobGroupName) {
        Preconditions.checkNotNull(clazz, "Class is required for new JobDetail");
        Preconditions.checkNotNull(identity, "Identity is required for new JobDetail");
        Preconditions.checkNotNull(jobGroupName, "JobeGroupName is required for new JobDetail");

        return newJob(clazz)
                .withIdentity(identity, jobGroupName)
                .build();
    }
}