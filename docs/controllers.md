# Controllers 🎮

Controllers are the entry point for application logic. Each public method that handles HTTP traffic returns an `io.mangoo.routing.Response`. There is no framework base class to extend: your classes are plain Java types that Guice instantiates and injects, which means you can unit test a controller by just calling `new MyController(...)` with mocked dependencies, no servlet container or test harness required.

A controller method can declare only what it actually needs in its parameter list: path variables, query strings, JSON bodies, the raw `Request`, session data, forms, authentication state, or translation helpers. Anything you do not list simply is not there, which keeps method signatures honest about what a given endpoint actually reads from the request.

Since version 9, returning `Response.ok()` alone does **not** render a template. You have to call `.render()` or set an explicit body yourself. Earlier versions rendered a template implicitly by convention, which was convenient right up until you wanted a controller method to return plain JSON or a bare status code, at which point the "helpful" default became something you had to actively suppress. Making it explicit means a response is exactly as empty, JSON, or HTML as the code says it is, and nothing more.

```java
package controllers;

import io.mangoo.routing.Response;

public class ApplicationController {
    public Response index() {
        return Response.ok();
    }
}
```

`Response.ok()` on its own sends HTTP 200 with an empty `text/plain` body. It does **not** render a template.

## HTML templates

Call `render()` to use Freemarker. By convention the template path is:

```
src/main/resources/templates/CONTROLLER_NAME/METHOD_NAME.ftl
```

```java
public Response index() {
    return Response.ok().render();
}

public Response greet() {
    return Response.ok().render("name", "Ada");
}
```

The first example looks up `templates/ApplicationController/index.ftl`. The mapping is case-sensitive, so a controller named `ApplicationController` will not find a template directory named `applicationcontroller`.

`bodyDefault()` sends a built-in HTML page for the status code. That is what the framework uses when you have not supplied your own error page.

`Response.ok()` with no body is HTTP 200 and `text/plain; charset=UTF-8`. Text responses get that charset appended.

Override the template when several methods share one page. The path is resolved under `src/main/resources/templates/`, and `.ftl` is optional. A leading slash is accepted:

```java
return Response.ok().render("title", "Saved").template("shared/confirm.ftl");
```

`${...}` in a template is HTML-escaped. See [Templating](templating.md).

## Response helpers

```java
Response.ok()                  // 200
Response.created()             // 201
Response.accepted()            // 202
Response.notModified()         // 304
Response.badRequest()          // 400
Response.unauthorized()        // 401
Response.forbidden()           // 403
Response.notFound()            // 404
Response.internalServerError() // 500
Response.status(418)
Response.redirect("/login")
```

Body helpers:

```java
return Response.ok().bodyText("hello");
return Response.ok().bodyHtml("<p>hello</p>");
return Response.ok().bodyJson(person);
return Response.badRequest().bodyJsonError("Invalid payload");
return Response.ok().bodyBinary(bytes);
return Response.ok().bodyFile(Path.of("/var/lib/app/archive.zip"));
```

`bodyJson(Object)` calls `JsonUtils.toJson`, which is compact and omits nulls. A failed conversion sends an empty JSON body and the status you set. It does not throw. `bodyJsonError(String)` sends `io.mangoo.models.Error`: `timestamp` (a `LocalDateTime` with no zone), `status`, and `message`.

`bodyBinary` holds the whole response in the heap. `bodyFile` streams from disk. The file is checked when the response is sent, and a missing or unreadable file becomes HTTP 500. Both detect `Content-Type` when you have not set one.

`redirect` sends 302. `disposeCookie(name)` clears a cookie with `Secure`. Pass `false` as the second argument when the cookie was not secure. `end()` stops the rest of the filter chain.

Add headers and cookies with `header(...)`, `headers(...)`, and `cookie(...)`. Call `end()` when a [filter](filters.md) further down the chain should not run, for example after a filter has already produced the response it wants to send.

Redirects are a static factory, not something you chain onto `ok()`, since a redirect response has no meaningful "ok" status of its own:

```java
return Response.redirect("/dashboard");
```

## Path and query parameters

```java
Bind.controller(UserController.class).withRoutes(
    On.get().to("/users/{id}").respondeWith("show")
);
```

For a request to `/users/1?active=true`:

```java
public Response show(int id, boolean active) {
    return Response.ok().render();
}
```

Note that `id` is bound from the `{id}` path placeholder while `active` comes from the query string, purely by matching parameter names, there is no annotation required for either. This is exactly why the `-parameters` compiler flag matters so much (see [Getting started](getting-started.md)): without it, the JVM cannot tell mangoo I/O that a parameter is called `id` in the first place.

If a query parameter carries the same name as a route parameter, the route value always wins: a request to `/users/1?id=2` binds `id` to `1`. A client can therefore never forge a route parameter. Set `application.parameter.strict` to `true` to reject such ambiguous requests with a `400` instead.

### Which parameter to trust

When you need to know where a value actually came from, take the `Request` and ask it directly:

```java
public Response show(Request request) {
    String id = request.getPathParameter("id");       // from {id}, never client-forgeable
    String limit = request.getQueryParameter("limit"); // from the query string, untrusted
    return Response.ok().render();
}
```

`request.getParameter(key)` returns the merged view described above. It cannot tell a route parameter apart from client input, so **authorization decisions must not be based on it**. In particular, if the presence of a parameter selects an operation, use `request.hasPathParameter(key)` — a client can always append a query parameter of any name:

```java
// wrong: any client can add ?id= and switch the operation
var operation = request.getParameter("id") != null ? Operation.VIEW : Operation.LIST;

// right: only the route decides
var operation = request.hasPathParameter("id") ? Operation.VIEW : Operation.LIST;
```

Supported parameter types:

- `String`
- `Integer` / `int`
- `Long` / `long`
- `Float` / `float`
- `Double` / `double`
- `Boolean` / `boolean`
- `LocalDate` (`ISO_LOCAL_DATE`, `yyyy-MM-dd`)
- `LocalDateTime` (`ISO_LOCAL_DATE_TIME`)
- `Optional<String>` (the value is always a `String`, other type arguments are not converted)

Parameter names are case-sensitive and must match the path placeholder exactly. Decimal values are parsed with `.` as the separator even if the client's locale sends a `,`, since the framework parses numbers the same way regardless of where the request came from.

These types are bound from the request itself, not from the URL, and should not be used as path placeholder names:

- `Request`
- `Session`
- `Form`
- `Flash`
- `Authentication`
- `Messages`

A JSON POJO is bound from the request body on `POST`, `PUT`, and `PATCH` when the request sends `Content-Type: application/json`. See [Working with JSON](working-with-json.md).

If conversion fails, for example `/users/abc` where `id` is an `int`, the framework returns **HTTP 422** with an empty body and does not call the method. There is no callback for that case.

A missing value is not a conversion failure. A missing `int`, `long`, `float`, or `double` becomes `0`. A missing `boolean` becomes `false`. A missing `String`, boxed number, `Boolean`, `LocalDate`, or `LocalDateTime` is `null`.

A `boolean` parameter accepts only `true` and `false`, ignoring case. `1` is a 422. `Form.getBoolean` is a different parser: `"1"` and `"true"` are true, `"0"` and `"false"` are false, and the match is case-sensitive.

`Optional` is always `Optional<String>`. The type argument is erased, so `Optional<Integer>` is not converted.

A type outside that list is a JSON body only on `POST`, `PUT`, and `PATCH` when `Content-Type` starts with `application/json` or contains `+json`. Otherwise the argument is `null`. A blank JSON body is HTTP 400. Invalid JSON uses a no-arg constructor when the class has one, and is a 422 when it does not.

A repeated query key, or a key that fails URL decoding, is HTTP 400 with the built-in bad-request page, before the controller runs. The same happens for a query key that equals a path key when `application.parameter.strict` is true. That default is false, and then the path value wins.

## Bean Validation

Annotate controller parameters with Jakarta Bean Validation constraints. On failure, the default response is HTTP 400 with the built-in HTML error page, which is usually what you want for a server-rendered form submission.

```java
public Response show(@NotBlank String id) {
    return Response.ok().render();
}
```

For a JSON API, set `application.validation.passthrough` to `true`. The status is still 400. The messages are the English Bean Validation text, not the request locale:

```json
{ "errors": { "id": "must not be blank" } }
```

This is not the same mechanism as [form validation](forms.md). Bean Validation here applies to controller method parameters directly; form validation rules live on the `Form` object and are meant for classic HTML form submissions with multiple fields and a re-rendered form on error.

## Request object

Inject `Request` when you need something that does not fit neatly into a typed parameter, like a header or the raw body:

```java
public Response index(Request request) {
    String foo = request.getParameter("foo");
    String agent = request.getHeader("User-Agent");
    String body = request.getBody();
    return Response.ok();
}
```

Other useful methods: `getURI()` (not decoded, no query), `getURL()` (absolute, no query), `getPath()` (decoded, no query), `getScheme()`, `getContentLength()` (`-1` when absent), `getBodyAsJsonMap()` (an empty map for a blank body, `null` for invalid JSON), and `hasValidCsrf()`.

`getCookie(name)` returns an Undertow `Cookie`, not a `String`:

```java
Cookie cookie = request.getCookie("myapp-session");
String value = cookie == null ? null : cookie.getValue();
```

## Custom handlers

Handler classes such as `LocaleHandler` are Guice-managed too, so you can swap the framework's behavior for your own by binding a subclass in `app.Module`, without forking the framework itself:

```java
bind(LocaleHandler.class).to(MyLocaleHandler.class);
```

```java
public class MyLocaleHandler extends LocaleHandler {
    @Inject
    public MyLocaleHandler(Config config) {
        super(config);
    }

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        super.handleRequest(exchange);
    }
}
```

Calling `super.handleRequest(exchange)` keeps the original behavior and lets you add logic before or after it, which is usually less risky than reimplementing the handler from scratch.
