# Server-Sent Events

mangoo I/O provides built-in support for Server-Sent Events (SSE), enabling real-time one-way communication from the server to the client.

## Routing

To set up Server-Sent Events, define a route for the SSE endpoint in your `Bootstrap` class:

```java
Bind.serverSentEvent().to("/sse");
```

## Sending Data

Once the routing is configured, you can send Server-Sent Events from anywhere in your application using the `ServerSentEventManager`:

```java
private final ServerSentEventManager sse;

@Inject
public MyClass(ServerSentEventManager sse) {
    this.sse = Objects.requireNonNull(sse, "sse cannot be null");
}

public void sendData() {
    sse.send("/sse", "data");
}
```

### Data Format

The data must be a string, such as a JSON object, and can be formatted according to your needs.

For more details on setting up Server-Sent Events on the client side, refer to the [Mozilla Developer Documentation](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events).