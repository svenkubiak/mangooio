# Async

Long-running or fire-and-forget work should not block the HTTP thread. The **`EventBus`** publishes payloads to **`Subscriber`** implementations; delivery runs on **virtual threads** so you can write straightforward blocking code without starving Undertow workers.

Subscribers are discovered at startup (classpath scan). Each subscriber type is tied to a **queue name equal to the payload class canonical name**. There is no `unregister`—register additional subscribers by class if you need multiple handlers for the same payload type.

## Subscribers

Implement `io.mangoo.async.Subscriber`. On startup, mangoo I/O registers every implementation. The queue name is the **canonical name of the payload type** (`java.lang.String` for `Subscriber<String>`).

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

`publish` looks up subscribers for `payload.getClass().getCanonicalName()`. There is no `unregister`. You can also call `eventBus.register("java.lang.String", AuditSubscriber.class)` yourself; classpath scanning already does that for `Subscriber` types.

Use a dedicated payload class if you need separate queues:

```java
public record OrderPlaced(String id) {}

public class OrderSubscriber implements Subscriber<OrderPlaced> {
    @Override
    public void receive(OrderPlaced payload) { }
}

eventBus.publish(new OrderPlaced("42"));
```
