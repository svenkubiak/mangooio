package subscribers;

import io.mangoo.async.Subscriber;

public class HelperFirstSubscriber implements Subscriber<HelperFirstEvent> {
    public static volatile String value; //NOSONAR

    // Intentionally declared before receive, the subscriber must be registered nevertheless
    private static String normalize(String value) {
        return value.trim();
    }

    @Override
    public void receive(HelperFirstEvent payload) {
        value = normalize(payload.value()); //NOSONAR
    }
}
