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
public class SaveEvent {
    private Datastore datastore;
    private Object object;
    
    @Inject
    public SaveEvent(Datastore datastore) {
        this.datastore = Objects.requireNonNull(datastore, Required.DATASTORE.toString());
    }

    public void save() {
        datastore.save(object);
    }
    
    public SaveEvent withObject(Object object) {
        this.object = Objects.requireNonNull(object, Required.OBJECT.toString());
        return this;
    }
}