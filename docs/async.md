# Async

mangoo I/O supports a simple `EventBus` mechanism, allowing asynchronous event handling. To start using the `EventBus`, inject it into your logic:

```java
@Inject
private EventBus eventBus;
```

## EventBus Functions

The `EventBus` provides essential functions for event handling, including registering, unregistering, and publishing events. Example usage:

```java
MyListener myListener = new MyListener();
eventBus.register("queueName", myListener);
eventBus.publish("This is a test event");
eventBus.unregister(myListener);
```

## Subscriber Implementation

To receive events, a subscriber class must implement the `Subscriber` interface:

```java
public class MySubscriber implements Subscriber<String> {
    @Override
    public void receive(String payload) {
        // Handle received event data
    }
}
```

### Payload Handling

The payload type depends on the data being passed to the receiver. In this example, a `String` is used, but other data types can be utilized as needed.
