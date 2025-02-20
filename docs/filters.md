Filters are a way of executing code before each controller or each method is executed. To execute a filter before a controller or method, you can use the @FilterWith annotation.

```java
@FilterWith(MyFilter.class)
```

There are two types of filters in mangoo I/O: Controller or Method filters and a global filter.

## Controller or method filter

As mentioned, a filter can be added to a controller class or method. If added to a controller class the filter will be exectued before every method in the class. If added to a method, the filter will only be executed before that method.

```java
package controllers;

import io.mangoo.annotations.FilterWith;
import io.mangoo.filters.AuthenticityFilter;
import io.mangoo.routing.Response;

@FilterWith(MyFilter.class)
public class MyController {

    public Response token() {
        return Response.withOk().andContent("foo", "bar");
    }

    @FilterWith(AuthenticityFilter.class)
    public Response valid() {
        return Response.withOk().andContent("foo", "bar");
    }
}
```

On the above example, the Filter MyFilter will be executed when the token\(\) and the valid\(\) method is called. The Filter AuthenticityFilter will also be called, when the valid\(\) method is called.

You can assign multiple filters to a controller or a method.

```java
@FilterWith({"MyFirstFilter.class, MySecondFilter.class"})
```

Filters are executed in order.

## Global filter

Besides the controller class or method filter, there is a special filter which can be executed globally. This means, that this filter is called on every mapped request in the routes.yaml file for controller classes and methods. This is useful when you have to force the language for your application or if you have an application that does not have any public content and requires authentication for every request.

A global filter works similar to a controller or method filter, but the filter has to implement the MangooRequestFilter interface instead.

```java
import io.mangoo.interfaces.filters.OncePerRequestFilter;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Request;

public class MyGlobalFilter implements OncePerRequestFilter {

    @Override
    public Response execute(Request request, Response response) {
        return response;
    }
}
```

After creating your global filter you need to bind the class in the Module.java file in order to let mangoo I/O know that there is a global filter.

```java
import com.google.inject.AbstractModule;
import com.google.inject.Singleton;

import filters.MyGlobalFilter;
import io.mangoo.interfaces.MangooLifecycle;
import io.mangoo.interfaces.filters.OncePerRequestFilter;

@Singleton
public class Module extends AbstractModule {
    @Override
    protected void configure() {
        bind(MangooRequestFilter.class).to(MyGlobalFilter.class);
    }
}
```

Please note that due to the purpose of a global filter, there can be only one global filter in your mangoo I/O application.

## Creating a filter

A controller or method filter must implement the MangooFilter interface.

```java
package mangoo.io.filters;

import io.mangoo.interfaces.MangooControllerFilter;
import io.mangoo.routing.bindings.Exchange;

public class MyFilter implements MangooFilter {

    @Override
    public Response filter(Request request, Response response) {
        //Do nothing for now
        return response;
    }
}
```

The main method of a filter is the execute method, which receives the request and response class from mangoo I/O. This classes give you a handy way of manipulating the response as it is passed to other filters and merged with the response of your controller, if you don’t end the request at some point in the filter.

All returned response object from your filter are passed to the next filter in the following order:

```
Global filter
Controller filters
Method filters
```

Only the header and content values are merged with the response object returned from your controller.  
Here is an example of the AuthenticityFilter which is used for the CSRF checks.

```java
public class AuthenticityFilter implements MangooControllerFilter {

    @Override
    public Response execute(Request request, Response response) {
        if (!request.authenticityMatches()) {
            return Response.withForbidden().andBody(Template.DEFAULT.forbidden()).end();
        }

        return response;
    }
}
```

As you can see in the example, you can change the status code, a long with the content of the response inside a filter. The end\(\) method tells mangoo I/O that i should end the response at this point and should not execute further filters or controllers.

Please note, that you always have to return the response object. Return null will result most certainly in an exception.