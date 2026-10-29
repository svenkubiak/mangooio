package io.mangoo.interfaces;

import java.util.function.Consumer;

/**
 * Fan-out of messages to every instance, e.g. for server sent events. The MongoDB implementation is bound when
 * persistence is enabled, the in-memory implementation otherwise; bind your own implementation in app.Module to replace it.
 * Delivery is best effort and at most once: a message published while an instance is down or disconnected is lost for it.
 */
public interface MessageBroker {

    /** Delivers the message to every subscriber of the topic on every instance, including this one, on a virtual thread. */
    void publish(String topic, String message);

    void subscribe(String topic, Consumer<String> consumer);
}
