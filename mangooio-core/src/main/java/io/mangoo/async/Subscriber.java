package io.mangoo.async;

public interface Subscriber<T> {
    /**
     * Receives a payload when send via EventBus
     * @param payload The payload send to the EventBus
     */
    void receive(T payload);
}
