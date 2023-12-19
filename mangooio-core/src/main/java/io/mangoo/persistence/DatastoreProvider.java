package io.mangoo.persistence;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import io.mangoo.enums.Required;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Singleton
public class DatastoreProvider implements Provider<Datastore> {
    private Map<String, DatastoreImpl> datastores = new HashMap<>();
    private DatastoreImpl defaultDatastore;

    @Inject
    private DatastoreProvider(DatastoreImpl defaultDatastore) {
        this.defaultDatastore = Objects.requireNonNull(defaultDatastore, Required.DATASTORE.toString());
    }
    
    public Datastore getDatastore(String prefix) {
        Objects.requireNonNull(prefix, Required.PREFIX.toString());

        return datastores.computeIfAbsent(prefix, key -> new DatastoreImpl(prefix));
    }
    
    @Override
    public Datastore get() {
        return defaultDatastore;
    }
}