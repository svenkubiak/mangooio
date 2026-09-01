# Filters

Filters run before the controller method. Apply them with `@FilterWith`:

```java
@FilterWith(MyFilter.class)
```

There are two kinds:

1. **Per-request filters** on a controller class or method (`PerRequestFilter`)
2. **One global filter** for every mapped controller request (`OncePerRequestFilter`)

## Controller and method filters

A class-level filter runs for every method. A method-level filter runs only for that method.

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

Assign several filters; they run in declaration order:

```java
@FilterWith({MyFirstFilter.class, MySecondFilter.class})
```

Execution order for a request:

1. Global filter
2. Controller filters
3. Method filters

Only headers and content from the filter `Response` are merged into the controller response.

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

Return `response.end()` (or another finished `Response`) to skip the controller.

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

`OriginFilter` compares the `Origin` header to a comma-separated list:

```yaml
application:
  allowedOrigins: http://foo.example, http://bar.example
```

A mismatch returns HTTP 403.

## Global filter

A global filter implements `OncePerRequestFilter` and is bound in `app.Module`. Only one global filter is supported.

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
