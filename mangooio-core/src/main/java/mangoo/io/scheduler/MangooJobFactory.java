package mangoo.io.scheduler;

import org.quartz.Job;
import org.quartz.Scheduler;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;

import com.google.inject.Inject;
import com.google.inject.Injector;

/**
 *
 * @author svenkubiak
 *
 */
public class MangooJobFactory implements JobFactory {
    @Inject
    private Injector injector;

    @Override
    public Job newJob(final TriggerFiredBundle triggerFiredBundle, final Scheduler scheduler) {
        return injector.getInstance(triggerFiredBundle.getJobDetail().getJobClass());
    }
}