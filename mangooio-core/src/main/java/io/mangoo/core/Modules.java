package io.mangoo.core;

import org.quartz.spi.JobFactory;

import com.google.inject.AbstractModule;

import io.mangoo.scheduler.SchedulerFactory;

/**
 * Framework specific Google Guice Modules
 *
 * @author svenkubiak
 *
 */
public class Modules extends AbstractModule {
    @Override
    protected void configure() {
        bind(JobFactory.class).to(SchedulerFactory.class);
    }
}