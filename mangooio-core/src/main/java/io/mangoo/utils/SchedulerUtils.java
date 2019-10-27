package io.mangoo.utils;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;

import com.google.common.base.Preconditions;

import io.mangoo.enums.Required;

/**
 * 
 * @author svenkubiak
 *
 */
public final class SchedulerUtils {
    
    private SchedulerUtils() {
    }
    
    /**
     * Creates a new quartz scheduler Trigger, which can be used to
     * schedule a new job by passing it into {@link io.mangoo.scheduler.Scheduler#schedule(JobDetail, Trigger) schedule}
     *
     * @param identity The name of the trigger
     * @param groupName The trigger group name
     * @param description The trigger description
     * @param cron The cron expression for the trigger
     *
     * @return A new Trigger object
     */
    public static Trigger createTrigger(String identity, String groupName, String description, String cron) {
        Objects.requireNonNull(identity, Required.IDENTITY.toString());
        Objects.requireNonNull(groupName, Required.GROUP_NAME.toString());
        Objects.requireNonNull(cron, Required.CRON.toString());
        Preconditions.checkArgument(CronExpression.isValidExpression(cron), "cron expression is invalid");

        return newTrigger()
                .withIdentity(identity, groupName)
                .withSchedule(cronSchedule(cron))
                .withDescription(description)
                .build();
    }
    
    /**
     * Creates a new quartz scheduler Trigger, which can be used to
     * schedule a new job by passing it into {@link io.mangoo.scheduler.Scheduler#schedule(JobDetail, Trigger) schedule}
     *
     * @param identity The name of the trigger
     * @param groupName The trigger group name
     * @param description The trigger description
     * @param timespan The timespan for the trigger
     * @param timeUnit The timeUnit for the trigger
     *
     * @return A new Trigger object
     */
    public static Trigger createTrigger(String identity, String groupName, String description, int timespan, TimeUnit timeUnit) {
        Objects.requireNonNull(identity, Required.IDENTITY.toString());
        Objects.requireNonNull(groupName, Required.GROUP_NAME.toString());
        Objects.requireNonNull(timespan, Required.TIMEPSAN.toString());
        Objects.requireNonNull(timeUnit, Required.TIMEUNIT.toString());
        
        TriggerBuilder<Trigger> triggerBuilder = newTrigger()
                .withDescription(description)
                .withIdentity(identity, groupName);
        
        if (timeUnit.equals(TimeUnit.SECONDS)) {
            triggerBuilder.withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(timespan));
        } else if (timeUnit.equals(TimeUnit.MINUTES)) {
            triggerBuilder.withSchedule(SimpleScheduleBuilder.repeatMinutelyForever(timespan));
        } else if (timeUnit.equals(TimeUnit.HOURS)) {
            triggerBuilder.withSchedule(SimpleScheduleBuilder.repeatHourlyForever(timespan));
        } else if (timeUnit.equals(TimeUnit.DAYS)) {
            triggerBuilder.withSchedule(SimpleScheduleBuilder.repeatHourlyForever(timespan * 24));
        }

        return triggerBuilder.build();
    }

    /**
     * Creates a new quartz scheduler JobDetail, which can be used to
     * schedule a new job by passing it into {@link io.mangoo.scheduler.Scheduler#schedule(JobDetail, Trigger) schedule}
     *
     * @param identity The name of the job
     * @param groupName The name of the job Group
     * @param clazz The class where the actual execution takes place
     *
     * @return A new JobDetail object
     */
    public static JobDetail createJobDetail(String identity, String groupName, Class<? extends Job> clazz) {
        Objects.requireNonNull(identity, Required.IDENTITY.toString());
        Objects.requireNonNull(groupName, Required.GROUP_NAME.toString());
        Objects.requireNonNull(clazz, Required.CLASS.toString());

        return newJob(clazz)
                .withIdentity(identity, groupName)
                .build();
    }
}