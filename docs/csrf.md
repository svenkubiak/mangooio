# Cross-Site Request Forgery (CSRF)

CSRF attacks trick a logged-in browser into submitting a request the user did not intend—for example, a hidden form on another site that posts to your `/transfer` endpoint. mangoo I/O mitigates this by requiring a **secret token** that only your pages know, stored in the [session](sessions.md) and submitted with state-changing requests.

Protection is layered: **`OriginFilter`** can reject requests whose `Origin` header is not on your allow list; **`CsrfFilter`** validates the `x-csrf-token` field (or header) against the session value. HTML forms should use the Freemarker `<@csrfform/>` or `<@csrftoken/>` directives from [Templating](templating.md).

## Origin check

`OriginFilter` compares the `Origin` header to `application.allowedOrigins`:

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

A missing or unknown origin returns HTTP 403.

## CSRF token

Protect a class or method:

```java
@FilterWith(CsrfFilter.class)
```

The filter accepts the token in the `x-csrf-token` HTTP header or as a form field named `x-csrf-token`. Invalid tokens return HTTP 403.

Emit the token in Freemarker:

```ftl
<@csrfform/>
<@csrftoken/>
```

`<@csrfform/>` writes a hidden input. `<@csrftoken/>` writes the raw token (for JavaScript). Either directive keeps the session so the token survives the round-trip.

Create the session when the user starts a flow (for example the login form). Clear it on logout:

```java
public Response logout(Session session) {
    session.clear();
    return Response.redirect("/");
}
```
