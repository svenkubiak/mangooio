package io.mangoo.async;

import com.google.inject.Singleton;
import io.mangoo.core.Application;
import io.mangoo.enums.Required;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class EventBus<T> {
    private static final Logger LOG = LogManager.getLogger(EventBus.class);
    private final MultiValuedMap<String, Class<?>> subscribers = new ArrayListValuedHashMap<>();
    private final AtomicLong handledEvents = new AtomicLong();
    private final AtomicLong numSubscribers = new AtomicLong();

    /**
     * Register a subscriber class on a provided queue
     *
     * @param queue The name of the queue (case-sensitive)
     * @param subscriber The subscriber of the queue
     */
    public void register(String queue, Class<?> subscriber) {
        Objects.requireNonNull(queue, Required.QUEUE.toString());
        Objects.requireNonNull(subscriber, Required.SUBSCRIBER.toString());

        subscribers.put(queue, subscriber);
        numSubscribers.addAndGet(1);
    }

    /**
     * Publishes a payload to a queue which is then recieved
     * by all registered subscribers
     *
     * @param payload the playload to send
     */
    @SuppressWarnings("all")
    public void publish(T payload) {
        Objects.requireNonNull(payload, Required.PAYLOAD.toString());

        Thread.ofVirtual().start(() -> {
            String queue = payload.getClass().getCanonicalName();
            try {
                for (Class<?> subscriber : subscribers.get(queue)) {
                    ((Subscriber) Application.getInstance(subscriber)).receive(payload);
                    handledEvents.addAndGet(1);
                }
            } catch (Exception e) { //NOSONAR
                LOG.error("Failed to send payload to queue", e);
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
