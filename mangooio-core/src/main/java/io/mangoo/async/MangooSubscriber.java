package io.mangoo.async;

public interface MangooSubscriber<T> {
    void receive(T payload);
}
