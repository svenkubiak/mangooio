package reactive;

import io.mangoo.reactive.Subscriber;
import utils.Utils;

public class MySubscriber extends Subscriber<String> {
    @Override
    public void onNext(String item) {
        Utils.value = item;
    }
}
