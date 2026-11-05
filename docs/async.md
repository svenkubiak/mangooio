# Async

The **`EventBus`** publishes payloads to **`Subscriber`** implementations. `publish` returns immediately. Delivery of one event runs on a single virtual thread, and the subscribers for that payload are called one after another. A slow subscriber delays the next subscriber of the same event. It does not block the publisher, and it does not run on an Undertow thread.

Subscribers are discovered at startup. The queue name is `payload.getClass().getName()`. A subclass does not reach a subscriber registered for a parent type. A raw `Subscriber` or `Subscriber<Object>` is skipped and logged. There is no `unregister`. Register another class when you need a second handler for the same payload.

## Subscribers

Implement `io.mangoo.async.Subscriber<T>`, and mangoo I/O registers every implementation it finds on startup. The payload type is taken from the type argument `T`, so declare it explicitly; a raw `Subscriber` is not registered and logged as a warning. The queue name is the **binary name of the payload type**, so a `Subscriber<String>` registers under `java.lang.String` and a subscriber for a nested record `Events.OrderPlaced` under `com.example.Events$OrderPlaced`.

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

Subscribers are created through Guice, so you can inject dependencies. Every subscriber of a payload is called independently, an exception thrown by one subscriber is logged and does not prevent the others from receiving the payload.

## Publishing

```java
@Inject
private EventBus eventBus;

public void notify(String message) {
    eventBus.publish(message);
}
```

`publish` looks up subscribers for `payload.getClass().getName()`. You can also call `eventBus.register("java.lang.String", AuditSubscriber.class)`. Classpath scanning already does that for any `Subscriber` it finds. `getHandledEvents()`, `getFailedEvents()`, `getSubscribers()`, and `getNumberOfSubscribers()` are what the admin dashboard reads.

`EventBus` stays on the publishing JVM. Cross-instance messages go through `io.mangoo.interfaces.MessageBroker`:

```java
void publish(String topic, String message);
void subscribe(String topic, Consumer<String> consumer);
```

With persistence that is the capped collection `mangooio_messages`. Without it, subscribers on this JVM only. A message published while the tail is disconnected is not replayed. SSE and the admin cache-clear action use this broker. See [Operating](operating.md#running-several-instances).

Use a dedicated payload class if you need separate queues:

```java
public record OrderPlaced(String id) {}

public class OrderSubscriber implements Subscriber<OrderPlaced> {
    @Override
    public void receive(OrderPlaced payload) { }
}

eventBus.publish(new OrderPlaced("42"));
```
