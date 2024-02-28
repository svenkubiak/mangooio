package io.mangoo.async;

public class MySubscriber implements MangooSubscriber<String> {
    @Override
    public void receive(String payload) {
        EventBustTest.value = payload;
    }
}
