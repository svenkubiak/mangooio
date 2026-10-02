# Server-Sent Events

**Server-Sent Events (SSE)** let the server push updates to the browser over a long-lived HTTP connection. Unlike WebSockets, traffic is **one-way** (server → client) and uses ordinary HTTP, which plays well with proxies and load balancers that struggle with upgrade headers.

mangoo I/O maps SSE endpoints in routing and sends events from your own controller or service code. For bidirectional or binary protocols, reach for WebSockets instead, see the note in [Routing](routing.md).

## Routing

```java
Bind.serverSentEvent().to("/sse");
```

!!! warning "SSE endpoints are not authenticated"
    SSE routes are registered directly on Undertow's path handler and therefore bypass the framework's handler chain entirely, including the authentication check that `withAuthentication()` provides for controller routes. There is no `withAuthentication()` on an SSE route, and none of the framework's access control applies to one.

    Every SSE endpoint you bind is reachable by anyone who knows its URL, and that URL is usually visible in your client-side JavaScript. On top of that, the default handler groups connections by request URI rather than by user, so a single event sent to a path reaches *every* client connected to it. Treat an SSE stream bound this way as a public broadcast channel and do not push anything over it that is specific to one user or that you would not serve from an unauthenticated endpoint.

    A [custom connection handler](#custom-connection-handler) is the way out of both halves of this: it sees the connection while it is being established and can check a token off the query string or a header, reject the connection, and key the ones it accepts per user instead of per path. The framework still does not authenticate for you, but it no longer stands in the way of doing it yourself.

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

The first argument is the key the connections are held under, which for the default handler is the route URL, and the payload is a plain string. Delivery runs on a virtual thread, so `sendData()` returns immediately without blocking on connected clients.

## Custom connection handler

Everything above is what the default handler gives you: every connection of a route lands under the route URL, and `send()` is a broadcast to all of them. When you need to look at a connection before accepting it, or address a single client rather than a whole path, pass your own Undertow `ServerSentEventConnectionCallback` to the route:

```java
Bind.serverSentEvent().to("/sse/client").withHandler(MyServerSentEventHandler.class);
```

`withHandler` is optional. A route without it behaves exactly as described above, so this changes nothing for endpoints you already have.

The handler is instantiated by Guice, so it can inject whatever it needs. It replaces the default handler completely, which means it owns everything the default one did:

* **Registering the connection**, or deliberately not registering it. There is no automatic registration behind your back, so a connection your handler drops on the floor is simply never addressable.
* **Attaching a close task**, so the connection is removed again when the client goes away. `ServerSentEventCloseListener` removes a connection by its request URI, so it stays usable as long as you register under `connection.getRequestURI()`. Under any other key, the close task has to call `removeConnection(key, connection)` itself.

```java
public class MyServerSentEventHandler implements ServerSentEventConnectionCallback {
    @Override
    public void connected(ServerSentEventConnection connection, String lastEventId) {
        Deque<String> tokens = connection.getQueryParameters().get("token");
        String user = tokens == null ? null : authenticate(tokens.peekFirst());

        if (user == null) {
            connection.shutdown();
            return;
        }

        ServerSentEventManager manager = Application.getInstance(ServerSentEventManager.class);
        manager.addConnection(user, connection);
        connection.addCloseTask(closed -> manager.removeConnection(user, closed));
    }
}
```

The connection is now held under the user rather than under `/sse/client`, so `send("someUser", data)` reaches that one user and nobody else. Everything a key holds is still a list, so a user connected from two tabs receives the event on both.

`connected()` runs on an I/O thread, so keep it short and move anything blocking - a database lookup for the token, for example - onto a virtual thread, the way the default handler does.

## Proxies and load balancers

Every SSE route, including one with a custom handler, is prepared for running behind a reverse proxy such as nginx:

* The response carries `X-Accel-Buffering: no`, so nginx passes each event on immediately instead of buffering it, and `Cache-Control: no-cache`.
* A comment line is sent every 30 seconds while a connection is otherwise idle. Clients ignore comments, but the traffic keeps proxies and load balancers from closing the connection after their idle timeout.
* The response headers are sent as soon as the connection is established, so the client's `onopen` fires right away. A handler does not need to send anything on connect.

What the application cannot set is the proxy's own connection handling. For nginx, use HTTP/1.1 towards the upstream and keep `proxy_read_timeout` above the 30 second heartbeat (the default of 60 seconds is fine):

```nginx
location /sse {
    proxy_pass http://app;
    proxy_http_version 1.1;
    proxy_set_header Connection "";
}
```

Client setup: [MDN Server-sent events](https://developer.mozilla.org/en-US/docs/Web/API/Server-sent_events/Using_server-sent_events).
