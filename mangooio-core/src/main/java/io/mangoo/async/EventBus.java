package io.mangoo.async;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class EventBus<T> {
    private static final Logger LOG = LogManager.getLogger(EventBus.class);
    private final Multimap<String, Class<?>> subscribers = ArrayListMultimap.create();
    private final AtomicLong handledEvents = new AtomicLong();
    private final AtomicLong numSubscribers = new AtomicLong();
    private final AtomicLong failedEvents = new AtomicLong();

    public void register(String queue, Class<?> subscriber) {
        Objects.requireNonNull(queue, Required.QUEUE);
        Objects.requireNonNull(subscriber, Required.SUBSCRIBER);

        subscribers.put(queue, subscriber);
        numSubscribers.addAndGet(1);
    }

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
                    failedEvents.incrementAndGet();
                    LOG.error("Failed to send payload of queue '{}' to subscriber '{}'", queue, subscriber.getName(), e);
                }
            }
        });
    }

    public long getHandledEvents() {
        return handledEvents.longValue();
    }

    public long getFailedEvents() {
        return failedEvents.longValue();
    }

    /** Registered subscriber classes by queue, sorted by queue name. */
    public Map<String, List<String>> getSubscribers() {
        Map<String, List<String>> queues = new TreeMap<>();
        subscribers.asMap().forEach((queue, classes) -> queues.put(queue, classes.stream().map(Class::getName).toList()));

        return queues;
    }

    public long getNumberOfSubscribers() {
        return numSubscribers.longValue();
    }
}
