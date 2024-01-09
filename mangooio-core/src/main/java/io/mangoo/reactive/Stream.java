package io.mangoo.reactive;

import com.google.inject.Singleton;
import io.mangoo.core.Application;
import io.mangoo.enums.Required;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class Stream<T> {
    private final AtomicInteger handledEvents = new AtomicInteger();
    private final AtomicInteger subscribers = new AtomicInteger();
    private final Map<String, List<SubmissionPublisher<T>>> publishers = new ConcurrentHashMap<>(16, 0.9f, 1);

    /**
     * Registers a subscriber within a given queue to receive events
     * <p>
     * One queue can have multiple (different) subscriber
     *
     * @param queue The name of the queue
     * @param clazz The Subscriber class
     */
    @SuppressWarnings("unchecked")
    public void register(String queue, Class<T> clazz) {
        Objects.requireNonNull(queue, Required.QUEUE.toString());
        Objects.requireNonNull(clazz, Required.CLASS.toString());

        SubmissionPublisher<T> publisher = new SubmissionPublisher<>(Executors.newVirtualThreadPerTaskExecutor(), Flow.defaultBufferSize());
        publisher.subscribe(Application.getInstance((Class<Flow.Subscriber<? super T>>) clazz));

        subscribers.incrementAndGet();
        publishers.computeIfAbsent(queue, k -> new CopyOnWriteArrayList<>()).add(publisher);
    }

    /**
     * Publishes the given event on a specific queue which afterward is asynchronously executed
     * by the registered subscribers
     *
     * @param queue The name of the queue
     * @param event The event to handle
     */
    public void publish(String queue, T event) {
        Objects.requireNonNull(queue, Required.QUEUE.toString());
        Objects.requireNonNull(event, Required.EVENT.toString());

        List<SubmissionPublisher<T>> receivers = publishers.get(queue);
        if (receivers != null) {
            receivers.forEach(receiver -> {
                receiver.submit(event);
                handledEvents.incrementAndGet();
            });
        }
    }

    /**
     * @return The number of handled events of all the streams
     */
    public int getHandledEvents() {
        return handledEvents.get();
    }

    /**
     * @return The number of all subscribers registered to all streams
     */
    public int getNumberOfSubscribers() {
        return subscribers.get();
    }

    /**
     * Closes the all subscribers on all queues
     */
    public void close() {
        publishers.forEach((k, v) -> {
            v.forEach(SubmissionPublisher::close);
        });
    }
}
