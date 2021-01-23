package io.mangoo.persistence.events;

import java.util.Objects;

import com.google.inject.Inject;

import io.mangoo.persistence.Datastore;

public class SaveEvent {
    private Datastore datastore;
    private Object object;
    
    @Inject
    public SaveEvent(Datastore datastore) {
        this.datastore = Objects.requireNonNull(datastore, "datastore can not be null");
    }

    public void save() {
        datastore.save(object);
    }
    
    public SaveEvent withObject(Object object) {
        this.object = Objects.requireNonNull(object, "object can not be null");
        return this;
    }
}