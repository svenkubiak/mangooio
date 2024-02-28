package io.mangoo.async;

public class MySubscriber implements Subscriber<String> {
    @Override
    public void receive(String payload) {
        EventBustTest.value = payload;
    }
}
