package io.mangoo.scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Map;
import java.util.Objects;

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
        Objects.requireNonNull(config, "config can not be null");

        config.getAllConfigurations().entrySet().forEach((Map.Entry<String, String> entry) -> {
            if (entry.getKey().startsWith(Default.SCHEDULER_PREFIX.toString())) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
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
        } catch (final SchedulerException e) {
            LOG.error("Failed to start scheduler", e);
        }
    }

    public void shutdown() {
        Objects.requireNonNull(this.quartzScheduler, "Scheduler is not initialized or started");

        try {
            this.quartzScheduler.shutdown();
            if (this.quartzScheduler.isShutdown()) {
                LOG.info("Successfully shutdown quartz scheduler");
            } else {
                LOG.error("Failed to shutdown scheduler");
            }
        } catch (final SchedulerException e) {
            LOG.error("Failed to shutdown scheduler", e);
        }
    }

    public void standby() {
        Objects.requireNonNull(this.quartzScheduler, "Scheduler is not initialized or started");

        try {
            this.quartzScheduler.standby();
            if (this.quartzScheduler.isInStandbyMode()) {
                LOG.info("Scheduler is now in standby");
            } else {
                LOG.error("Failed to put scheduler in standby");
            }
        } catch (final SchedulerException e) {
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
            } catch (final SchedulerException e) {
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
        Objects.requireNonNull(jobDetail, "JobDetail is required for schedule");
        Objects.requireNonNull(trigger, "trigger is required for schedule");
        initialize();

        try {
            this.quartzScheduler.scheduleJob(jobDetail, trigger);
        } catch (final SchedulerException e) {
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
        Objects.requireNonNull(identity, "Identity is required for creating a new trigger");
        Objects.requireNonNull(groupName, "groupName is required for new trigger");
        Objects.requireNonNull(cron, "cron is required for new trigger");
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
     *
     * @return A new JobDetail object
     */
    public JobDetail createJobDetail(String identity, String groupName, Class<? extends Job> clazz) {
        Objects.requireNonNull(identity, "identity is required for new JobDetail");
        Objects.requireNonNull(groupName, "groupName is required for new JobDetail");
        Objects.requireNonNull(clazz, "clazz is required for new JobDetail");

        return newJob(clazz)
                .withIdentity(identity, groupName)
                .build();
    }
}