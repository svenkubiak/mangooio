# Working with JSON

REST and AJAX endpoints often return JSON instead of HTML. mangoo I/O uses [Jackson](https://github.com/FasterXML/jackson) with the Blackbird module for fast serialization. Controller methods can return a POJO directly, use `Response.ok().bodyJson(...)`, or bind the request body to a type with `@Body` on a parameter.

**PATCH** requests merge JSON into an existing object; invalid JSON or failed Bean Validation yields **422 Unprocessable Content** unless you enable passthrough mode in configuration. For error payloads shaped for clients, `bodyJsonError(...)` returns a consistent structure.

By default, `null` properties are omitted from output. Use `@JsonInclude` to force a value out, and `@JsonIgnore` to hide a field:

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

Automatic POJO binding works for `POST`, `PUT`, and `PATCH` with `Content-Type: application/json`:

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

If deserialization fails, the parameter becomes `UnprocessableContent` and the framework returns **HTTP 422**.

Without a POJO:

```java
public Response parse(Request request) {
    Map<String, Object> json = request.getBodyAsJsonMap();
    String firstname = (String) json.get("firstname");
    return Response.ok();
}
```

Raw body:

```java
public Response parse(Request request) {
    String body = request.getBody();
    return Response.ok();
}
```

`JsonUtils` helpers:

```java
String json = JsonUtils.toJson(person);
String pretty = JsonUtils.toPrettyJson(person);
Person object = JsonUtils.toObject(json, Person.class);
Map<String, String> flat = JsonUtils.toFlatMap(json);
ObjectMapper mapper = JsonUtils.getMapper();
```

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

`Response.badRequest().bodyJsonError("Invalid payload")` sends a small JSON error object.
