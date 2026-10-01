package subscribers;

import io.mangoo.async.Subscriber;

public class NestedEventSubscriber implements Subscriber<NestedEventSubscriber.Event> {
    public static volatile String value; //NOSONAR

    public record Event(String value) {
    }

    @Override
    public void receive(Event payload) {
        value = payload.value(); //NOSONAR
    }
}
