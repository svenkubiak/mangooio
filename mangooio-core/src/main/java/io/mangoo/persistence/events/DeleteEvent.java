package io.mangoo.persistence.events;

import java.util.Objects;

import com.google.inject.Inject;

import io.mangoo.enums.Required;
import io.mangoo.persistence.Datastore;

/**
 * 
 * @author svenkubiak
 *
 */
public class DeleteEvent {
    private final Datastore datastore;
    private Object object;
    
    @Inject
    public DeleteEvent(Datastore datastore) {
        this.datastore = Objects.requireNonNull(datastore, Required.DATASTORE.toString());
    }

    public void delete() {
        datastore.delete(object);
    }
    
    public DeleteEvent withObject(Object object) {
        this.object = Objects.requireNonNull(object, Required.OBJECT.toString());
        return this;
    }
}