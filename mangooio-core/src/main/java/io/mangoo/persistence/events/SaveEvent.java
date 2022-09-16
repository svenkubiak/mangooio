package io.mangoo.persistence.events;

import java.util.List;
import java.util.Objects;

import com.google.inject.Inject;

import io.mangoo.enums.Required;
import io.mangoo.persistence.Datastore;

/**
 * 
 * @author svenkubiak
 * @param <T>
 *
 */
public class SaveEvent<T> {
    private final Datastore datastore;
    private Object object;
    private List<T> objects;
    
    @Inject
    public SaveEvent(Datastore datastore) {
        this.datastore = Objects.requireNonNull(datastore, Required.DATASTORE.toString());
    }

    public void save() {
    	if (object != null) {
            datastore.save(object);
    	}
    	
    	if (objects != null && !objects.isEmpty()) {
    		datastore.saveAll(objects);
    	}
    }
    
    public SaveEvent<T> withObject(Object object) {
        this.object = Objects.requireNonNull(object, Required.OBJECT.toString());
        return this;
    }

	public SaveEvent<T> withObjects(List<T> objects) {
		this.objects = Objects.requireNonNull(objects, Required.OBJECTS.toString());
		return this;
	}
}