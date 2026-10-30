package io.mangoo.core;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.svenkubiak.embeddedmongodb.EmbeddedMongoDB;
import io.mangoo.async.InMemoryMessageBroker;
import io.mangoo.cache.*;
import io.mangoo.constants.Default;
import io.mangoo.constants.Key;
import io.mangoo.interfaces.AttemptStore;
import io.mangoo.interfaces.InstanceRegistry;
import io.mangoo.interfaces.MessageBroker;
import io.mangoo.interfaces.OneTimeStore;
import io.mangoo.interfaces.TokenBlacklist;
import io.mangoo.interfaces.persistence.Datastore;
import io.mangoo.persistence.*;
import org.apache.commons.lang3.StringUtils;

import java.util.TimeZone;

public class Module extends AbstractModule {
    public final Config config = new Config();
    private EmbeddedMongoDB embeddedMongoDB;

    public Module() {
        System.setProperty("user.timezone", config.getApplicationTimezone());
        TimeZone.setDefault(TimeZone.getTimeZone(config.getApplicationTimezone()));

        var prefix = Default.PERSISTENCE_PREFIX;
        if (config.isPersistenceEnabled() && Boolean.TRUE.equals(config.isMongoEmbedded(prefix))) {
            if (StringUtils.isNotBlank(config.getMongoUrl(prefix))) {
                throw new IllegalArgumentException(prefix + Key.PERSISTENCE_MONGO_URL
                        + " cannot be combined with " + prefix + Key.PERSISTENCE_MONGO_EMBEDDED);
            }
            this.embeddedMongoDB = EmbeddedMongoDB.create()
                    .withVersion(Version.Main.V7_0)
                    .withHost(config.getMongoHost(prefix))
                    .withPort(config.getMongoPort(prefix))
                    .start();
        }
    }

    @Override
    protected void configure() {
        Names.bindProperties(binder(), config.toProperties());

        bind(Config.class).toInstance(config);
        bind(Cache.class).toProvider(CacheProvider.class);
        bind(Datastore.class).toProvider(DatastoreProvider.class);

        // Persistence means several instances may run, so the state they have to share is kept in MongoDB
        if (config.isPersistenceEnabled()) {
            bind(AttemptStore.class).to(MongoAttemptStore.class);
            bind(OneTimeStore.class).to(MongoOneTimeStore.class);
            bind(TokenBlacklist.class).to(MongoTokenBlacklist.class);
            bind(MessageBroker.class).to(MongoMessageBroker.class);
            bind(InstanceRegistry.class).to(MongoInstanceRegistry.class);
        } else {
            bind(AttemptStore.class).to(InMemoryAttemptStore.class);
            bind(OneTimeStore.class).to(InMemoryOneTimeStore.class);
            bind(TokenBlacklist.class).to(InMemoryTokenBlacklist.class);
            bind(MessageBroker.class).to(InMemoryMessageBroker.class);
            bind(InstanceRegistry.class).to(InMemoryInstanceRegistry.class);
        }
    }

    public void stopEmbeddedMongoDB() {
        if (embeddedMongoDB != null) {
            embeddedMongoDB.stop();
        }
    }
}