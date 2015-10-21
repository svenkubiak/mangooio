package io.mangoo.scheduler;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import com.google.common.base.Preconditions;

import io.mangoo.core.Application;

/**
 * Factory method for passing Scheduler job instance to the Google Guice injector 
 * 
 * @author svenkubiak
 *
 */
public class MangooJobFactory implements JobFactory {
    @Override
    public Job newJob(final TriggerFiredBundle triggerFiredBundle, final Scheduler scheduler) {
        Preconditions.checkNotNull(triggerFiredBundle, "triggerFiredBundle is required for a new job");
        Preconditions.checkNotNull(scheduler, "scheduler is required for a new job");

        return Application.getInstance(triggerFiredBundle.getJobDetail().getJobClass());
    }
}