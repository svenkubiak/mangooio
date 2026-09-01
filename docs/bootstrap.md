# Bootstrap

Hook into startup and shutdown by implementing `MangooBootstrap` in the `app` package (the archetype class is `app.Bootstrap`).

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
