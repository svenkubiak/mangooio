package subscribers;

import io.mangoo.annotations.Async;
import io.mangoo.async.Subscriber;
import utils.Utils;

@Async
public class MySubscriber implements Subscriber<String> {
    @Override
    public void receive(String payload) {
        Utils.eventBusValue = payload; //NOSONAR
    }
}
