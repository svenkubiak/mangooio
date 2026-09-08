# Flash

The **post/redirect/get** pattern avoids duplicate form submissions: after a POST you redirect to a GET page instead of rendering directly, so a page refresh can't resubmit the form. Flash messages carry one-time feedback across that redirect, things like "Saved successfully" or validation errors, without stuffing the text into the URL as a query parameter.

Flash data lives in its own JWT cookie, separate from the session cookie. Values survive **exactly one** subsequent request, and then the cookie is cleared automatically. In Freemarker templates, the built-in `flash` object exposes `success`, `warning`, and `error` slots, plus arbitrary keys via `put()`.

Typical flow: set flash in the POST handler, `Response.redirect(...)`, then read flash in the GET template or controller.

```java
package controllers;

import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Flash;

public class FlashController {
    public Response save(Flash flash) {
        flash.setSuccess("Saved");
        flash.setWarning("Check the address");
        flash.setError("Could not send email");
        flash.put("foo", "bar");
        return Response.redirect("/");
    }
}
```

`setSuccess`, `setWarning`, and `setError` store under the fixed keys `success`, `warning`, and `error`. Reach for `put` when you need a custom key instead.

Keys and values must not contain spaces, `|`, `:`, or `&`.

## Templates

Flash is available in Freemarker automatically, with no need to pass it from the controller:

```ftl
${flash.success}
${flash.warning}
${flash.error}
${flash.foo}
```

Cookie name, key, and secret are configured under `flash.cookie.*`. The Secure flag follows `session.cookie.secure` rather than having its own setting. See [Configuration](configuration.md).
