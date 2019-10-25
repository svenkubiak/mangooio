package io.mangoo.core;

import org.quartz.spi.JobFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheProvider;
import io.mangoo.interfaces.MangooAuthorizationService;
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
        Config config = new Config();
        Names.bindProperties(binder(), config.toProperties());
        
        bind(JobFactory.class).to(SchedulerFactory.class);
        bind(Cache.class).toProvider(CacheProvider.class);
        bind(MangooAuthorizationService.class).to(AuthorizationService.class);
    }
}