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
public Response show(@Param("id") long id) {
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

Undertow handles I/O without blocking by default, which is great for throughput but means every controller method normally runs on a small pool of I/O threads shared by all requests. If a controller method does blocking work (a JDBC call, a slow outbound HTTP request, heavy computation), mark the route so it runs on a separate thread pool instead:

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

Despite the name, `withNonBlocking()` marks the route as containing **blocking work**. The name describes the effect on the rest of the application, not the route itself: it keeps mangoo I/O's non-blocking I/O threads free by moving your (blocking) code off of them. If you forget to set this on a route that does something slow, you will not see an error, you will just see every other request on the same I/O thread queue up behind it, which tends to show up as an odd, hard-to-explain latency spike under load rather than a crash.

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

`pathResource()` serves an entire directory tree under a URL prefix, while `fileResource()` maps a single file to a single URL. Nothing under `files/` is served unless you bind it, which avoids accidentally exposing a directory just because a file happened to land there.

## Server-Sent Events

SSE routes do not use a controller at all, since there is no request/response cycle to hand off to one:

```java
Bind.serverSentEvent().to("/sse");
Bind.serverSentEvent().to("/sseauth").withAuthentication();
```

An authenticated SSE route requires a valid authentication cookie, checked once when the connection is opened. See [Server-Sent Events](sse.md) for how to push data to connected clients afterward.

## WebSockets

WebSocket routes use an Undertow `WebSocketConnectionCallback` directly, rather than a mangoo I/O abstraction on top of it:

```java
Bind.webSocket().to("/ws").withHandler(MyWebSocketHandler.class);
```

There is no dedicated test helper for WebSockets, unlike SSE or regular controller routes. Treat this as an available, working API rather than a fully documented, polished product feature: it gets you access to Undertow's WebSocket support without extra plumbing, but you are closer to the underlying library here than anywhere else in the framework.
