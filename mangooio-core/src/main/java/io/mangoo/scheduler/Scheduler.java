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
import io.mangoo.enums.Required;
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
     * Checks if the scheduler has been initialized
     * 
     * @return True if the scheduler has been initializes, false otherwise
     */
    public boolean isInitialize() {
        return this.quartzScheduler != null;
    }
    
    /**
     * Checks if the scheduler is initialized and started
     * 
     * @return true if the scheduler is started, false otherwise
     * @throws MangooSchedulerException if an error occurred accessing the scheduler
     */
    public boolean isStarted() throws MangooSchedulerException {
        boolean started;
        try {
            started = this.quartzScheduler != null && this.quartzScheduler.isStarted();
        } catch (SchedulerException e) {
            throw new MangooSchedulerException(e);
        }
        
        return started;
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
        Objects.requireNonNull(this.quartzScheduler, Required.SCHEDULER.toString());
        
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
        Objects.requireNonNull(this.quartzScheduler, Required.SCHEDULER.toString());

        try {
            if (isStarted()) {
                this.quartzScheduler.shutdown();
                if (this.quartzScheduler.isShutdown()) {
                    LOG.info("Successfully shutdown quartz scheduler");
                } else {
                    LOG.error("Failed to shutdown scheduler");
                }  
            }
        } catch (final SchedulerException e) {
            throw new MangooSchedulerException(e);
        }
    }

    public void standby() throws MangooSchedulerException {
        Objects.requireNonNull(this.quartzScheduler, Required.SCHEDULER.toString());

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
     * @throws MangooSchedulerException when accessing the scheduler results in an error
     */
    public void schedule(JobDetail jobDetail, Trigger trigger) throws MangooSchedulerException {
        Objects.requireNonNull(jobDetail, Required.JOB_DETAIL.toString());
        Objects.requireNonNull(trigger, Required.TRIGGER.toString());
        Objects.requireNonNull(this.quartzScheduler, Required.SCHEDULER.toString());

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
        Objects.requireNonNull(this.quartzScheduler, Required.SCHEDULER.toString());
        
        List<io.mangoo.models.Job> jobs = new ArrayList<>();
        try {
            for (JobKey jobKey : getAllJobKeys()) {
                List<Trigger> triggers = (List<Trigger>) this.quartzScheduler.getTriggersOfJob(jobKey);
                Trigger trigger = triggers.get(0);  
                TriggerState triggerState = quartzScheduler.getTriggerState(trigger.getKey());
                jobs.add(new io.mangoo.models.Job(TriggerState.PAUSED == triggerState ? false : true, jobKey.getName(), trigger.getDescription(), trigger.getNextFireTime(), trigger.getPreviousFireTime()));
            }
        } catch (SchedulerException e) {
            throw new MangooSchedulerException(e);
        }
        
        return jobs;
    }
    
    /**
     * Pauses a job by a given name
     * @param name The name of the job to pause
     * 
     * @throws MangooSchedulerException when pausing fails
     */
    public void pauseJob(String name) throws MangooSchedulerException {
        Objects.requireNonNull(name, Required.NAME.toString());
        
        try {
            JobKey jobKey = getJobKey(name);
            this.quartzScheduler.pauseJob(jobKey);
        } catch (SchedulerException | MangooSchedulerException e) {
            throw new MangooSchedulerException(e);
        }            
    }
    
    /**
     * Resume a job by a given name
     * @param name The name of the job to resume
     * 
     * @throws MangooSchedulerException if resuming fails
     */
    public void resumeJob(String name) throws MangooSchedulerException {
        Objects.requireNonNull(name, Required.NAME.toString());
        
        try {
            JobKey jobKey = getJobKey(name);
            this.quartzScheduler.resumeJob(jobKey);
        } catch (SchedulerException | MangooSchedulerException e) {
            throw new MangooSchedulerException(e);
        }        
    }
    
    /**
     * Delete a job by a given name
     * @param name The name of the job to delete
     * 
     * @throws MangooSchedulerException if deletion fails
     */
    public void deleteJob(String name) throws MangooSchedulerException {
        Objects.requireNonNull(name, Required.NAME.toString());
        
        try {
            JobKey jobKey = getJobKey(name);
            this.quartzScheduler.deleteJob(jobKey);
        } catch (SchedulerException | MangooSchedulerException e) {
            throw new MangooSchedulerException(e);
        }          
    }
    
    /**
     * Retrieves a JobKey by it given name
     * 
     * @param name The name of the Job in the Scheduler
     * @return Optional of JobKey
     * 
     * @throws MangooSchedulerException if retrieving the job fails
     */
    public JobKey getJobKey(String name) throws MangooSchedulerException {
        Objects.requireNonNull(name, Required.NAME.toString());
        
        List<JobKey> allJobKeys = getAllJobKeys();
        for (JobKey jobKey : allJobKeys) {
            if (jobKey.getName().equalsIgnoreCase(name)) {
                return jobKey;
            }
        }
        
        return null;
    }
    
    /**
     * Executes a single Quartz Scheduler job right away only once
     * 
     * @param jobName The name of the job to execute
     * @throws MangooSchedulerException if an error occurs during execution of the job
     */
    public void executeJob(String jobName) throws MangooSchedulerException {
        Objects.requireNonNull(this.quartzScheduler, Required.SCHEDULER.toString());
        Objects.requireNonNull(jobName, Required.JOB_NAME.toString());
        
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
        Objects.requireNonNull(this.quartzScheduler, Required.SCHEDULER.toString());
        
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
        Objects.requireNonNull(this.quartzScheduler, Required.SCHEDULER.toString());
        
        try {
            for (JobKey jobKey : getAllJobKeys()) {
                if (jobKey.getName().equalsIgnoreCase(jobName)) {
                    TriggerState triggerState = getTriggerState(jobKey);
                    if (TriggerState.NORMAL == triggerState) {
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
        Objects.requireNonNull(this.quartzScheduler, Required.SCHEDULER.toString());
        
        List<Trigger> triggers = (List<Trigger>) this.quartzScheduler.getTriggersOfJob(jobKey);
        Trigger trigger = triggers.get(0);  

        return this.quartzScheduler.getTriggerState(trigger.getKey());
    }
}