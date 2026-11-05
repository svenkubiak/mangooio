# Bootstrap 🔧

The bootstrap class is the main extension point for application wiring: it is where routes get registered and where you hook into startup and shutdown. It implements `MangooBootstrap` and is bound in `app.Module`. Most projects use a class named `Bootstrap` in the `app` package, since that is what the archetype generates, but nothing forces that name on you (see [Getting started](getting-started.md) for the generated version).

You use it for three kinds of work:

1. **Routes.** `initializeRoutes()` registers every URL before the server starts accepting traffic, so by the time the first request can possibly arrive, the whole route table already exists.
2. **Startup and shutdown.** Hooks (all four must be implemented, but may stay empty) run after configuration is loaded, after connectors are listening, and when the JVM shuts down, giving you three distinct moments to plug into instead of one big "app started" event.
3. **Cross-cutting setup.** For example, global security headers via `Server.header(...)` at the start of `initializeRoutes()`. They are copied onto controller responses, the 404 page, error pages, and authentication redirects. They are not applied to SSE, WebSocket handshakes, static files, or the early 400 for a bad query string. A blank value is not sent, which is why the built-in `Content-Security-Policy` stays absent until you set one.

Because `Bootstrap` is a normal Guice-managed type, you can inject services (a `Datastore`, a cache, your own service class) and use them in `applicationStarted()` to seed data or warm caches, exactly the way the archetype's sample app seeds a few `Person` records.

```java
package app;

import io.mangoo.interfaces.MangooBootstrap;

public class Bootstrap implements MangooBootstrap {

    @Override
    public void initializeRoutes() {
        // Bind.controller(...).withRoutes(...)
    }

    @Override
    public void applicationInitialized() {
        // Config is loaded and the Guice injector exists,
        // but no routes are registered and no connector is listening yet
    }

    @Override
    public void applicationStarted() {
        // HTTP/HTTPS listeners are up and accepting requests;
        // a good place to seed data or warm a cache
    }

    @Override
    public void applicationStopped() {
        // JVM shutdown signal received;
        // close whatever you opened yourself (a connection pool, a file handle)
    }
}
```

The four methods run in this order: `applicationInitialized()`, `initializeRoutes()`, `applicationStarted()`, `applicationStopped()`. `applicationInitialized()` runs before config validation and before any route exists. `applicationStarted()` runs after the connectors are listening, and too late to change the route table. `applicationStopped()` runs after Undertow has stopped, while the MongoDB clients are still open. The full sequence, including the classpath scan that overlaps `initializeRoutes()`, is in [Getting started](getting-started.md#what-happens-on-startup).

The bootstrap class name is arbitrary. It has to implement `MangooBootstrap` and be bound in `app.Module`. The module class itself is not arbitrary: the framework loads `app.Module` by name.

```java
package app;

import com.google.inject.AbstractModule;
import io.mangoo.interfaces.MangooBootstrap;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MangooBootstrap.class).to(Bootstrap.class);
    }
}
```

That single `bind(...)` line is how mangoo I/O locates your application at startup, so if you ever rename your bootstrap class, this is the one place you have to remember to update. Put extra Guice bindings in the same `configure()` method rather than scattering them elsewhere. See [Dependency injection](dependency-injection.md) and [Routing](routing.md).

`initializeRoutes()` is also a convenient place for [global response headers](operating.md), since it runs exactly once, before any request handling starts:

```java
Server.header(Header.CONTENT_SECURITY_POLICY, "default-src 'self'");
Server.header(Header.X_FRAME_OPTIONS, "DENY");
```

Use this for headers you want on ordinary pages. SSE, static files, and WebSocket handshakes do not pick them up. See [Operating](operating.md#global-response-headers) for the headers the framework already sends.
