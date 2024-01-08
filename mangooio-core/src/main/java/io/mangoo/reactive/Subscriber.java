package io.mangoo.reactive;

import java.util.concurrent.Flow;

class Subscriber<T> implements Flow.Subscriber<T> {
    protected Flow.Subscription subscription;

    @Override
    public void onSubscribe(Flow.Subscription subscription) {
        this.subscription = subscription;
        subscription.request(Integer.MAX_VALUE);
    }

    @Override
    public void onNext(T item) {
    }

    @Override
    public void onError(Throwable error) {
    }

    @Override
    public void onComplete() {
    }
}
