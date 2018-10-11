package io.mangoo.core;

import org.quartz.spi.JobFactory;

import com.google.inject.AbstractModule;

import io.mangoo.configuration.Config;
import io.mangoo.interfaces.MangooCache;
import io.mangoo.interfaces.MangooAuthorizationService;
import io.mangoo.providers.CacheProvider;
import io.mangoo.scheduler.SchedulerFactory;
import io.mangoo.services.AuthorizationService;

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
        bind(MangooCache.class).toProvider(CacheProvider.class);
        bind(MangooAuthorizationService.class).to(AuthorizationService.class);
    }
}