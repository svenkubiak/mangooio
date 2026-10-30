package io.mangoo.interfaces;

import java.util.function.Consumer;

public interface MessageBroker {
    void publish(String topic, String message);
    void subscribe(String topic, Consumer<String> consumer);
}
