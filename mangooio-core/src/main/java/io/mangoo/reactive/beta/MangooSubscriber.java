package io.mangoo.reactive.beta;

public interface MangooSubscriber<T> {
    void receive(T payload);
}
