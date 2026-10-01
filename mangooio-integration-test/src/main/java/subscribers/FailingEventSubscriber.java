package subscribers;

import io.mangoo.async.Subscriber;

// Registered before FailingEventWitnessSubscriber, as subscribers are registered ordered by class name
public class FailingEventSubscriber implements Subscriber<FailingEvent> {
    @Override
    public void receive(FailingEvent payload) {
        throw new IllegalStateException("Intentionally failing subscriber");
    }
}
