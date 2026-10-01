package io.mangoo.async;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class EventBus<T> {
    private static final Logger LOG = LogManager.getLogger(EventBus.class);
    private final Multimap<String, Class<?>> subscribers = ArrayListMultimap.create();
    private final AtomicLong handledEvents = new AtomicLong();
    private final AtomicLong numSubscribers = new AtomicLong();

    /** The queue is the binary name of the event class as returned by {@link Class#getName()}, e.g. com.example.Events$OrderCreated. */
    public void register(String queue, Class<?> subscriber) {
        Objects.requireNonNull(queue, Required.QUEUE);
        Objects.requireNonNull(subscriber, Required.SUBSCRIBER);

        subscribers.put(queue, subscriber);
        numSubscribers.addAndGet(1);
    }

    /** Delivers the payload asynchronously to all subscribers of its class; subscriber failures are only logged. */
    @SuppressWarnings("all")
    public void publish(T payload) {
        Objects.requireNonNull(payload, Required.PAYLOAD);

        Thread.ofVirtual().start(() -> {
            String queue = payload.getClass().getName();
            for (Class<?> subscriber : subscribers.get(queue)) {
                try {
                    ((Subscriber) Application.getInstance(subscriber)).receive(payload);
                    handledEvents.addAndGet(1);
                } catch (Exception e) { //NOSONAR
                    LOG.error("Failed to send payload of queue '{}' to subscriber '{}'", queue, subscriber.getName(), e);
                }
            }
        });
    }

    public long getHandledEvents() {
        return handledEvents.longValue();
    }

    public long getNumberOfSubscribers() {
        return numSubscribers.longValue();
    }
}
