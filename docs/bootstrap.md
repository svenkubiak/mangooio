# Bootstrap 🔧

The bootstrap class is the main extension point for application wiring: it is where routes get registered and where you hook into startup and shutdown. It implements `MangooBootstrap` and is bound in `app.Module`. Most projects use a class named `Bootstrap` in the `app` package, since that is what the archetype generates, but nothing forces that name on you (see [Getting started](getting-started.md) for the generated version).

You use it for three kinds of work:

1. **Routes.** `initializeRoutes()` registers every URL before the server starts accepting traffic, so by the time the first request can possibly arrive, the whole route table already exists.
2. **Startup and shutdown.** Optional hooks run after configuration is loaded, after connectors are listening, and when the JVM shuts down, giving you three distinct moments to plug into instead of one big "app started" event.
3. **Cross-cutting setup.** For example, global security headers via `Server.header(...)` at the start of `initializeRoutes()`, which then apply to every response the application sends.

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

The four methods run in the order they appear above, which matters if you are deciding where to put something: `applicationInitialized()` runs too early to register anything that depends on routes existing, and `applicationStarted()` runs too late to influence routing, since by then the server is already serving traffic.

The class name is arbitrary; it only has to implement `MangooBootstrap` and be bound in `app.Module`:

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

Headers set this way apply to every response the application sends, so they are a good fit for security headers you want on every page, rather than something you would want to repeat in every controller method.
