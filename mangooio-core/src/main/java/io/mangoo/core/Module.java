package io.mangoo.core;

import org.quartz.spi.JobFactory;

import com.google.inject.AbstractModule;

import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.interfaces.MangooTemplateEngine;
import io.mangoo.providers.CacheProvider;
import io.mangoo.providers.TemplateEngineProvider;
import io.mangoo.scheduler.SchedulerFactory;

/**
 * 
 * @author svenkubiak
 *
 */
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(Config.class).toInstance(new Config());
        bind(JobFactory.class).to(SchedulerFactory.class);
        bind(Cache.class).toProvider(CacheProvider.class);
        bind(MangooTemplateEngine.class).toProvider(TemplateEngineProvider.class);
    }
}