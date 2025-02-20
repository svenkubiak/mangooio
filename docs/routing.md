One of the main pieces of a mangoo I/O application is the mapping of request URLs to controller classes and their methods. Whether you are rendering a template, sending JSON or just a HTTP 200 OK, every request has to be mapped. This mapping is done in the Bootstra.java class, which you’ll find in the /src/main/java/app package of your application.

Here is an example of how a routing might look like.

```java
@Override
public void initializeRoutes() {
    // ApplicationController
    Bind.controller(ApplicationController.class).withRoutes(
    	On.get().to("/").respondeWith("index")
	);
}
```

This example maps a GET request to “/” to the index method of the ApplicationController class. Thus, when you open your browser and open the “/” of your application the index method in the ApplicationController class will be called.

You can use the following request methods to define your mappings

```java
GET
POST
OPTIONS
PUT
HEAD
DELETE
PATCH
```

The underlying Undertow server handles all request by using non-blocking I/O. However, there might be situations where you need a long running request. To allow blocking in a request, simply at the blocking attribute to your request mapping.

```java
@Override
public void initializeRoutes() {
    // ApplicationController
    Bind.controller(ApplicationController.class).withRoutes(
    	On.get().to("/").respondeWith("index").withNonBlocking()
	);
}
```

## Static files

If you want to serve static files \(e.g. assets\) you can map those files from your routes.yaml. You can map either a specific file or a complete folder and all its sub-content.

```java
Bind.pathResource().to("/assets/");
Bind.fileResource().to("/robots.txt");
```

The file or path mapping is bound to the src/main/resources/files folder in your application. The above mappings would server the following files accordingly.

```properties
/src/main/resources/files/robots.txt
/src/main/resources/files/assets/
```

## Server Sent Events and WebSocket

Mappings for Server-Sent Events and WebSockets are also defined in the routes.yaml. As the Server-Sent Event is a uni-directional protocol, it does not have a controller it needs mapping to. You would map a Server-Sent Event as follows

```java
Bind.serverSentEvent().to("/mysse");
```

As a WebSocket comes with a pre-defined interface, you just need to add you implementing class

```java
Bind.webSocket().onController(WebSocketController.class).to("/websocket");
```

There might be situations where your Server-Sent Events and/or WebSockets are only available for authenticated users. If this is the case, you can simply add the authentication attribute to your mappings.

```java
Bind.serverSentEvent().to("/sseauth").withAuthentication();
```

This will require an authentication cookie in the request to the Server-Sent Event or WebSocket, which is based on the [build-in authentication mechanism](https://docs.mangoo.io/custom-authentication.html). If the request does not have such a cookie, the Server-Sent Event or WebSocket connection will be rejected.

Check the page for [Server-Sent Events ](https://docs.mangoo.io/server-sent-events.html) and [WebSocket](https://docs.mangoo.io/websockets.html) on how to handled the SSE and WSS requests.