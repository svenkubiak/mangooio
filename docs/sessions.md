# Sessions 🍪

mangoo I/O stores session data in an encrypted and signed JWT cookie. Any instance that has the same cookie key, the same cookie secret, and the same `application.name` can read it. There is no server-side session store.

Browsers cap a cookie at about **4 KB**. Store an id and a few flags, and keep the rest in the database. Cookie name, signing key, and SameSite are in [Configuration](configuration.md). Lifetime is `session.cookie.token.expires` together with `session.cookie.expires`. There is no sliding expiry: a rewrite keeps the original `exp`.

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
session.clear();       // empties values and expires the cookie
session.invalidate();  // expires the cookie, values stay readable
session.keep();        // persist a session that was created on this request
session.getCsrf();
session.hasContent();
```

A new cookie is written when `put`, `remove`, or `clear` changed the values, or when `keep()` was called on a session that was not restored from a cookie. `keep()` is what `<@csrfform/>` and `<@csrftoken/>` call so a freshly created CSRF token is actually stored. It does not refresh `exp`, and it does nothing useful for a session that was already restored.

`clear()` and `invalidate()` both send the cookie with `Max-Age: 0`. `clear()` replaces the value map. `getCsrf()` still returns the token for the rest of this request. `invalidate()` only marks the cookie for expiry.

The keys `iss`, `aud`, `sub`, `iat`, `nbf`, `exp`, `jti` and `x-csrf-token` are reserved, since the session is stored as claims inside the JWT; `put()` rejects them with an `IllegalArgumentException`.

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
    secret: env{}
    key: env{}
```

- `session.cookie.expires`: `false` (the default) keeps a browser-session cookie that disappears when the browser closes. `true` instead sets an actual expiry taken from `session.cookie.token.expires`, useful if you want a session to survive a browser restart.
- `session.cookie.token.expires`: JWT lifetime in **seconds** (default `3600`). Once this passes, the cookie decodes as expired regardless of what `session.cookie.expires` says about the cookie itself.
- The cookie is always `HttpOnly` and `Path=/`. `samesitemode` must be `Strict` or `Lax`. Anything else fails startup. The default is `Strict`.
- `session.cookie.secret` is exactly 64 bytes. `session.cookie.key` is at least 64 bytes. Both fall back to `application.secret`. JWT checks allow 30 seconds of clock skew. Set dedicated values with `env{}`, `arg{}`, or `file{}`. See [Secrets](secrets.md).

See [CSRF](csrf.md) for how the session holds the CSRF token used to protect form submissions.
