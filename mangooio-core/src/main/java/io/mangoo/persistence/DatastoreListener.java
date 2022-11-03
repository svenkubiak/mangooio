package io.mangoo.persistence;

import com.google.common.eventbus.Subscribe;

import io.mangoo.persistence.events.DeleteEvent;
import io.mangoo.persistence.events.SaveEvent;

public class DatastoreListener {
    @Subscribe
    public void saveEvent(SaveEvent<?> saveEvent) {
        saveEvent.save();
    }
    
    @Subscribe
    public void deleteEvent(DeleteEvent<?> deleteEvent) {
        deleteEvent.delete();
    }
}