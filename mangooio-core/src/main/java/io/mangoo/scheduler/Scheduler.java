package io.mangoo.scheduler;

import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

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
    private static final Config CONFIG = Application.getConfig();
    private org.quartz.Scheduler quartzScheduler;

    public Scheduler() {
        CONFIG.getAllConfigurations().entrySet().forEach((Map.Entry<String, String> entry) -> {
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
    public org.quartz.Scheduler getQuartzScheduler() {
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
                this.quartzScheduler.setJobFactory(Application.getInstance(SchedulerFactory.class));
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
}