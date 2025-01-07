package io.mangoo.persistence;

import io.mangoo.constants.NotNull;
import io.mangoo.persistence.interfaces.Datastore;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Singleton
public class DatastoreProvider implements Provider<Datastore> {
    private final Map<String, DatastoreImpl> datastores = new HashMap<>();
    private final DatastoreImpl defaultDatastore;

    @Inject
    private DatastoreProvider(DatastoreImpl defaultDatastore) {
        this.defaultDatastore = Objects.requireNonNull(defaultDatastore, NotNull.DATASTORE);
    }
    
    public Datastore getDatastore(String prefix) {
        Objects.requireNonNull(prefix, NotNull.PREFIX);

        return datastores.computeIfAbsent(prefix, key -> new DatastoreImpl(prefix));
    }
    
    @Override
    public Datastore get() {
        return defaultDatastore;
    }
}