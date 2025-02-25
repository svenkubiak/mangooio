# Routing

One of the core components of a Mangoo I/O application is mapping request URLs to controller classes and their methods. Whether rendering a template, sending JSON, or returning an HTTP 200 OK, every request must be mapped. This mapping is handled in the `Bootstrap.java` class, located in the `/src/main/java/app` package of your application.

## Routing Example

Below is an example of how routing is defined:

```java
@Override
public void initializeRoutes() {
    Bind.controller(ApplicationController.class).withRoutes(
        On.get().to("/").respondWith("index")
    );
}
```

This example maps a `GET` request to `/` to the `index` method of the `ApplicationController` class. When a user accesses `/` in a browser, the `index` method in `ApplicationController` is called.

## Supported Request Methods

You can use the following request methods to define your mappings:

```
GET
POST
OPTIONS
PUT
HEAD
DELETE
PATCH
```

## Handling Long-Running Requests

Mangoo I/O uses Undertow for non-blocking I/O. However, some situations may require a long-running request. To allow blocking in a request, add the `blocking` attribute to your request mapping:

```java
@Override
public void initializeRoutes() {
    Bind.controller(ApplicationController.class).withRoutes(
        On.get().to("/").respondWith("index").withNonBlocking()
    );
}
```

## Authentication

Authentication can be applied at both the controller and method levels:

```java
@Override
public void initializeRoutes() {
    Bind.controller(ApplicationController.class).withAuthentication().withRoutes(
        On.get().to("/").respondWith("index")
    );

    Bind.controller(DashboardController.class).withRoutes(
        On.get().to("/").respondWith("index").withAuthentication(),
        On.get().to("/login").respondWith("login")
    );
}
```

## Serving Static Files

To serve static files (e.g., assets), map them in your `routes.yaml`. You can specify individual files or entire directories:

```java
Bind.pathResource().to("/assets/");
Bind.fileResource().to("/robots.txt");
```

The mappings correspond to files in the `src/main/resources/files` folder:

```properties
/src/main/resources/files/robots.txt
/src/main/resources/files/assets/
```

## Server-Sent Events (SSE)

Mappings for Server-Sent Events (SSE) are defined in the `Bootstrap.java` class. Since SSE is a unidirectional protocol, it does not require a controller:

```java
Bind.serverSentEvent().to("/mysse");
```

If authentication is required for SSE or WebSockets, simply add the authentication attribute:

```java
Bind.serverSentEvent().to("/sseauth").withAuthentication();
```

This requires an authentication cookie in the request. If the cookie is missing, the SSE connection will be rejected.

For further details, refer to the [Server-Sent Events documentation](https://docs.mangoo.io/server-sent-events.html).
