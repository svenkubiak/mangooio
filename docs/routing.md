# Routing

Map request URLs to controller methods in `app.Bootstrap`, in `initializeRoutes()`.

```java
import controllers.ApplicationController;
import io.mangoo.routing.Bind;
import io.mangoo.routing.On;

@Override
public void initializeRoutes() {
    Bind.controller(ApplicationController.class).withRoutes(
        On.get().to("/").respondeWith("index")
    );
}
```

A `GET` request to `/` calls `ApplicationController.index()`.

!!! note
    The method name on the route builder is `respondeWith` (with an "e"). That is the public API.

## HTTP methods

```java
On.get()
On.post()
On.put()
On.patch()
On.delete()
On.options()
On.head()
On.anyOf(Http.PUT, Http.PATCH, Http.DELETE)
```

`On.anyOf(...)` binds one controller method to several verbs.

## Long-running requests

Undertow handles I/O without blocking. If a controller method does blocking work (JDBC, a slow HTTP call, heavy computation), mark the route so it runs on a separate thread pool:

```java
Bind.controller(ApplicationController.class).withRoutes(
    On.get().to("/export").respondeWith("export").withNonBlocking()
);
```

You can also apply that to every route of a controller:

```java
Bind.controller(ExportController.class).withNonBlocking().withRoutes(
    On.get().to("/export").respondeWith("index")
);
```

Despite the name, `withNonBlocking()` marks the route as **blocking work** so the non-blocking I/O threads stay free.

## Authentication

Require a valid authentication cookie on a controller or on a single route. See [Authentication](authentication.md).

```java
Bind.controller(DashboardController.class).withAuthentication().withRoutes(
    On.get().to("/dashboard").respondeWith("index")
);

Bind.controller(LoginController.class).withRoutes(
    On.get().to("/account").respondeWith("account").withAuthentication(),
    On.get().to("/login").respondeWith("login")
);
```

## Static files

Files under `src/main/resources/files` are served after you bind them:

```java
Bind.pathResource().to("/assets/");
Bind.fileResource().to("/robots.txt");
```

That maps to:

```
src/main/resources/files/assets/
src/main/resources/files/robots.txt
```

## Server-Sent Events

SSE routes do not use a controller:

```java
Bind.serverSentEvent().to("/sse");
Bind.serverSentEvent().to("/sseauth").withAuthentication();
```

An authenticated SSE route requires a valid authentication cookie. See [Server-Sent Events](sse.md).

## WebSockets

WebSocket routes use an Undertow `WebSocketConnectionCallback`:

```java
Bind.webSocket().to("/ws").withHandler(MyWebSocketHandler.class);
```

There is no dedicated test helper for WebSockets. Treat this as an available API, not a fully documented product feature.
