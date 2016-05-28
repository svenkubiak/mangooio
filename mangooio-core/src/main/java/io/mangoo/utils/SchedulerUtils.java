package io.mangoo.utils;

import static org.quartz.CronScheduleBuilder.cronSchedule;
import static org.quartz.JobBuilder.newJob;
import static org.quartz.TriggerBuilder.newTrigger;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.quartz.CronExpression;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.matchers.GroupMatcher;

import com.google.common.base.Preconditions;

import io.mangoo.core.Application;
import io.mangoo.exceptions.MangooSchedulerException;
import io.mangoo.scheduler.Scheduler;

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
     * schedule a new job by passing it into {@link io.mangoo.scheduler.Scheduler#schedule(JobDetail, Trigger) schedule}
     *
     * @param identity The name of the job
     * @param groupName The name of the job Group
     * @param clazz The class where the actual execution takes place
     *
     * @return A new JobDetail object
     */
    public static JobDetail createJobDetail(String identity, String groupName, Class<? extends Job> clazz) {
        Objects.requireNonNull(identity, "identity is required for new JobDetail");
        Objects.requireNonNull(groupName, "groupName is required for new JobDetail");
        Objects.requireNonNull(clazz, "clazz is required for new JobDetail");

        return newJob(clazz)
                .withIdentity(identity, groupName)
                .build();
    }
    
    /**
     * Retrieves a list of all jobs and their current status
     * 
     * @return List of io.mangoo.models.Job objects
     * @throws MangooSchedulerException if an error occurs during access to the Quartz Scheduler
     */
    @SuppressWarnings("unchecked")
    public static List<io.mangoo.models.Job> getAllJobs() throws MangooSchedulerException {
        List<io.mangoo.models.Job> jobs = new ArrayList<>();
        org.quartz.Scheduler scheduler = Application.getInstance(Scheduler.class).getQuartzScheduler();
        try {
            for (JobKey jobKey : getAllJobKeys()) {
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                Trigger trigger = triggers.get(0);
                TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                jobs.add(new io.mangoo.models.Job(TriggerState.PAUSED.equals(triggerState) ? false : true, jobKey.getName(), trigger.getDescription(), trigger.getNextFireTime(), trigger.getPreviousFireTime()));
            }
        } catch (SchedulerException e) {
            throw new MangooSchedulerException(e);
        }
        
        return jobs;
    }
    
    /**
     * Executes a single Quartz Scheduler job right away only once
     * 
     * @param jobName The name of the job to execute
     * @throws MangooSchedulerException if an error occurs during execution of the job
     */
    public static void executeJob(String jobName) throws MangooSchedulerException {
        org.quartz.Scheduler scheduler = Application.getInstance(Scheduler.class).getQuartzScheduler();
        try {
            for (JobKey jobKey : getAllJobKeys()) {
                if (jobKey.getName().equalsIgnoreCase(jobName)) {
                    scheduler.triggerJob(jobKey);  
                }
            }
        } catch (SchedulerException | MangooSchedulerException e) {
            throw new MangooSchedulerException(e);
        }
    }
    
    /**
     * Retrieves a list of all JobKeys from the Quartz Scheduler
     * 
     * @return List of all JobKey objects
     * @throws MangooSchedulerException if an errors occurs during access to the scheduler
     */
    public static List<JobKey> getAllJobKeys() throws MangooSchedulerException {
        List<JobKey> jobKeys = new ArrayList<>();
        org.quartz.Scheduler scheduler = Application.getInstance(Scheduler.class).getQuartzScheduler();
        try {
            for (String groupName : scheduler.getJobGroupNames()) {
                jobKeys.addAll(scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)));
                scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)).forEach(j -> System.out.println(j.getName()));
            }            
        } catch (SchedulerException e) {
            throw new MangooSchedulerException(e);
        }
        
        return jobKeys;
    }
    
    /**
     * Changes the state of a normally running job from pause to resume or resume to pause
     * 
     * @param jobName The name of the job
     * @throws MangooSchedulerException if an error occurs during access to the quartz scheuler
     */
    @SuppressWarnings("unchecked")
    public static void changeState(String jobName) throws MangooSchedulerException {
        org.quartz.Scheduler scheduler = Application.getInstance(Scheduler.class).getQuartzScheduler();
        try {
            for (JobKey jobKey : getAllJobKeys()) {
                if (jobKey.getName().equalsIgnoreCase(jobName)) {
                    List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                    Trigger trigger = triggers.get(0);  
                    TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                    if (TriggerState.NORMAL.equals(triggerState)) {
                        scheduler.pauseJob(jobKey);                        
                    } else {
                        scheduler.resumeJob(jobKey);
                    }
                }
            }            
        } catch (SchedulerException | MangooSchedulerException e) {
            throw new MangooSchedulerException(e);
        }
    }
}