package io.mangoo.persistence.events;

import java.util.Objects;

import com.google.inject.Inject;

import io.mangoo.persistence.Datastore;

public class DeleteEvent {
    private Datastore datastore;
    private Object object;
    
    @Inject
    public DeleteEvent(Datastore datastore) {
        this.datastore = Objects.requireNonNull(datastore, "datastore can not be null");
    }

    public void delete() {
        datastore.delete(object);
    }
    
    public DeleteEvent withObject(Object object) {
        this.object = Objects.requireNonNull(object, "object can not be null");
        return this;
    }
}