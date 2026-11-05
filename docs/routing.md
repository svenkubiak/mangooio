# Routing 🧭

Routing connects incoming URLs to controller methods. In mangoo I/O every route is declared in code, typically in `app.Bootstrap.initializeRoutes()`, so the URL map always matches what is actually compiled into the application. There is no separate routes file that can silently drift out of sync with the controller it is supposed to point at.

You use two builders:

- **`On`**: HTTP verb and path for a controller method, for example `On.get().to("/users").respondeWith("list")`.
- **`Bind`**: Groups routes for a controller, or registers static files, SSE endpoints, and WebSockets.

Once a route matches, the framework invokes the corresponding method on a Guice-managed controller instance. Path segments in `{braces}` become method parameters, see [Controllers](controllers.md) for how those get bound.

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
    The method name on the route builder is `respondeWith` (with an "e"). It looks like a typo the first time you see it, but it is the actual public API, so autocomplete will not save you here.

A route with a path parameter looks like this:

```java
Bind.controller(UserController.class).withRoutes(
    On.get().to("/users/{id}").respondeWith("show")
);
```

```java
public Response show(long id) {
    return Response.ok().render("id", id);
}
```

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

`On.anyOf(...)` binds one controller method to several verbs at once, useful when the same handler legitimately needs to react to more than one, for example a `PUT`/`PATCH` pair that both update a resource but differ only in whether the body is a full or partial representation.

## Long-running requests

`POST`, `PUT`, and `PATCH` already leave Undertow's I/O thread before the controller runs, body and all. `GET`, `HEAD`, `DELETE`, and `OPTIONS` stay on the I/O thread unless you mark the route. A slow read on that thread holds up other requests on the same thread. `withNonBlocking()` dispatches those verbs onto Undertow's worker threads. The name describes the I/O threads you are keeping free, not the work inside the method:

```java
Bind.controller(ApplicationController.class).withRoutes(
    On.get().to("/export").respondeWith("export").withNonBlocking()
);
```

You can also apply that to every route of a controller at once:

```java
Bind.controller(ExportController.class).withNonBlocking().withRoutes(
    On.get().to("/export").respondeWith("index")
);
```

Calling it on `On.post()`, `On.put()`, or `On.patch()` changes nothing. Those verbs are dispatched either way. A slow `GET` without it does not fail. Other requests on that I/O thread wait behind it.

## Authentication

Require a valid authentication cookie on a controller or on a single route. See [Authentication](authentication.md) for how the cookie itself gets issued and verified.

```java
Bind.controller(DashboardController.class).withAuthentication().withRoutes(
    On.get().to("/dashboard").respondeWith("index")
);

Bind.controller(LoginController.class).withRoutes(
    On.get().to("/account").respondeWith("account").withAuthentication(),
    On.get().to("/login").respondeWith("login")
);
```

Putting `withAuthentication()` on the controller protects every route it declares; putting it on a single route lets one controller mix public and protected pages, like the `LoginController` example above, where `/login` has to stay reachable by definition.

## Static files

Files under `src/main/resources/files` are served once you bind them explicitly:

```java
Bind.pathResource().to("/assets/");
Bind.fileResource().to("/robots.txt");
```

That maps to:

```
src/main/resources/files/assets/
src/main/resources/files/robots.txt
```

`pathResource()` serves a directory tree. The prefix is forced to start and end with `/`, and the files are read from the classpath directory `files` plus that prefix. `fileResource()` maps one file and is registered as `GET` only. Nothing under `files/` is served unless you bind it. These routes skip authentication, filters, CSRF, and `Server.header(...)`.

## Server-Sent Events

SSE routes do not use a controller at all, since there is no request/response cycle to hand off to one:

```java
Bind.serverSentEvent().to("/sse");
```

An SSE route optionally takes its own Undertow `ServerSentEventConnectionCallback`, which replaces the default handler and lets the application inspect, reject, or individually key a connection as it is established:

```java
Bind.serverSentEvent().to("/sse/client").withHandler(MyServerSentEventHandler.class);
```

See [Server-Sent Events](sse.md) for how to push data to connected clients afterward, and for what a custom handler has to take care of itself.

!!! warning "SSE and WebSocket routes are not authenticated"
    Both are registered directly on Undertow's path handler, which means they never pass through the handler chain described above. `withAuthentication()` does not exist on them, and no filter, authentication check, or CSRF protection applies. Anyone who knows the URL can connect.

    A custom connection handler is the place to check a token and refuse the handshake. Without that, anything you send is public. Keep user-specific data on a controller route.

## WebSockets

WebSocket routes use an Undertow `WebSocketConnectionCallback` directly, rather than a mangoo I/O abstraction on top of it:

```java
Bind.webSocket().to("/ws").withHandler(MyWebSocketHandler.class);
```

`withHandler` is required. The handler class is created by Guice, and a missing handler fails startup. There is no test helper for WebSockets or SSE. The handler has to read the authentication cookie off the handshake itself. Connections are tracked and closed on shutdown. There is no cross-instance fan-out. If a message has to reach clients on other instances, publish it on `MessageBroker` and forward it from the subscriber on each instance. See [Operating](operating.md#running-several-instances).
