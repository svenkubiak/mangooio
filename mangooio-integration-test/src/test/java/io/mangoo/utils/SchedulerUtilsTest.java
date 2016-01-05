package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;
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
}