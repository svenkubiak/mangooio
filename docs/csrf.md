# Cross-Site Request Forgery (CSRF) protection

mangoo I/O provides mechanisms to protect against CSRF with minimal configuration.

## Origin check

The OriginFilter provides a fluent way to check a request’s origin against a predefined configuration.
Add @FilterWith(OriginFilter.class) to any controller class or method, and the request will be validated against the
allowedOrigins value in your config.yaml.

```yaml
application:
  allowedOrigins: http://foo.de, http://bar.de
```

You can specify multiple values for this configuration option. If a controller class or method is annotated with
OriginFilter.class and the request’s Origin header does not match any configured value, the server responds
with 403 – Forbidden.

## CSRF-Token Check

To simplify CSRF token integration, mangoo I/O provides convenient methods to make the process as easy as possible. To protect any controller class or method, use the following filter:

```java
@FilterWith(CsrfFilter.class)
```

Once added, the filter checks whether the X-CSRF-TOKEN header or a form parameter named X-CSRF-TOKEN is present, and validates it against the user’s session. To ensure a user has a valid CSRF token when making a request to a CsrfFilter-protected endpoint, use the following template function:

```yaml
<@csrfform/>  // Outputs <input type="hidden" value="$CSRF_TOKEN" name="x-csrf-token" /> for direct usage in Forms 
<@csrftoken/> // Adds the raw CSRF Token, e.g. for JavaScript request or non-Form usage
```

Once you add either of these functions, a session will be created for the user, and a session cookie will be sent. mangoo I/O automatically retrieves the cookie from each request and validates it against the submitted form or header value. A good time to create the session is at the start of any specific user session, such as on the login form. To invalidate a session and force the creation of a new CSRF token, you must invalidate the session via a controller.

```java
public Response logout(Session session) {
    session.clear();
    return Response.ok();
}
```
A good time to clear the session is at the end of a specific user session, such as during logout.
