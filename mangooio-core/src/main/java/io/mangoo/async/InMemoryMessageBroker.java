package io.mangoo.async;

import io.mangoo.constants.Required;
import io.mangoo.interfaces.MessageBroker;
import io.mangoo.utils.Argument;
import jakarta.inject.Singleton;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

@Singleton
public class InMemoryMessageBroker implements MessageBroker {
    private static final Logger LOG = LogManager.getLogger(InMemoryMessageBroker.class);
    private final Map<String, List<Consumer<String>>> subscribers = new ConcurrentHashMap<>();

    @Override
    public void publish(String topic, String message) {
        Argument.requireNonBlank(topic, Required.TOPIC);
        Objects.requireNonNull(message, Required.MESSAGE);

        deliver(subscribers, topic, message);
    }

    @Override
    public void subscribe(String topic, Consumer<String> consumer) {
        Argument.requireNonBlank(topic, Required.TOPIC);
        Objects.requireNonNull(consumer, Required.CONSUMER);

        subscribers.computeIfAbsent(topic, key -> new CopyOnWriteArrayList<>()).add(consumer);
    }

    public static void deliver(Map<String, List<Consumer<String>>> subscribers, String topic, String message) {
        for (Consumer<String> consumer : subscribers.getOrDefault(topic, List.of())) {
            Thread.ofVirtual().start(() -> {
                try {
                    consumer.accept(message);
                } catch (RuntimeException e) {
                    LOG.error("Failed to deliver a message of topic '{}'", topic, e);
                }
            });
        }
    }
}
