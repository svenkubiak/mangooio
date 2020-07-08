package io.mangoo.core;

import org.quartz.spi.JobFactory;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import de.svenkubiak.embeddedmongodb.EmbeddedMongoDB;
import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheProvider;
import io.mangoo.interfaces.MangooAuthorizationService;
import io.mangoo.persistence.Datastore;
import io.mangoo.persistence.DatastoreProvider;
import io.mangoo.scheduler.SchedulerFactory;
import io.mangoo.services.AuthorizationService;

/**
 * 
 * @author svenkubiak
 *
 */
public class Module extends AbstractModule {
    private Config config = new Config();
    
    public Module() {
        if (config.isMongoEmbedded("")) {
            EmbeddedMongoDB.create()
                .withHost(config.getMongoHost(""))
                .withPort(config.getMongoPort(""))
                .start();
        }
    }
    
    @Override
    protected void configure() {
        Names.bindProperties(binder(), config.toProperties());
        
        bind(JobFactory.class).to(SchedulerFactory.class);
        bind(Cache.class).toProvider(CacheProvider.class);
        bind(Datastore.class).toProvider(DatastoreProvider.class);
        bind(MangooAuthorizationService.class).to(AuthorizationService.class);
    }
}