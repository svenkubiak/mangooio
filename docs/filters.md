# Filters

Filters allow execution of code before each controller or method in a controller is executed. To apply a filter, use the `@FilterWith` annotation:

```java
@FilterWith(MyFilter.class)
```

mangoo I/O provides two types of filters:

1. **Controller or Method Filters** – Applied at the class or method level.

2. **Global Filters** – Applied to all controller classes and methods.

## Controller or Method Filters

Filters can be applied to an entire controller class or individual methods. If applied at the class level, it executes before every method in that class. If applied to a method, it only executes before that specific method.

```java
package controllers;

import io.mangoo.annotations.FilterWith;
import io.mangoo.filters.AuthenticityFilter;
import io.mangoo.routing.Response;

@FilterWith(MyFilter.class)
public class MyController {
    public Response token() {
        return Response.withOk().andContent("foo", "bar");
    }

    @FilterWith(AuthenticityFilter.class)
    public Response valid() {
        return Response.withOk().andContent("foo", "bar");
    }
}
```

In this example, `MyFilter` executes for both `token()` and `valid()`. Additionally, `AuthenticityFilter` is applied to `valid()`.

### Multiple Filters

You can assign multiple filters to a controller or method:

```java
@FilterWith({MyFirstFilter.class, MySecondFilter.class})
```

Filters execute in the order they are declared.

## Global Filter

A **global filter** executes on every mapped request in `Bootstrap` class for all mapped controller classes and methods. This is useful for enforcing application-wide logic such as language settings or authentication.

Unlike controller and method filters, a global filter must implement the `MangooRequestFilter` interface:

```java
import io.mangoo.interfaces.filters.OncePerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class MyGlobalFilter implements OncePerRequestFilter {
    @Override
    public Response execute(Request request, Response response) {
        return response;
    }
}
```

### Registering a Global Filter

After creating a global filter, bind it in `Module.java`:

```java
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;
import filters.MyGlobalFilter;
import io.mangoo.interfaces.MangooLifecycle;
import io.mangoo.interfaces.filters.OncePerRequestFilter;

@Singleton
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MangooRequestFilter.class).to(MyGlobalFilter.class);
    }
}
```

!!! note
    Only one global filter can be used per mangoo I/O application.

## Creating a Custom Filter

A controller or method filter must implement the `MangooFilter` interface:

```java
package mangoo.io.filters;

import io.mangoo.interfaces.MangooControllerFilter;
import io.mangoo.routing.bindings.Exchange;

public class MyFilter implements MangooFilter {
    @Override
    public Response filter(Request request, Response response) {
        // Custom filter logic
        return response;
    }
}
```

Filters process requests in the following order:
1. Global filters
2. Controller filters
3. Method filters

Only the header and content values are merged with the response object returned by the controller.

## Example: CSRF Protection Filter

Below is an example of an authenticity filter used for CSRF checks:

```java
public class AuthenticityFilter implements MangooControllerFilter {
    @Override
    public Response execute(Request request, Response response) {
        if (!request.authenticityMatches()) {
            return Response.withForbidden().andBody(Template.DEFAULT.forbidden()).end();
        }
        return response;
    }
}
```

This filter checks the request’s authenticity and, if invalid, returns a `403 Forbidden` response, ending further filter execution.

!!! note
    Always return a response object. Returning `null` will result in an exception.
