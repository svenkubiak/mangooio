package mangoo.io.scheduler;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

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

import mangoo.io.configuration.Config;
import mangoo.io.enums.Default;

/**
 *
 * @author svenkubiak
 *
 */
@Singleton
public class MangooScheduler {
    private static final Logger LOG = LoggerFactory.getLogger(MangooScheduler.class);
    private Scheduler scheduler;

    @Inject
    public MangooScheduler(MangooJobFactory quartzJobFactory, Config config) {
        for (String property : config.getAllConfigurations().stringPropertyNames()) {
            if (property.startsWith(Default.SCHEDULER_PREFIX.toString())) {
                System.setProperty(property, config.getString(property));
            }
        }

        final SchedulerFactory schedulerFactory = new StdSchedulerFactory();
        try {
            this.scheduler = schedulerFactory.getScheduler();
            this.scheduler.setJobFactory(quartzJobFactory);
        } catch (SchedulerException e) {
            LOG.error("Failed to get scheduler from schedulerFactory", e);
        }
    }

    public Scheduler getScheduler() {
        return this.scheduler;
    }

    public void start() {
        Preconditions.checkNotNull(this.scheduler, "Scheduler has not been initialized");

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
        Preconditions.checkNotNull(this.scheduler);

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
        Preconditions.checkNotNull(this.scheduler);

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

    public void schedule(JobDetail jobDetail, Trigger trigger) {
        Preconditions.checkNotNull(this.scheduler, "Scheduler has not been initialized");
        Preconditions.checkNotNull(jobDetail, "JobDetail is required for schedule");
        Preconditions.checkNotNull(trigger, "trigger is required for schedule");

        try {
            this.scheduler.scheduleJob(jobDetail, trigger);
        } catch (SchedulerException e) {
            LOG.error("Failed to schedule a new job", e);
        }
    }

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

    public <T extends Job> JobDetail getJobDetail(Class<T> clazz, String identity, String jobGroupName) {
        Preconditions.checkNotNull(clazz, "Class is required for new JobDetail");
        Preconditions.checkNotNull(identity, "Identity is required for new JobDetail");
        Preconditions.checkNotNull(jobGroupName, "JobeGroupName is required for new JobDetail");

        return newJob(clazz)
                .withIdentity(identity, jobGroupName)
                .build();
    }
}