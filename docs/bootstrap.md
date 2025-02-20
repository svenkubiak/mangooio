In some cases it is useful to hook into the startup process of a mangoo I/O application \(e.g. for starting a database connection\). For this cases mangoo I/O offers the Lifecycle class, which can be found in the /conf package of your application.

```java
package app;

import io.mangoo.interfaces.MangooBootstrap;

public class Bootstrap implements MangooBootstrap {

    @Override
    public void initializeRoutes() {
        // Nothing to do here
    }
    
    @Override
    public void applicationInitialized() {
        // Nothing to do here
    }

    @Override
    public void applicationStarted() {
        // Nothing to do here
    }

    @Override
    public void applicationStopped() {
        // Nothing to do here
    }
}
```

The Bootstrap class doesn’t have to be named “Bootstrap”, but the class must implement the MangooBootstrap interface and you have to bind the implementation using Google Guice in your Module class. The Module class is also located in the /conf package in your application.

The Module class class can also hold other [custom Google Guice bindings](https://github.com/google/guice/wiki/GettingStarted).

```java
package app;

import com.google.inject.AbstractModule;

import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.interfaces.MangooRequestFilter;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MangooBootstrap.class).to(Bootstrap.class);
    }
}
```