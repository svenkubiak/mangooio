# Sessions

HTTP is stateless; **sessions** carry server-side state across requests without a database lookup on every hit. mangoo I/O stores session data in a **signed, encrypted JWT cookie** (share-nothing). That fits horizontal scaling: any node can verify and decode the cookie with the vault key.

Capacity is limited to roughly **4 KB** per cookie—store identifiers and small flags, not large blobs. Configure cookie name, signing key, and SameSite in [Configuration](configuration.md). Session lifetime is controlled by token TTL and `session.cookie.expires`, not an arbitrary day count.

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

Keys and values must not contain spaces, `|`, `:`, or `&`.

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

- `session.cookie.expires`: `false` (default) keeps a browser-session cookie. `true` sets an expiry from `session.cookie.token.expires`.
- `session.cookie.token.expires`: JWT lifetime in **seconds** (default `3600`).
- Signing (`key`) and encryption (`secret`) fall back to `application.secret` if unset. Use the [vault](secrets.md) in production.

See [CSRF](csrf.md) for how the session holds the CSRF token.
