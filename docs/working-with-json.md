mangoo I/O uses [boon JSON](https://github.com/boonproject/boon) for parsing JSON. boon is a [very fast](http://rick-hightower.blogspot.de/2014/01/boon-json-in-five-minutes-faster-json.htm) JSON handler with its main focus on serializing and deserializing of objects.

## JSON Input

To retrieve JSON which is send to your mangoo I/O application you have three options: automatic object serialization, generic object convertion or working with the raw JSON string.

#### Custom serializer

By default JSON Boon will not write out nulls, empty lists or values that are default values. If you want a value to be written out even if it is empty, null, false or 0, you can use the @JsonInclude annotation. If you want a value to be excluded from JSON generation you can use the @JsonIgnore annotation.

```java
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

You can customize the JSON serialization by overwriting the JsonSerializer in the JSONUtils class which is recommended to use, when working with JSON in mangoo I/O.

```java
JsonSerializerFactory jsonSerializerFactory = new JsonSerializerFactory();
jsonSerializerFactory.useAnnotations();
jsonSerializerFactory.useFieldsOnly();
...
JsonSerializer serializer = jsonSerializerFactory.create();

JsonUtils.withJsonSerializer(serializer);
```

It is recommended that you customize the serializer when the framework starts using the lifecycle methods.

#### Automatic object convertion

Consider the Car class from above and the following JSON send to mangoo I/O

```
{
    "brand" : "Nissan",
    "doors" : 4
}
```

To handle this JSON with automatic object convertion you can simply do this in a controller.

```java
package controllers;

import io.mangoo.routing.Response;
import models.Car;

public class JsonController {
    public Response parse(Car car) {
        // TODO Do something with person object
        ...
    }
}
```

You just pass the object you want to convert from the JSON request and mangoo I/O automatically makes the serialization to your POJO, making it available in your controller.

If you don’t have a POJO and you want to retrieve the JSON content, mangoo I/O offers you a generic way of retrieving the content through the object body of a request to a Map.

```java
package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class MyController {
    public Response parse(Request request) {
        Map myjson = request.getBodyAsJsonMap();
        String foo = json.get("firstname");
    }
}
```

You can also get hold of the JSON using the great JsonPath library.

```java
package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;
import com.jayway.jsonpath.ReadContext;

public class MyController {
    public Response parse(Request request) {
        ReadContext readContext = request.getBodyAsJsonPath();
        String foo = readContext.read("$.firstname");
    }
}
```

#### Handle raw JSON string

If you don’t want mangoo I/O to automatically convert a JSON input you can also work with the raw JSON string. The body object contains the raw values of a request. Here is an example

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

Consider for example the following POJO.

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

To create a new person object and send it as a response you can simply can do this in a controller

```java
package controllers;

import io.mangoo.routing.Response;
import models.Person;

public class JsonController {
    public Response render() {
        Person person = new Person("Peter", "Parker", 24);
        return Response.withOk().andJsonBody(person);
    }
}
```

The output of the response will look as follows

```json
{
    "firstname" : "Peter",
    "lastname" : "Parker",
    "age" : 24
}
```

