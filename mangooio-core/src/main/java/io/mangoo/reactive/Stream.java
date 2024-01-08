package io.mangoo.reactive;

import com.google.inject.Singleton;
import io.mangoo.core.Application;

import java.util.Objects;
import java.util.concurrent.Flow.Subscriber;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicInteger;

@Singleton
public class Stream<T> {
    private final SubmissionPublisher<T> publisher = new SubmissionPublisher<>();
    private final AtomicInteger handledEvents = new AtomicInteger();

    /**
     * Registers a subscriber on the stream to receive events
     * @param clazz The Subscriber class
     */
    @SuppressWarnings("unchecked")
    public void register(Class<T> clazz) {
        Objects.requireNonNull(clazz, "clazz can not be null");

        publisher.subscribe(Application.getInstance((Class<Subscriber<? super T>>) clazz));
    }

    /**
     * Publishes the given event to the stream which afterward is asynchronously executed
     *
     * @param event The event to handle
     */
    public void publish(T event) {
        Objects.requireNonNull(event, "event can not be null");

        publisher.submit(event);
        handledEvents.incrementAndGet();
    }

    /**
     * @return The number of handled events of the stream
     */
    public int getHandledEvents() {
        return handledEvents.get();
    }

    /**
     * @return The number of all subscribers registered to this stream
     */
    public int getNumberOfSubscribers() {
        return publisher.getNumberOfSubscribers();
    }

    /**
     * Closes the Stream and hence closing all subscribers
     */
    public void close() {
        publisher.close();
    }
}
