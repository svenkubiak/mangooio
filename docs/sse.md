# Server-Sent Events

**Server-Sent Events (SSE)** let the server push updates to the browser over a long-lived HTTP connection. Unlike WebSockets, traffic is **one-way** (server → client) and uses ordinary HTTP, which plays well with proxies and load balancers that struggle with upgrade headers.

mangoo I/O maps SSE endpoints in routing and sends events from your own controller or service code. For bidirectional or binary protocols, reach for WebSockets instead, see the note in [Routing](routing.md).

## Routing

```java
Bind.serverSentEvent().to("/sse");
```

!!! warning "SSE endpoints are not authenticated"
    SSE routes are registered directly on Undertow's path handler and therefore bypass the framework's handler chain entirely, including the authentication check that `withAuthentication()` provides for controller routes. There is no `withAuthentication()` on an SSE route, and none of the framework's access control applies to one.

    Every SSE endpoint you bind is reachable by anyone who knows its URL, and that URL is usually visible in your client-side JavaScript. On top of that, `ServerSentEventManager` groups connections by request URI rather than by user, so a single event sent to a path reaches *every* client connected to it. Treat an SSE stream as a public broadcast channel and do not push anything over it that is specific to one user or that you would not serve from an unauthenticated endpoint.

    If you need a per-user stream, the practical workaround today is to give each user their own unguessable path (for example one derived from a server-issued token) and send to that path only. That is obscurity, not authentication, so pair it with short-lived tokens.

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

The first argument is the route URL the event goes to, and the payload is a plain string. Delivery runs on a virtual thread, so `sendData()` returns immediately without blocking on connected clients.

Client setup: [MDN Server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events).
