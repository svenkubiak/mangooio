# Controllers

Every controller method, whether it renders a template, sends JSON, or just returns an HTTP status, must return a `Response` object. This is handled using the `Response` class of **mangoo I/O**. Below is an example of a controller method:

```java
package controllers;

import io.mangoo.routing.Response;

public class ApplicationController {
    public Response index() {
        return Response.ok();
    }
}
```

The example above returns a blank HTML page without any rendering.

## Predefined HTML Templates
Mangoo I/O provides predefined HTML templates for standard responses:

```java
package controllers;

import io.mangoo.routing.Response;

public class ApplicationController {
    public Response index() {
        return Response.ok().bodyDefault();
    }
}
```

To trigger rendering of a **Freemarker template**, use `render()`:

```java
package controllers;

import io.mangoo.routing.Response;

public class ApplicationController {
    public Response index() {
        return Response.ok().render();
    }

    public Response foo() {
        return Response.ok().render("bar", "value");
    }
}
```

By convention, the corresponding Freemarker template is expected to be in:

```
/src/main/resources/templates/CONTROLLER_NAME/METHOD_NAME.ftl
```

For example:

```
/src/main/resources/templates/ApplicationController/index.ftl
```

!!! note
    Mapping of controller methods to templates is **case-sensitive**.

With the previously mapped request in the `Bootstrap.java` file, a request to `/` will render the `index.ftl` template and send it along with an HTTP **200 OK** response.

---

## Request and Query Parameters

Mangoo I/O makes handling request or query parameters straightforward. Assume the following mapping in your `Bootstrap` class:

```java
Bind.controller(MyController.class).withRoutes(
    On.get().to("/foo/{id}").respondWith("myMethod")
);
```

Here, `{id}` in the URL defines a **request parameter**.

For example, given the request:

```
/user/1?foo=bar
```

Both **request** and **query** parameters can be accessed in the controller method:

```java
public Response index(int id, String foo) {
    // Process id and foo
    return Response.ok().render();
}
```

Mangoo I/O automatically converts passed parameters into the required data types.

### **Supported Parameter Types**
The following parameter types are supported by default:

```java
String
Integer / int
Float / float
Double / double
Long / long
LocalDate
LocalDateTime
Optional
```

- **Double and Float values** must use a `.` delimiter, even if the query parameter is passed with `,`.
- **All parameters are case-sensitive**. For example, if a method parameter is `localDateTime`, it must be mapped exactly in Bootstrap URL mapping, like so:

  ```
  /foo/{localDateTime}
  ```

- **Date Formats**
    - `LocalDate`: `yyyy-MM-dd` (`ISO_LOCAL_DATE`)
    - `LocalDateTime`: `yyyy-MM-ddThh:mm:ss` (`ISO_LOCAL_DATE_TIME`)

### **Unsupported Parameter Types**
The following classes **cannot** be used as request or query parameters but can still be used in controller methods:

```java
Request
Session
Form
Flash
Authentication
Messages
```

These will be explained in the next chapters.

---

## Custom Handlers

Using **Google Guice**, you can customize request handlers as needed. To override a handler, first bind your custom handler in your `Modules` class:

```java
bind(LocaleHandler.class).to(MyLocaleHandler.class);
```

Then, extend the default handler class:

```java
public class MyLocaleHandler extends LocaleHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        // Custom handling logic
    }

    @Override
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        // Call a different handler instead of the default one
    }
}
```

---

## Request Validation

Mangoo I/O allows **validation of incoming request parameters**.

```java
public Response index(Request request) {
    request.expectValue("bar");
    request.expectEmail("foo");

    if (request.isValid()) {
        // Process request
    } else {
        // Handle invalid request
    }
}
```

### **Returning Validation Errors as JSON**
You can return validation errors as a JSON response:

```java
public Response index(Request request) {
    request.expectValue("bar");
    request.expectEmail("foo");

    if (!request.isValid()) {
        return Response.badRequest().bodyJson(request.getErrors());
    }

    return Response.ok();
}
```

---

## Request Object and Values

The `Request` object provides access to headers, URL values, and additional request data. It can be passed directly into a controller method:

```java
public Response index(Request request) {
    // Process request
    return Response.ok();
}
```

When dealing with multiple query or request parameters, instead of listing them explicitly, you can access them using:

```java
public Response index(Request request) {
    String foo = request.getParameter("foo");
    return Response.ok();
}
```
