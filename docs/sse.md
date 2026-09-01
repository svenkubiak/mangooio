# Server-Sent Events

SSE is one-way: the server pushes text (often JSON) to the browser.

## Routing

```java
Bind.serverSentEvent().to("/sse");
Bind.serverSentEvent().to("/sseauth").withAuthentication();
```

Authenticated endpoints require a valid authentication cookie. See [Routing](routing.md) and [Authentication](authentication.md).

## Sending

```java
import io.mangoo.manager.ServerSentEventManager;
import jakarta.inject.Inject;

public class NotifyService {
    private final ServerSentEventManager sse;

    @Inject
    public NotifyService(ServerSentEventManager sse) {
        this.sse = sse;
    }

    public void sendData() {
        sse.send("/sse", "{\"status\":\"ok\"}");
    }
}
```

The first argument is the route URL. The payload is a string. Delivery runs on a virtual thread.

Client setup: [MDN Server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events).
