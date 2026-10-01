package io.mangoo.async;

public interface Subscriber<T> {
    void receive(T payload);
}
