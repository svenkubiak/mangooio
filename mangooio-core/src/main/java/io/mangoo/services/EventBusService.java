package io.mangoo.services;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.eventbus.AsyncEventBus;
import com.google.inject.Singleton;

import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooEventBusException;

/**
*
* @author svenkubiak
*
*/
@Singleton
public class EventBusService {
    private AsyncEventBus asyncEventBus;
    private AtomicLong listeners = new AtomicLong();
    private AtomicLong events = new AtomicLong();
    
    public EventBusService() {
        this.asyncEventBus = new AsyncEventBus(Executors.newCachedThreadPool());
    }
    
    /**
     * Registers an event listener to the event bus
     * 
     * @param eventListener The listener to register
     */
    public void register(Object eventListener) {
        Objects.requireNonNull(eventListener, Required.EVENT_LISTENER.toString());
        
        this.asyncEventBus.register(eventListener);
        this.listeners.getAndIncrement();
    }
    
    /**
     * Unregisters an event listener to the event bus
     * 
     * @param eventListener The listener to unregister
     * @throws MangooEventBusException when unregistering an event fails
     */
    public void unregister(Object eventListener) throws MangooEventBusException {
        Objects.requireNonNull(eventListener, Required.EVENT_LISTENER.toString());
        
        try {
            this.asyncEventBus.unregister(eventListener);
        } catch (IllegalArgumentException e) {
            throw new MangooEventBusException(e);
        }

        if (this.listeners.get() > 0) {
            this.listeners.getAndDecrement();            
        }
    }
    
    /**
     * Publishes an event to the event bus
     * 
     * @param event The event to publish
     */
    public void publish(Object event) {
        Objects.requireNonNull(event, Required.EVENT.toString());
        
        this.asyncEventBus.post(event);
        this.events.getAndIncrement();
    }
    
    /**
     * @return The number of registered listeners
     */
    public long getNumListeners() {
        return this.listeners.get();
    }
    
    /**
     * @return The number of published events to the event bus
     */
    public long getNumEvents() {
        return this.events.get();
    }
}