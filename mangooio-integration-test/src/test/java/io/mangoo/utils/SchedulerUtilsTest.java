package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.List;

import org.junit.Test;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;

import io.mangoo.core.Application;
import io.mangoo.exceptions.MangooSchedulerException;
import io.mangoo.scheduler.Scheduler;
import jobs.InfoJob;

/**
 * 
 * @author svenkubiak
 *
 */
public class SchedulerUtilsTest {
    private static final String JOB_NAME = "jobs.InfoJob";

    @Test
    public void testCreateTrigger() {
        //given
        String identity = "foo";
        String groupName = "bar";
        String description = "description";
        String cron = "0 0 3 * * ?";
        
        //when
        Trigger trigger = SchedulerUtils.createTrigger(identity, groupName, description, cron);
        
        //then
        assertThat(trigger, not(nullValue()));
        assertThat(trigger.getDescription(), equalTo(description));
    }
    
    @Test
    public void testCreateJobDetail() {
        //given
        String identity = "foo";
        String groupName = "bar";
        Class<? extends Job> clazz = InfoJob.class;
        
        //when
        JobDetail jobDetail = SchedulerUtils.createJobDetail(identity, groupName, clazz);
        
        //then
        assertThat(jobDetail, not(nullValue()));
    }
    
    @Test
    public void testGetAllJobs() throws MangooSchedulerException {
        //when
        List<io.mangoo.models.Job> jobs = SchedulerUtils.getAllJobs();
        
        //then
        assertThat(jobs, not(nullValue()));
        assertThat(jobs.size(), equalTo(1));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testChangeState() throws MangooSchedulerException, SchedulerException {
        //given
        org.quartz.Scheduler scheduler = Application.getInstance(Scheduler.class).getQuartzScheduler();
        
        //when
        SchedulerUtils.changeState(JOB_NAME);
        
        //then
        List<JobKey> jobKeys = SchedulerUtils.getAllJobKeys();
        
        //then
        for (JobKey jobKey : jobKeys) {
            if (jobKey.getName().equals(JOB_NAME)) {
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                Trigger trigger = triggers.get(0);  
                TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                
                assertThat(triggerState, equalTo(TriggerState.PAUSED));
            }
        }
        
        //when
        SchedulerUtils.changeState(JOB_NAME);
        
        //then
        jobKeys = SchedulerUtils.getAllJobKeys();
        
        //then
        for (JobKey jobKey : jobKeys) {
            if (jobKey.getName().equals(JOB_NAME)) {
                List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                Trigger trigger = triggers.get(0);  
                TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
                
                assertThat(triggerState, equalTo(TriggerState.NORMAL));
            }
        }
    }
    
    @Test
    public void testExecuteJob() throws MangooSchedulerException {
        //when
        SchedulerUtils.executeJob(JOB_NAME);
    }

    @Test
    public void testGetAllJobKeys() throws MangooSchedulerException {
        //when
        List<JobKey> jobKeys = SchedulerUtils.getAllJobKeys();
        
        //then
        assertThat(jobKeys, not(nullValue()));
        assertThat(jobKeys.size(), equalTo(1));
    }
}