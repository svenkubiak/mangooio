# Controllers 🎮

Controllers are the entry point for application logic. Each public method that handles HTTP traffic returns an `io.mangoo.routing.Response` and lives in the package configured by `application.controller` (default `controllers.`). There is no framework base class to extend: your classes are plain Java types that Guice instantiates and injects, which means you can unit test a controller by just calling `new MyController(...)` with mocked dependencies, no servlet container or test harness required.

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

`bodyDefault()` sends a built-in HTML page for the status code, which is what the framework itself falls back to for error pages when you have not supplied a custom one.

Override the template path with `template("/path/to/file.ftl")` when the convention does not fit, for example several methods sharing one confirmation page.

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
```

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

Supported parameter types:

- `String`
- `Integer` / `int`
- `Long` / `long`
- `Float` / `float`
- `Double` / `double`
- `Boolean` / `boolean`
- `LocalDate` (`ISO_LOCAL_DATE`, `yyyy-MM-dd`)
- `LocalDateTime` (`ISO_LOCAL_DATE_TIME`)
- `Optional` of the types above

Parameter names are case-sensitive and must match the path placeholder exactly. Decimal values are parsed with `.` as the separator even if the client's locale sends a `,`, since the framework parses numbers the same way regardless of where the request came from.

These types are bound from the request itself, not from the URL, and should not be used as path placeholder names:

- `Request`
- `Session`
- `Form`
- `Flash`
- `Authentication`
- `Messages`

A JSON POJO is bound from the request body on `POST`, `PUT`, and `PATCH` when the request sends `Content-Type: application/json`. See [Working with JSON](working-with-json.md).

If conversion fails (someone requests `/users/abc` where `abc` cannot become an `int`), the framework returns **HTTP 422 Unprocessable Entity** unless you handle it yourself, since the request was syntactically fine, it is the value itself that could not be turned into what the method expected.

## Bean Validation

Annotate controller parameters with Jakarta Bean Validation constraints. On failure, the default response is HTTP 400 with the built-in HTML error page, which is usually what you want for a server-rendered form submission.

```java
public Response show(@NotBlank String id) {
    return Response.ok().render();
}
```

If you are building a JSON API instead, set `application.validation.passthrough` to `true` to get the same validation but as a JSON error body rather than an HTML page:

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

Other useful methods include `getURI()`, `getURL()`, `getPath()`, `getScheme()`, `getCookie(name)`, `getBodyAsJsonMap()`, and `hasValidCsrf()`.

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
