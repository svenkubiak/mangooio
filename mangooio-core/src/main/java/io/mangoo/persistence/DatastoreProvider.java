package io.mangoo.persistence;

import io.mangoo.constants.Required;
import io.mangoo.persistence.interfaces.Datastore;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Singleton
public class DatastoreProvider implements Provider<Datastore> {
    // Concurrent first calls for a prefix must not create a second client that is never closed
    private final Map<String, DatastoreImpl> dataStores = new ConcurrentHashMap<>();
    private final DatastoreImpl defaultDatastore;

    @Inject
    private DatastoreProvider(DatastoreImpl defaultDatastore) {
        this.defaultDatastore = Objects.requireNonNull(defaultDatastore, Required.DATASTORE);
    }
    
    public Datastore getDatastore(String prefix) {
        Objects.requireNonNull(prefix, Required.PREFIX);

        return dataStores.computeIfAbsent(prefix, key -> new DatastoreImpl(prefix));
    }
    
    public void close() {
        dataStores.values().forEach(DatastoreImpl::close);
        defaultDatastore.close();
    }

    @Override
    public Datastore get() {
        return defaultDatastore;
    }
}