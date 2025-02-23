# Bootstrap

In some cases, it is useful to hook into the startup process of a Mangoo I/O application, such as initializing a database connection. For these cases, Mangoo I/O provides the `MangooBootstrap` interface, which can be implemented in a class within the `/conf` package of your application.

## Implementing the Bootstrap Class

The `Bootstrap` class allows you to execute code at different stages of the application lifecycle. Below is a basic implementation:

```java
package app;

import io.mangoo.interfaces.MangooBootstrap;

public class Bootstrap implements MangooBootstrap {

    @Override
    public void initializeRoutes() {
        // Executed when routes are initialized
    }
    
    @Override
    public void applicationInitialized() {
        // Executed after the application has been initialized
    }

    @Override
    public void applicationStarted() {
        // Executed after the application has fully started
    }

    @Override
    public void applicationStopped() {
        // Executed when the application is shutting down
    }
}
```

The class name does not need to be `Bootstrap`, but it must implement the `MangooBootstrap` interface. Additionally, you must bind the implementation using Google Guice in the `Module` class.

## Configuring the Module Class

The `Module` class is also located in the `/conf` package and is responsible for defining dependency bindings using Google Guice. You can bind the `MangooBootstrap` implementation as shown below:

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

The `Module` class can also be used to define additional [custom Google Guice bindings](https://github.com/google/guice/wiki/GettingStarted) as needed.
