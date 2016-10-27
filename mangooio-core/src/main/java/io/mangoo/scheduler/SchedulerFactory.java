package io.mangoo.scheduler;

import java.util.Objects;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import io.mangoo.core.Application;
import io.mangoo.enums.Required;

/**
 * Factory method for passing Scheduler job instance to the Google Guice injector
 *
 * @author svenkubiak
 *
 */
public class SchedulerFactory implements JobFactory {
    @Override
    public Job newJob(TriggerFiredBundle triggerFiredBundle, Scheduler scheduler) {
        Objects.requireNonNull(triggerFiredBundle, Required.TRIGGER_FIRE_BUNDLE.toString());
        Objects.requireNonNull(scheduler, Required.SCHEDULER.toString());

        return Application.getInstance(triggerFiredBundle.getJobDetail().getJobClass());
    }
}