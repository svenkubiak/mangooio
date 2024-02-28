package io.mangoo.reactive;

import io.mangoo.reactive.beta.MangooSubscriber;

public class MySubscriber implements MangooSubscriber<String> {
    @Override
    public void receive(String payload) {
        EventBustTest.value = payload;
    }
}
