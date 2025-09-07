package io.mangoo.persistence;

import io.mangoo.constants.Required;
import io.mangoo.persistence.interfaces.Datastore;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Singleton
public class DatastoreProvider implements Provider<Datastore> {
    private final Map<String, DatastoreImpl> dataStores = new HashMap<>();
    private final DatastoreImpl defaultDatastore;

    @Inject
    private DatastoreProvider(DatastoreImpl defaultDatastore) {
        this.defaultDatastore = Objects.requireNonNull(defaultDatastore, Required.DATASTORE);
    }
    
    public Datastore getDatastore(String prefix) {
        Objects.requireNonNull(prefix, Required.PREFIX);

        return dataStores.computeIfAbsent(prefix, key -> new DatastoreImpl(prefix));
    }
    
    @Override
    public Datastore get() {
        return defaultDatastore;
    }
}