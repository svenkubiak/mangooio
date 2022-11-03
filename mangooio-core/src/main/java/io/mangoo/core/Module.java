package io.mangoo.core;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import de.svenkubiak.embeddedmongodb.EmbeddedMongoDB;
import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheProvider;
import io.mangoo.enums.Default;
import io.mangoo.persistence.Datastore;
import io.mangoo.persistence.DatastoreProvider;

public class Module extends AbstractModule {
    private Config config = new Config();
    private EmbeddedMongoDB embeddedMongoDB;
    
    public Module() {
        var prefix = Default.PERSISTENCE_PREFIX.toString();
        if (config.isMongoEmbedded(prefix)) {
            this.embeddedMongoDB = EmbeddedMongoDB.create()
                .withHost(config.getMongoHost(prefix))
                .withPort(config.getMongoPort(prefix))
                .start();
        }
    }
    
    @Override
    protected void configure() {
        Names.bindProperties(binder(), config.toProperties());
        
        bind(Cache.class).toProvider(CacheProvider.class);
        bind(Datastore.class).toProvider(DatastoreProvider.class);
    }
    
    public void stopEmbeddedMongoDB() {
        if (embeddedMongoDB != null) {
            embeddedMongoDB.stop();
        }
    }
}