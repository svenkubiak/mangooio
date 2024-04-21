package io.mangoo.async;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import io.mangoo.core.Application;
import io.mangoo.enums.Required;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class EventBusHandler<T> {
    private static final Logger LOG = LogManager.getLogger(EventBusHandler.class);
    private final ConcurrentMap<String, Class<?>> subscribers = new ConcurrentHashMap<>(16, 0.9f, 1);
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
     * @param queue The name of the queue (case-sensitive)
     * @param payload the playload to send
     */
    @SuppressWarnings("all")
    public void publish(String queue, T payload) {
        Objects.requireNonNull(queue, Required.QUEUE.toString());
        Objects.requireNonNull(payload, Required.PAYLOAD.toString());
        Preconditions.checkArgument(subscribers.containsKey(queue), Required.SUBSCRIBER.toString());

        Thread.ofVirtual().start(() -> {
            try {
                Class<?> subscriber = subscribers.get(queue);
                ((Subscriber) Application.getInstance(subscriber)).receive(payload);
            } catch (Exception e) { //NOSONAR
                LOG.error("Failed to send payload to queue '{}'", queue, e);
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
