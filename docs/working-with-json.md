# Working with JSON 📦

REST and AJAX endpoints often need to return JSON instead of HTML. mangoo I/O uses [Jackson](https://github.com/FasterXML/jackson) with the Blackbird module for fast serialization, chosen specifically because Blackbird generates property accessors at startup instead of relying on reflection on every request, which matters once an endpoint is handling real traffic. Controller methods can return a POJO directly, use `Response.ok().bodyJson(...)`, or bind the request body to a type by declaring it as a parameter.

**PATCH** requests merge JSON into an existing object rather than replacing it wholesale, matching what `PATCH` means in HTTP: a partial update, not a full resource replacement. Invalid JSON or failed Bean Validation yields **422 Unprocessable Content** unless you enable passthrough mode in configuration (see [Controllers](controllers.md)). For error payloads shaped consistently for API clients, `bodyJsonError(...)` returns the same structure every time, so a frontend does not need a special case for each endpoint's error format.

By default, `null` properties are omitted from output, which keeps payloads smaller and avoids leaking fields a client did not ask about. Use `@JsonInclude` to force a value out anyway (useful when a client genuinely needs to distinguish "explicitly null" from "field absent"), and `@JsonIgnore` to hide a field entirely, such as an internal comment that should never leave the server:

```java
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

public class Car {
    @JsonInclude
    public String brand = null;

    @JsonInclude
    public int doors = 0;

    @JsonIgnore
    public String comment = "internal";

    public String foo = "visible";
}
```

## JSON input

Automatic POJO binding works for `POST`, `PUT`, and `PATCH` requests sent with `Content-Type: application/json`:

```java
package controllers;

import io.mangoo.routing.Response;
import models.Car;

public class JsonController {
    public Response parse(Car car) {
        return Response.ok().bodyJson(car);
    }
}
```

If deserialization fails, the parameter becomes `UnprocessableContent` and the framework returns **HTTP 422**, the same way a failed path or query parameter conversion does.

If you would rather not commit to a POJO, for example when the payload shape genuinely varies:

```java
public Response parse(Request request) {
    Map<String, Object> json = request.getBodyAsJsonMap();
    String firstname = (String) json.get("firstname");
    return Response.ok();
}
```

Or work with the raw body directly, useful when you need to verify a signature over the exact bytes before touching the content:

```java
public Response parse(Request request) {
    String body = request.getBody();
    return Response.ok();
}
```

`JsonUtils` offers the same conversions outside of request handling, for example inside a service class or a scheduled job:

```java
String json = JsonUtils.toJson(person);
String pretty = JsonUtils.toPrettyJson(person);
Person object = JsonUtils.toObject(json, Person.class);
Map<String, String> flat = JsonUtils.toFlatMap(json);
ObjectMapper mapper = JsonUtils.getMapper();
```

`toJson()`, `toPrettyJson()`, and `toObject()` return `null` if conversion fails rather than throwing, and do not log the underlying exception. That is a deliberate trade-off: it keeps a single malformed object from crashing an otherwise unrelated code path, but it also means you should check the return value yourself if a `null` here would be a real problem for you, rather than assuming a non-null object always comes back.

## JSON output

```java
Person person = new Person("Peter", "Parker", 24);
return Response.ok().bodyJson(person);
```

```json
{
  "firstname": "Peter",
  "lastname": "Parker",
  "age": 24
}
```

`Response.badRequest().bodyJsonError("Invalid payload")` sends a small, consistent JSON error object, so clients can parse errors the same way regardless of which endpoint produced them.
