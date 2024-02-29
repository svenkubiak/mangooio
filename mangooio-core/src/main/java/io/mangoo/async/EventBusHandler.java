package io.mangoo.async;

import com.google.common.base.Preconditions;
import com.google.inject.Singleton;
import com.softwaremill.jox.Channel;
import io.mangoo.core.Application;
import io.mangoo.enums.Required;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class EventBusHandler<T> {
    private static final Logger LOG = LogManager.getLogger(EventBusHandler.class);
    private final ConcurrentMap<String, Channel<?>> channels = new ConcurrentHashMap<>(16, 0.9f, 1);
    private final AtomicLong handledEvents = new AtomicLong();
    private final AtomicLong subscribers = new AtomicLong();

    /**
     * Register a subscriber class on a provided queue
     *
     * @param queue The name of the queue (case-sensitive)
     * @param subscriber The subscriber of the queue
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void register(String queue, Class<?> subscriber) {
        Objects.requireNonNull(queue, Required.QUEUE.toString());
        Objects.requireNonNull(subscriber, Required.SUBSCRIBER.toString());

        Channel<?> channel = channels.computeIfAbsent(queue, k -> new Channel<>(-1));
        Thread.ofVirtual().start(() -> {
            try {
                do {
                    var payload = channel.receive();
                    ((Subscriber) Application.getInstance(subscriber)).receive(payload);
                } while (true);
            } catch (InterruptedException e) { //NOSONAR
                LOG.error("EventBus queue was interrupted", e);
            }
        });
        subscribers.addAndGet(1);
    }

    /**
     * Publishes a payload to a queue which is then recieved
     * by all registered subscribers
     *
     * @param queue The name of the queue (case-insensitive)
     * @param payload the playload to send
     */
    @SuppressWarnings("all")
    public void publish(String queue, T payload) {
        Objects.requireNonNull(queue, Required.QUEUE.toString());
        Objects.requireNonNull(payload, Required.PAYLOAD.toString());
        Preconditions.checkArgument(channels.containsKey(queue), "queue '" + queue + "' does not exist");

        Channel channel = channels.get(queue);
        if (channel != null) {
            try {
                channel.send(payload);
                handledEvents.addAndGet(1);
            } catch (InterruptedException e) { //NOSONAR
                LOG.error("Failed to send payload to queue '" + queue + "'", e);
            }
        }
    }

    public void shutdown() {
        for (Map.Entry<String, Channel<?>> entry : channels.entrySet()) entry.getValue().done();
    }

    public long getHandledEvents() {
        return handledEvents.longValue();
    }

    public long getNumberOfSubscribers() {
        return subscribers.longValue();
    }
}
