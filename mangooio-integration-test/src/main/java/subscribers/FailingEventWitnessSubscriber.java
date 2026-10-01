package subscribers;

import io.mangoo.async.Subscriber;

public class FailingEventWitnessSubscriber implements Subscriber<FailingEvent> {
    public static volatile String value; //NOSONAR

    @Override
    public void receive(FailingEvent payload) {
        value = payload.value(); //NOSONAR
    }
}
