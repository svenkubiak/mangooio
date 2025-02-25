# Flash

When working with forms, it is often necessary to pass information such as error or success messages to the next request. In a stateless environment, mangoo I/O provides the `Flash` class for this purpose. The `Flash` class functions similarly to a session but stores data in a temporary flash cookie, which is automatically discarded after the request is completed.

## Using Flash Messages in Controllers

To use flash messages, pass the `Flash` class to your controller method:

```java
package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Flash;

public class FlashController {
    public Response flash(Flash flash) {
        flash.success("This is a success message");
        flash.warning("This is a warning message");
        flash.error("This is an error message");
        flash.add("foo", "bar");

       return Response.ok().redirect("/");
    }
}
```

### Built-in Flash Methods

The `Flash` class provides three predefined methods for commonly used scenarios:
- `success(String message)`: Stores a success message under the key `success`
- `warning(String message)`: Stores a warning message under the key `warning`
- `error(String message)`: Stores an error message under the key `error`

Additionally, you can add custom key-value pairs to the flash storage using:

```java
flash.add("key", "value");
```

## Accessing Flash Messages in Templates

Flash messages are automatically available in templates, without needing to pass the class explicitly. You can retrieve values using:

```html
${flash.success}
${flash.warning}
${flash.error}
${flash.foo}
```

This allows seamless handling of flash messages in your views.
