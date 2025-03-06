# Working with JSON

mangoo I/O uses [Jackson](https://github.com/FasterXML/jackson) with [Blackbird](https://github.com/stevenschlansker/jackson-blackbird) for JSON serialization and deserialization of objects.

## JSON Input

To retrieve JSON sent to your mangoo I/O application, you have three options:

1. Automatic object serialization
2. Generic object conversion
3. Working with the raw JSON string

### Custom Serializer

By default, mangoo I/O does not write out nulls, empty lists, or default values. To ensure a value is included even if it is empty, `null`, `false`, or `0`, use the `@JsonInclude` annotation. To exclude a value from JSON generation, use `@JsonIgnore`.

```java
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Car {
    @JsonInclude
    public String brand = null;

    @JsonInclude
    public int doors = 0;

    @JsonIgnore
    public String comment = "blablabla";

    public String foo = "blablabla";

    public Car() {}
}
```

### Automatic Object Conversion

Given the following JSON input:

```json
{
    "brand": "Nissan",
    "doors": 4
}
```

You can automatically convert it to a Java object in a controller:

```java
package controllers;

import io.mangoo.routing.Response;
import models.Car;

public class JsonController {
    public Response parse(Car car) {
        // Process the car object
        ...
    }
}
```

mangoo I/O automatically deserializes JSON into a POJO, making it available in the controller.

!!! note
    Automatic conversion only works with `PUT` or `POST` requests having `Content-Type: application/json`.

If you do not have a POJO but still need to retrieve JSON content, you can use a generic approach:

```java
package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import java.util.Map;

public class MyController {
    public Response parse(Request request) {
        Map<String, Object> json = request.getBodyAsJsonMap();
        String foo = (String) json.get("firstname");
    }
}
```

### Handling Raw JSON String

If you prefer working with the raw JSON string, retrieve it from the request body:

```java
package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class MyController {
    public Response parse(Request request) {
        String body = request.getBody();
        ...
    }
}
```

## JSON Output

Consider the following POJO:

```java
package models;

public class Person {
    private String firstname;
    private String lastname;
    private int age;

    public Person(String firstname, String lastname, int age) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.age = age;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public int getAge() {
        return age;
    }
}
```

To create a new `Person` object and send it as a JSON response:

```java
package controllers;

import io.mangoo.routing.Response;
import models.Person;

public class JsonController {
    public Response render() {
        Person person = new Person("Peter", "Parker", 24);
        return Response.ok().bodyJson(person);
    }
}
```

### JSON Response Output

```json
{
    "firstname": "Peter",
    "lastname": "Parker",
    "age": 24
}
```
