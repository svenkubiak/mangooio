package io.mangoo.core;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import de.svenkubiak.embeddedmongodb.EmbeddedMongoDB;
import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheProvider;
import io.mangoo.constants.Default;
import io.mangoo.core.beta.YamlConfig;
import io.mangoo.persistence.DatastoreProvider;
import io.mangoo.persistence.interfaces.Datastore;

public class Module extends AbstractModule {
    private final boolean beta = System.getProperty("mangooio.beta") != null;
    private final Config config = new Config();
    private YamlConfig yamlConfig;
    private EmbeddedMongoDB embeddedMongoDB;
    
    public Module() {
        var prefix = Default.PERSISTENCE_PREFIX;
        if (config.isPersistenceEnabled() && config.isMongoEmbedded(prefix)) {
            this.embeddedMongoDB = EmbeddedMongoDB.create()
                .withHost(config.getMongoHost(prefix))
                .withPort(config.getMongoPort(prefix))
                .start();
        }

        if (beta) {
            yamlConfig = new YamlConfig();
            if (yamlConfig.isPersistenceEnabled() && yamlConfig.isMongoEmbedded(prefix)) {
                this.embeddedMongoDB = EmbeddedMongoDB.create()
                        .withHost(yamlConfig.getMongoHost(prefix))
                        .withPort(yamlConfig.getMongoPort(prefix))
                        .start();
            }
        }
    }
    
    @Override
    protected void configure() {
        Names.bindProperties(binder(), config.toProperties());
        if (beta) {
            Names.bindProperties(binder(), yamlConfig.toProperties());
        }
        
        bind(Cache.class).toProvider(CacheProvider.class);
        bind(Datastore.class).toProvider(DatastoreProvider.class);
    }
    
    public void stopEmbeddedMongoDB() {
        if (embeddedMongoDB != null) {
            embeddedMongoDB.stop();
        }
    }
}