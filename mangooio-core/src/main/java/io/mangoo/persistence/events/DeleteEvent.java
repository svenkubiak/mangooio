package io.mangoo.persistence.events;

import java.util.List;
import java.util.Objects;

import com.google.inject.Inject;

import io.mangoo.enums.Required;
import io.mangoo.persistence.Datastore;

public class DeleteEvent<T> {
    private final Datastore datastore;
    private Object object;
    private List<Class<T>> clazzes;
    
    @Inject
    public DeleteEvent(Datastore datastore) {
        this.datastore = Objects.requireNonNull(datastore, Required.DATASTORE.toString());
    }

    public void delete() {
    	if (object != null) {
            datastore.delete(object);
    	}
    	
    	if (clazzes != null && !clazzes.isEmpty()) {
    		for (Class<T> clazz : clazzes) {
        		datastore.deleteAll(clazz);  			
    		}
    	}
    }
    
    public DeleteEvent<T> withObject(Object object) {
        this.object = Objects.requireNonNull(object, Required.OBJECT.toString());
        return this;
    }

	public Object withObjects(List<Class<T>> clazzes) {
		this.clazzes = Objects.requireNonNull(clazzes, Required.CLASS.toString());
		return this;
	}
}