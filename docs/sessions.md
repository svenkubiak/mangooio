With a Shared nothing architecture in place mangoo I/O uses a so called client-side session. This means, that all information for a specific user is stored on the client-side inside a cookie. The big advantage of this concept is, that you can scale your application very easy. The downside of this architecture is, that you can only store limited data in the cookie \(around 4k of data\).

To make use of the mangoo I/O session, you can just pass the Session class into your controller method.

```java
package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Session;

public class SessionController {
    public Response session(Session session) {
        session.add("foo", "this is a session value");
        return Response.withOk().andEmptyBody();
    }
}
```

The Session class offers you some convenient methods for adding, deleting or completly erasing session data.

By default the session cookie has a lifespan of one day \(86400 seconds\). This, a long with the name of the cookie, can be configure using the following properties in the config.props file

```properties
[session]
	cookie.expires = 86400
	cookie.name = My-Session
```


