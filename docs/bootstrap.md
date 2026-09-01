# Bootstrap

The bootstrap class is the main extension point for application wiring. It implements `MangooBootstrap` and is bound in `app.Module`. Most projects use a class named `Bootstrap` in the `app` package, as the archetype does.

You use it for three kinds of work:

1. **Routes** — `initializeRoutes()` registers every URL before the server accepts traffic.
2. **Startup and shutdown** — Optional hooks run after configuration is loaded, after connectors are listening, and when the JVM shuts down.
3. **Cross-cutting setup** — For example global security headers via `Server.header(...)` at the start of `initializeRoutes()`.

Because `Bootstrap` is a normal Guice-managed type, you can inject services (such as `Datastore`) and use them in `applicationStarted()` to seed data or warm caches.

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
        // After config load and Guice injector creation
    }

    @Override
    public void applicationStarted() {
        // HTTP/HTTPS listeners are up
    }

    @Override
    public void applicationStopped() {
        // JVM shutdown
    }
}
```

The class name is arbitrary; it must implement `MangooBootstrap` and be bound in `app.Module`:

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

Put extra Guice bindings in the same `configure()` method. See [Dependency injection](dependency-injection.md) and [Routing](routing.md).

`initializeRoutes()` is also a good place for [global response headers](operating.md):

```java
Server.header(Header.CONTENT_SECURITY_POLICY, "default-src 'self'");
```
