package subscribers;

import io.mangoo.async.Subscriber;
import utils.Utils;

public class MySubscriber implements Subscriber<String> {
    @Override
    public void receive(String payload) {
        Utils.eventBusValue = payload;
    }
}
