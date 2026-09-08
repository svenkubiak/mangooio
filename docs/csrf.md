# Cross-Site Request Forgery (CSRF) 🛡️

CSRF attacks trick a logged-in browser into submitting a request the user did not intend, for example a hidden form on another site that quietly posts to your `/transfer` endpoint while the victim thinks they are just browsing. The browser happily attaches the user's cookies to that request, since cookies are sent per origin regardless of which page triggered the request, which is exactly the gap mangoo I/O closes here. It mitigates this by requiring a **secret token** that only your pages know, stored in the [session](sessions.md) and submitted alongside state-changing requests. An attacker's page can trigger the request, but it cannot read your session cookie to get the token out, so it has no way to attach a valid one.

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

A missing or unknown origin returns HTTP 403.

## CSRF token

Protect a class or method:

```java
@FilterWith(CsrfFilter.class)
```

The filter accepts the token in the `x-csrf-token` HTTP header or as a form field named `x-csrf-token`, so it works equally well for a classic form submission and for an AJAX call from your own frontend JavaScript. Invalid tokens return HTTP 403.

Emit the token in Freemarker:

```ftl
<@csrfform/>
<@csrftoken/>
```

`<@csrfform/>` writes a hidden input, ready to submit with a regular HTML form. `<@csrftoken/>` writes just the raw token value, meant for JavaScript to read and attach as a header on its own requests. Either directive relies on the session already existing, since that is where the token itself lives; if there is no session yet, there is nothing to validate against.

Create the session when the user starts a flow that will need protecting later, for example when they load the login form, and clear it on logout so a stale token cannot be reused after the user has signed out:

```java
public Response logout(Session session) {
    session.clear();
    return Response.redirect("/");
}
```
