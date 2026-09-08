# Filters

Filters intercept requests **after** routing matches but **before** the controller method runs. Reach for them for cross-cutting checks such as authentication gates, API keys, CSRF validation, CORS preflight handling, or shared request logging, anything you would otherwise have to repeat at the top of every controller method.

Attach a filter with `@FilterWith` on a controller class or individual method:

```java
@FilterWith(MyFilter.class)
```

For logic that must run on **every** controller request, implement `OncePerRequestFilter` and register it in `Bootstrap.initializeRoutes()` alongside your route bindings.

There are two kinds, depending on how broadly you need the filter applied:

1. **Per-request filters** on a controller class or method (`PerRequestFilter`)
2. **One global filter** for every mapped controller request (`OncePerRequestFilter`)

## Controller and method filters

A class-level filter runs for every method on that controller. A method-level filter runs only for the one method it is attached to.

```java
package controllers;

import filters.MyFilter;
import io.mangoo.annotations.FilterWith;
import io.mangoo.filters.CsrfFilter;
import io.mangoo.routing.Response;

@FilterWith(MyFilter.class)
public class AccountController {
    public Response form() {
        return Response.ok().render();
    }

    @FilterWith(CsrfFilter.class)
    public Response save() {
        return Response.ok().render();
    }
}
```

Assign several filters at once, and they run in the order you declared them:

```java
@FilterWith({MyFirstFilter.class, MySecondFilter.class})
```

Execution order for a request:

1. Global filter
2. Controller filters
3. Method filters

Only headers and content from the filter `Response` are merged into the controller response; everything else the filter sets is discarded.

## Writing a per-request filter

Implement `io.mangoo.interfaces.filters.PerRequestFilter`:

```java
package filters;

import io.mangoo.interfaces.filters.PerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class MyFilter implements PerRequestFilter {
    @Override
    public Response execute(Request request, Response response) {
        return response;
    }
}
```

Return `response.end()` (or any other finished `Response`) to short-circuit the request and skip the controller method entirely.

## Built-in filters

| Filter | Purpose |
|---|---|
| `CsrfFilter` | Requires a valid CSRF token. See [CSRF](csrf.md). |
| `OriginFilter` | Requires `Origin` to match `application.allowedOrigins`. |
| `ApiKeyFilter` | Requires `Authorization: Bearer <application.api.key>`. |
| `AdminFilter` | Protects `/@admin` (used internally). |

```java
@FilterWith(OriginFilter.class)
public class ApiController { }

@FilterWith(ApiKeyFilter.class)
public Response privateApi() {
    return Response.ok().bodyJson(payload);
}
```

`OriginFilter` compares the `Origin` header against a comma-separated list:

```yaml
application:
  allowedOrigins: http://foo.example, http://bar.example
```

A mismatch returns HTTP 403 rather than falling through to the controller.

## Global filter

A global filter implements `OncePerRequestFilter` and is bound in `app.Module`. Only one is supported at a time, so if you need several concerns handled globally, compose them into a single filter implementation.

```java
package filters;

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

```java
package app;

import com.google.inject.AbstractModule;
import filters.MyGlobalFilter;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.interfaces.filters.OncePerRequestFilter;

public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MangooBootstrap.class).to(Bootstrap.class);
        bind(OncePerRequestFilter.class).to(MyGlobalFilter.class);
    }
}
```
