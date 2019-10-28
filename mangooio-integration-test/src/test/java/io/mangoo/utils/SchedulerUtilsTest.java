package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.exparity.hamcrest.date.DateMatchers;
import org.junit.jupiter.api.Test;
import org.quartz.Job;
import org.quartz.JobDetail;
import org.quartz.Trigger;

import jobs.InfoJob;

/**
 * 
 * @author svenkubiak
 *
 */
public class SchedulerUtilsTest {

    @Test
    public void testCreateTriggerCron() {
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
    public void testCreateTriggerEverySecond() {
        //given
        String identity = "foo";
        String groupName = "bar";
        String description = "description";
        int timespan = 3;
        TimeUnit timeUnit = TimeUnit.SECONDS;
        
        //when
        Trigger trigger = SchedulerUtils.createTrigger(identity, groupName, description, timespan, timeUnit);
        
        //then
        assertThat(trigger, not(nullValue()));
        assertThat(trigger.getDescription(), equalTo(description));
        assertThat(trigger.getStartTime(), DateMatchers.sameMinuteOfHour(new Date()));
    }
    
    @Test
    public void testCreateTriggerEveryMinutes() {
        //given
        String identity = "foo";
        String groupName = "bar";
        String description = "description";
        int timespan = 3;
        TimeUnit timeUnit = TimeUnit.MINUTES;
        
        //when
        Trigger trigger = SchedulerUtils.createTrigger(identity, groupName, description, timespan, timeUnit);
        
        //then
        assertThat(trigger, not(nullValue()));
        assertThat(trigger.getDescription(), equalTo(description));
        assertThat(trigger.getStartTime(), DateMatchers.sameHourOfDay(new Date()));
    }
    
    @Test
    public void testCreateTriggerEveryDay() {
        //given
        String identity = "foo";
        String groupName = "bar";
        String description = "description";
        int timespan = 3;
        TimeUnit timeUnit = TimeUnit.DAYS;
        
        //when
        Trigger trigger = SchedulerUtils.createTrigger(identity, groupName, description, timespan, timeUnit);
        
        //then
        assertThat(trigger, not(nullValue()));
        assertThat(trigger.getDescription(), equalTo(description));
        assertThat(trigger.getStartTime(), DateMatchers.sameDayOfMonth(new Date()));
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
}