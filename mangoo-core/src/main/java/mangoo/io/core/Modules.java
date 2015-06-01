package mangoo.io.core;

import mangoo.io.scheduler.MangooJobFactory;

import org.quartz.spi.JobFactory;

import com.google.inject.AbstractModule;

/**
 *
 * @author svenkubiak
 *
 */
public class Modules extends AbstractModule {
    @Override
    protected void configure() {
        bind(JobFactory.class).to(MangooJobFactory.class);
    }
}