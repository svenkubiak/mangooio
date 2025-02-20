Every controller method, wether it renders a template, sends JSON or just returns a HTTP Status, must return a Response object. This is handled by using the Response class of mangoo I/O. Here is an example of how a controller method may look like.

```java
package controllers;

import io.mangoo.routing.Response;

public class ApplicationController {
    public Response index() {
        String foo = "Hello world!";
        return Response.withOk().andContent("foo", foo);
    }
}
```

This controller method would lookup a method named corresponding file, which is by convention expected to be in the following path

```
/src/main/resources/templates/CONTROLLER_NAME/METHOD_NAME.ftl
```

or more concrete

```
/src/main/resources/templates/ApplicationController/index.ftl
```

With the previously mapped request in the Bootstrap.java file, a request to “/” will render the index.ftl template and send the template along with a HTTP Status OK to the browser.

## Request and query parameters


mangoo I/O makes it very easy to handle request or query parameter. Lets imagine you have the following mapping in your Bootstrap class.

```java
Bind.controller(MyController.class).withRoutes(
	On.get().to("/foo/{id}").respondeWith("myMethod")
);
```

Note the {id} in the URL, that defines that this part of the URL is a request parameter.

Now lets imagine you execute the following request

```
/user/1?foo=bar
```

For this example we are also adding a query parameter.

To access both the request and query parameter, you can simply add the names of the parameters along with the required data type to your controller method

```java
public Response index(int id, String foo) {
    //Do something useful with id and foo
    return Response.withOk();
}
```

mangoo I/O will automatically convert the passed parameters into your required data types based on their names.

The following method parameters are available in mangoo I/O controller methods by default and can be used as a request or query parameter.

```java
String
Integer/int
Float/float
Double/double
Long/long
LocalDate
LocalDateTime
```

Double and Float values are always passed with “.” delimiter, either if you pass the query or request parameter with “,” delimiter.

All parameters are parsed case-sensitive, which means, that if you have a method parameter “localDateTime” you have to map the request parameter in your routes.yaml accordingly, e.g. /foo/{localDateTime}.

LocalDate is parsed as

```
ISO_LOCAL_DATE "yyyy-MM-dd"
```

and LocalDateTime is parsed as

```
ISO_LOCAL_DATE_TIME "yyyy-MM-ddThh:mm:ss"
```

The following classes can also be used directly in controller methods, but **can not** be used as a request or query parameter

```java
Request
Session
Form
Flash
Authentication
Messages
```

It will be explained how this classes can be used in the next chapters.

## Request chain


The heart of mangoo I/O \(and probably of all web frameworks\) is the handling of requests. As mangoo I/O is based on Undertow for serving request, this is done by so called handlers. mangoo I/O has a number of handlers which all perform a specific task when a request is served. The handlers are chained to each other from the first receive of a request until sending out the response.

A DispatcherHandler is created at framework startup for each mapped controller from the routes.yaml file, waiting to receive a request. From the DispatcherHandler the request chain is as follows:

```java
LimitHandler
LocaleHandler
InboundCookiesHandler
AuthenticationHandler
AuthorizationHandler
FormHandler
RequestHandler
OutboundCookiesHandler
ResponseHandler
```

By using Google Guice features you have the option to customized each handler and change the request chain for your own needs.

To overwrite a handler, first bind the handler to you custom handler in your Modules class.

```java
bind(LocaleHandler.class).to(MyLocaleHandler.class);
```

In your custom handler you need to extend the handler class and overwrite the methods from the default handlers as you want.

```java
public class MyLocaleHandler extends LocaleHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        //do something different
    }

    @Override
    protected void nextHandler(HttpServerExchange exchange) throws Exception {
        //call another handler than the default one
    }
}
```

## Request validation


As an additional feature on the request object, you can validate incoming parameters.

```java
public Response index(Request request) {
  request.expectValue("bar");
  request.expectEmail("foo");

  if (request.isValid()) {
     //Handle request
  } else {
     //Do nothing
  }
  ...
}
```

With this validation you can check an incoming request and return specific error messages, for e.g. as JSON.

```java
public Response index(Request request) {
  request.expectValue("bar");
  request.expectEmail("foo");

  if (request.isValid()) {
     //Handle request
  } else {
     return Response.withBadRequest()
        .andJSONBody(request.getErrors());
  }
  ...
}
```

## Request values


The request class is a special object which can be passed into a controller method. It enables you access to header and URL values a long with additional information about the request. To gain access to the request object, simply pass it to your controller method.

```java
public Response index(Request request) {
    //Do something useful with the request
    return Response.withOk();
}
```

The request class is also useful when you have multiple query or request parameter which you don’t all want to name in your controller method header. To access a query or request parameter simply call the getter for the parameter.

```java
public Response index(Request request) {
    String foo = request.getParameter("foo");
    return Response.withOk();
}
```

## Response headers

mangoo I/O ships with some predifined headers that are send with the response to the client. For example:
```
X-ContentType = nosniff
X-Frame-Options = DENY
```
These headers can customized through your config.props file using the appropriate config keys. Check the default values on how to set custom headers.