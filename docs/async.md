# Async

Long-running or fire-and-forget work should not block the HTTP thread. The **`EventBus`** publishes payloads to **`Subscriber`** implementations, and delivery runs on **virtual threads**, so you can write straightforward blocking code inside a subscriber without starving Undertow's worker pool.

Subscribers are discovered at startup through a classpath scan. Each subscriber type is tied to a **queue name equal to the payload class's canonical name**. There is no `unregister` method, so if you need multiple handlers for the same payload type, register additional subscribers by class instead.

## Subscribers

Implement `io.mangoo.async.Subscriber`, and mangoo I/O registers every implementation it finds on startup. The queue name is the **canonical name of the payload type**, so a `Subscriber<String>` registers under `java.lang.String`.

```java
package subscribers;

import io.mangoo.async.Subscriber;

public class AuditSubscriber implements Subscriber<String> {
    @Override
    public void receive(String payload) {
        // handle payload
    }
}
```

Subscribers are created through Guice, so you can inject dependencies.

## Publishing

```java
@Inject
private EventBus eventBus;

public void notify(String message) {
    eventBus.publish(message);
}
```

`publish` looks up subscribers for `payload.getClass().getCanonicalName()`, and again, there is no `unregister`. You can also call `eventBus.register("java.lang.String", AuditSubscriber.class)` yourself, though classpath scanning already does that for you for any `Subscriber` type.

Use a dedicated payload class if you need separate queues:

```java
public record OrderPlaced(String id) {}

public class OrderSubscriber implements Subscriber<OrderPlaced> {
    @Override
    public void receive(OrderPlaced payload) { }
}

eventBus.publish(new OrderPlaced("42"));
```
