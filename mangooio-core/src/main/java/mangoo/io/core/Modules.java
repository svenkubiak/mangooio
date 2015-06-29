package mangoo.io.core;

import org.quartz.spi.JobFactory;

import com.google.inject.AbstractModule;

import mangoo.io.scheduler.MangooJobFactory;

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