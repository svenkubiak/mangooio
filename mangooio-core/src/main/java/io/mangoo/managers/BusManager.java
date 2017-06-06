package io.mangoo.managers;

import java.util.Objects;

import com.google.common.eventbus.EventBus;
import com.google.inject.Singleton;

import io.mangoo.enums.Required;

/**
*
* @author svenkubiak
*
*/
@Singleton
public class BusManager {
    private EventBus eventBus;
    
    public BusManager() {
        this.eventBus = new EventBus();
    }
    
    /**
     * Registers an event listener to the event bus
     * 
     * @param eventListener The listener to register
     */
    public void register(Object eventListener) {
        Objects.requireNonNull(eventListener, Required.EVENT_LISTENER.toString());
        this.eventBus.register(eventListener);
    }
    
    /**
     * Unregisters an event listener to the event bus
     * 
     * @param eventListener The listener to unregister
     */
    public void unregister(Object eventListener) {
        Objects.requireNonNull(eventListener, Required.EVENT_LISTENER.toString());
        this.eventBus.unregister(eventListener);
    }
    
    /**
     * Publishes an event to the event bus
     * 
     * @param event The event to publish
     */
    public void publish(Object event) {
        Objects.requireNonNull(event, Required.EVENT.toString());
        this.eventBus.post(event);
    }
}