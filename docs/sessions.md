# Sessions

mangoo I/O employs a **client-side session** approach within its shared-nothing architecture. This means all session data for a user is stored inside a cookie on the client side.

## Advantages and Limitations

- **Scalability**: Since session data is stored on the client, scaling the application becomes seamless.
- **Storage Limitation**: Cookie storage is limited to approximately **4KB**, restricting the amount of session data.

## Using Sessions in mangoo I/O

To work with sessions, simply pass the `Session` class into your controller method:

```java
package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Session;

public class SessionController {
    public Response session(Session session) {
        session.add("foo", "this is a session value");
        return Response.ok();
    }
}
```

## Session Management

The `Session` class provides convenient methods for:

- **Adding session data**: `session.add("key", "value");`
- **Removing a session entry**: `session.remove("key");`
- **Clearing all session data**: `session.clear();`

## Configuring Session Properties

By default, the session cookie has a lifespan of **one day (86,400 seconds)**. The session expiration and cookie name can be modified in the `config.yaml` file:

```properties
session:
    cookie:
        expires: 86400
        name: My-Session
```

This allows customization of session behavior based on application needs.
