package io.mangoo.persistence;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.mangoo.enums.Required;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class DatastoreProvider implements Provider<Datastore> {
    private Map<String, DatastoreImpl> datastores = new HashMap<>();
    private DatastoreImpl defaultDatastore;

    @Inject
    private DatastoreProvider(DatastoreImpl defaultDatastore) {
        this.defaultDatastore = Objects.requireNonNull(defaultDatastore, "defaultDatastore can not be null");
    }
    
    public Datastore getDatastore(String prefix) {
        Objects.requireNonNull(prefix, Required.PREFIX.toString());
        
        DatastoreImpl datastore = this.datastores.get(prefix);
        if (datastore == null) {
            datastore = new DatastoreImpl(prefix);
            this.datastores.put(prefix, datastore);
        }
        
        return datastore;
    }
    
    @Override
    public Datastore get() {
        return this.defaultDatastore;
    }
}