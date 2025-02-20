Specially when working with forms it is useful to pass certain informations \(e.g. error- or success messages\) to the next request. To do this in a stateless environment, mangoo I/O uses the Flash class. This is basically the same mechanism as a session, but all informations are stored in a special flash cookie which is disposed once the request is finished.

Just pass the Flash class to your controller as follows:

```java
package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Flash;

public class FlashController {
    public Response flash(Flash flash) {
        flash.success("this is a success");
        flash.warning("this is a warning");
        flash.error("this is an error");
        flash.add("foo", "bar");

       return Response.withRedirect("/");
    }
}
```

The Flash class has three convenient methods for commonly used scenarios: success, warning and error. This methods will automatically create a key “success”, “warning” or “error” in the flash class. Besides that, you can pass custom keys and values to the flash class.

**Flash in templates**

To access the flash values, simply call the appropriate key in your template.

```
${flash.success}
${flash.warning}
${flash.error}
${flash.foo}
```

The Flash class is automatically available in the template so you don’t have to pass the class to the template via a controller.

