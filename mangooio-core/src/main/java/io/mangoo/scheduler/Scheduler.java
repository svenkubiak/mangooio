package io.mangoo.scheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.impl.StdSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.exceptions.MangooSchedulerException;

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
    	config.getAllConfigurations().entrySet().forEach((Map.Entry<String, String> entry) -> {
            if (entry.getKey().startsWith(Default.SCHEDULER_PREFIX.toString())) {
                System.setProperty(entry.getKey(), entry.getValue());
            }
        });
    }
    
    /***
     * Initializes the scheduler by booting up Quartz
     * 
     */
    public void initialize() {
        try {
            this.quartzScheduler = new StdSchedulerFactory().getScheduler();
            this.quartzScheduler.setJobFactory(Application.getInstance(SchedulerFactory.class));
        } catch (final SchedulerException e) {
            LOG.error("Failed to initialize scheduler", e);
        }	
    }

    /**
     * Returns the current scheduler instance
     * @deprecated As of release 3.1.0, replaced by {@link #getQuartzScheduler()}
     *
     * @return Scheduler instance, null if scheduler is not initialize or started
     */
    @Deprecated
    public org.quartz.Scheduler getScheduler() {
        return this.quartzScheduler;
    }
    
    /**
     * Returns the current quartz scheduler instance
     *
     * @return Scheduler instance, null if scheduler is not initialize or started
     */
    public org.quartz.Scheduler getQuartzScheduler() {
        return this.quartzScheduler;
    }

    public void start() throws MangooSchedulerException {
    	Objects.requireNonNull(this.quartzScheduler, "Scheduler is not initialized");
    	
        try {
            this.quartzScheduler.start();
            if (this.quartzScheduler.isStarted()) {
                LOG.info("Successfully started quartz scheduler");
            } else {
                LOG.error("Scheduler is not started");
            }
        } catch (final SchedulerException e) {
        	throw new MangooSchedulerException(e);
        }
    }

    public void shutdown() throws MangooSchedulerException {
        Objects.requireNonNull(this.quartzScheduler, "Scheduler is not initialized");

        try {
            this.quartzScheduler.shutdown();
            if (this.quartzScheduler.isShutdown()) {
                LOG.info("Successfully shutdown quartz scheduler");
            } else {
                LOG.error("Failed to shutdown scheduler");
            }
        } catch (final SchedulerException e) {
        	throw new MangooSchedulerException(e);
        }
    }

    public void standby() throws MangooSchedulerException {
        Objects.requireNonNull(this.quartzScheduler, "Scheduler is not initialized");

        try {
            this.quartzScheduler.standby();
            if (this.quartzScheduler.isInStandbyMode()) {
                LOG.info("Scheduler is now in standby");
            } else {
                LOG.error("Failed to put scheduler in standby");
            }
        } catch (final SchedulerException e) {
        	throw new MangooSchedulerException(e);
        }
    }

    /**
     * Adds a new job with a given JobDetail and Trigger to the scheduler
     *
     * @param jobDetail The JobDetail for the Job
     * @param trigger The Trigger for the job
     */
    public void schedule(JobDetail jobDetail, Trigger trigger) throws MangooSchedulerException {
        Objects.requireNonNull(jobDetail, "JobDetail is required for schedule");
        Objects.requireNonNull(trigger, "trigger is required for schedule");
        Objects.requireNonNull(this.quartzScheduler, "Scheduler is not initialized");

        try {
            this.quartzScheduler.scheduleJob(jobDetail, trigger);
        } catch (final SchedulerException e) {
        	throw new MangooSchedulerException(e);
        }
    }
    

    /**
     * Retrieves a list of all jobs and their current status
     * 
     * @return List of io.mangoo.models.Job objects
     * @throws MangooSchedulerException if an error occurs during access to the Quartz Scheduler
     */
    @SuppressWarnings("unchecked")
    public List<io.mangoo.models.Job> getAllJobs() throws MangooSchedulerException {
        Objects.requireNonNull(this.quartzScheduler, "Scheduler is not initialized");
        
        List<io.mangoo.models.Job> jobs = new ArrayList<>();
        try {
            for (JobKey jobKey : getAllJobKeys()) {
                List<Trigger> triggers = (List<Trigger>) this.quartzScheduler.getTriggersOfJob(jobKey);
                Trigger trigger = triggers.get(0);  
                TriggerState triggerState = quartzScheduler.getTriggerState(trigger.getKey());
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
    public void executeJob(String jobName) throws MangooSchedulerException {
        Objects.requireNonNull(this.quartzScheduler, "Scheduler is not initialized");
        
        try {
            for (JobKey jobKey : getAllJobKeys()) {
                if (jobKey.getName().equalsIgnoreCase(jobName)) {
                    this.quartzScheduler.triggerJob(jobKey);  
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
    public List<JobKey> getAllJobKeys() throws MangooSchedulerException {
        Objects.requireNonNull(this.quartzScheduler, "Scheduler is not initialized");
        
        List<JobKey> jobKeys = new ArrayList<>();
        try {
            for (String groupName : this.quartzScheduler.getJobGroupNames()) {
                jobKeys.addAll(this.quartzScheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName)));
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
     * @throws MangooSchedulerException if an error occurs during access to the quartz scheduler
     */
    public void changeState(String jobName) throws MangooSchedulerException {
        Objects.requireNonNull(this.quartzScheduler, "Scheduler is not initialized");
        
        try {
            for (JobKey jobKey : getAllJobKeys()) {
                if (jobKey.getName().equalsIgnoreCase(jobName)) {
                    TriggerState triggerState = getTriggerState(jobKey);
                    if (TriggerState.NORMAL.equals(triggerState)) {
                        this.quartzScheduler.pauseJob(jobKey);                        
                    } else {
                        this.quartzScheduler.resumeJob(jobKey);
                    }
                }
            }            
        } catch (SchedulerException | MangooSchedulerException e) {
            throw new MangooSchedulerException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private TriggerState getTriggerState(JobKey jobKey) throws SchedulerException {
        Objects.requireNonNull(this.quartzScheduler, "Scheduler is not initialized");
        
        List<Trigger> triggers = (List<Trigger>) this.quartzScheduler.getTriggersOfJob(jobKey);
        Trigger trigger = triggers.get(0);  

        return this.quartzScheduler.getTriggerState(trigger.getKey());
    }
}