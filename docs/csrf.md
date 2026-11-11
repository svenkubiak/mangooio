# Cross-Site Request Forgery (CSRF) 🛡️

A CSRF attack submits a request from another site while the browser attaches your cookies. mangoo I/O requires a token that only your pages can read, stored in the [session](sessions.md) and sent back with the request. The other site can trigger the request. It cannot read the session cookie.

Protection is layered: **`OriginFilter`** can reject requests whose `Origin` header is not on your allow list before anything else runs; **`CsrfFilter`** then validates the `x-csrf-token` field (or header) against the session value. HTML forms should use the Freemarker `<@csrfform/>` or `<@csrftoken/>` directives from [Templating](templating.md) rather than wiring the token in by hand.

## Origin check

`OriginFilter` compares the `Origin` header to `application.allowedOrigins`. This is a coarser check than the token itself: it does not require anything from the session, just that the browser says it is calling from a domain you trust.

```yaml
application:
  allowedOrigins: http://foo.example, http://bar.example
```

```java
@FilterWith(OriginFilter.class)
public class AccountController {
    public Response save() {
        return Response.ok();
    }
}
```

A missing or unknown origin returns HTTP 403 with an empty body.

## CSRF token

Protect a class or method:

```java
@FilterWith(CsrfFilter.class)
```

The filter accepts the token in the `x-csrf-token` header or in a form field of the same name. The header wins when both are present. The comparison is constant-time. A mismatch returns HTTP 403 and the built-in HTML page.

Emit the token in Freemarker:

```ftl
<@csrfform/>
<@csrftoken/>
```

`<@csrfform/>` writes a hidden input. `<@csrftoken/>` writes the raw token, for a script that sends it as a header. The token is 32 random characters, created the first time it is read on a session that was not restored from a cookie. The directives call `session.keep()` and then read the token, which writes that cookie when the session was not restored from an existing one.

Clear the session on logout so a stale token cannot be reused after the user has signed out:

```java
public Response logout(Session session) {
    session.clear();
    return Response.redirect("/");
}
```
