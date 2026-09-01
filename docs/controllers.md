# Controllers

Controllers are the entry point for application logic. Each public method that handles HTTP traffic returns an `io.mangoo.routing.Response` and lives in the package configured by `application.controller` (default `controllers.`). There is no framework base class: your classes are plain Java types that Guice instantiates and injects.

A controller method can declare only what it needs in its parameter list—path variables, query strings, JSON bodies, the raw `Request`, session data, forms, authentication state, or translation helpers. That keeps signatures readable and makes unit testing straightforward.

Since version 9, returning `Response.ok()` alone does **not** render a template. You must call `.render()` or set an explicit body. This makes it obvious when a response is empty, JSON, or HTML.

```java
package controllers;

import io.mangoo.routing.Response;

public class ApplicationController {
    public Response index() {
        return Response.ok();
    }
}
```

`Response.ok()` sends HTTP 200 with an empty `text/plain` body. It does **not** render a template.

## HTML templates

Call `render()` to use Freemarker. By convention the template is:

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

The first example looks up `templates/ApplicationController/index.ftl`. Mapping is case-sensitive.

`bodyDefault()` sends a built-in HTML page for the status code (used for framework error pages).

Override the template path with `template("/path/to/file.ftl")`.

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

Add headers and cookies with `header(...)`, `headers(...)`, and `cookie(...)`. Call `end()` when a [filter](filters.md) should stop the remaining chain.

Redirects are a static factory, not a chain on `ok()`:

```java
return Response.redirect("/dashboard");
```

## Path and query parameters

```java
Bind.controller(UserController.class).withRoutes(
    On.get().to("/users/{id}").respondeWith("show")
);
```

For `/users/1?active=true`:

```java
public Response show(int id, boolean active) {
    return Response.ok().render();
}
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
- `Optional` of the types above

Parameter names are case-sensitive and must match the path placeholder. Decimal values use `.` even if the client sent `,`.

These types are bound from the request, not from the URL, and must not be used as path placeholders:

- `Request`
- `Session`
- `Form`
- `Flash`
- `Authentication`
- `Messages`

A JSON POJO is bound from the body on `POST`, `PUT`, and `PATCH` with `Content-Type: application/json`. See [Working with JSON](working-with-json.md).

If conversion fails, the framework returns **HTTP 422** unless you handle it yourself.

## Bean Validation

Annotate controller parameters with Jakarta Bean Validation constraints. On failure the default response is HTTP 400 with the built-in HTML error page.

```java
public Response show(@NotBlank String id) {
    return Response.ok().render();
}
```

Set `application.validation.passthrough` to `true` to return JSON instead:

```json
{ "errors": { "id": "must not be blank" } }
```

This is not the same as [form validation](forms.md). Form rules live on the `Form` object.

## Request object

```java
public Response index(Request request) {
    String foo = request.getParameter("foo");
    String agent = request.getHeader("User-Agent");
    String body = request.getBody();
    return Response.ok();
}
```

Useful methods include `getURI()`, `getURL()`, `getPath()`, `getScheme()`, `getCookie(name)`, `getBodyAsJsonMap()`, and `hasValidCsrf()`.

## Custom handlers

Handler classes such as `LocaleHandler` are Guice-managed. Bind a subclass in `app.Module` if you need to replace one:

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
