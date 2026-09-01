# Cross-Site Request Forgery (CSRF)

mangoo I/O stores a CSRF token in the [session](sessions.md) cookie and checks it on protected endpoints.

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
