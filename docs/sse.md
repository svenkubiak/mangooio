# Server-Sent Events

**Server-Sent Events (SSE)** let the server push updates to the browser over a long-lived HTTP connection. Unlike WebSockets, traffic is **one-way** (server → client) and uses ordinary HTTP, which plays well with proxies and load balancers that struggle with upgrade headers.

mangoo I/O maps SSE endpoints in routing and sends events from controller code. Authenticated streams require a valid auth cookie on the initial connection. For bidirectional or binary protocols, see the WebSocket note in [Routing](routing.md).

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
