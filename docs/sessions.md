# Sessions 🍪

HTTP is stateless; **sessions** carry server-side state across requests without a database lookup on every hit. mangoo I/O stores session data in a **signed, encrypted JWT cookie**, meaning the state lives entirely on the client and the server never has to remember anything between requests (a share-nothing model). That fits horizontal scaling well: any node can verify and decode the cookie with the vault key, so there is no shared session store to keep in sync and no risk of a user's session vanishing because a load balancer sent their next request to a different instance.

Capacity is limited to roughly **4 KB** per cookie, which is a practical limit browsers themselves enforce, not a number mangoo I/O picked arbitrarily. Store identifiers and small flags, not large blobs; if you find yourself needing more than that, the data probably belongs in your database with just an id in the session pointing at it. Configure cookie name, signing key, and SameSite in [Configuration](configuration.md). Session lifetime is controlled by the JWT's own token TTL and `session.cookie.expires`, not an arbitrary day count set somewhere else, so the actual expiry behavior lives in exactly one place.

Pass `Session` into the controller method:

```java
package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Session;

public class SessionController {
    public Response session(Session session) {
        session.put("foo", "this is a session value");
        return Response.ok();
    }
}
```

## Methods

```java
session.put("key", "value");
session.get("key");
session.remove("key");
session.clear();       // empties values and invalidates the cookie
session.invalidate();  // expires the cookie
session.keep();        // keep the session even if it would otherwise be dropped
session.getCsrf();
session.hasContent();
```

`clear()` and `invalidate()` both end up removing the session cookie, but `clear()` also wipes the in-memory values for the rest of the current request, which matters if your code checks `session.get(...)` again later in the same method after calling it.

Keys and values must not contain spaces, `|`, `:`, or `&`. Those characters are used internally to encode the session as claims inside the JWT, so allowing them in your own values would make the encoding ambiguous to decode again.

## Cookie settings

```yaml
session:
  cookie:
    name: myapp-session
    expires: false
    token:
      expires: 3600
    secure: true
    samesitemode: Strict
    secret: vault{}
    key: vault{}
```

- `session.cookie.expires`: `false` (the default) keeps a browser-session cookie that disappears when the browser closes. `true` instead sets an actual expiry taken from `session.cookie.token.expires`, useful if you want a session to survive a browser restart.
- `session.cookie.token.expires`: JWT lifetime in **seconds** (default `3600`). Once this passes, the cookie decodes as expired regardless of what `session.cookie.expires` says about the cookie itself.
- Signing (`key`) and encryption (`secret`) fall back to `application.secret` if unset, which is fine for local development but not something to rely on in production. Use the [vault](secrets.md) instead, so each mode gets its own generated keys.

See [CSRF](csrf.md) for how the session holds the CSRF token used to protect form submissions.
