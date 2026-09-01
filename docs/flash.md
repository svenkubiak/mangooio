# Flash

The **post/redirect/get** pattern avoids duplicate form submissions: after a POST you redirect to a GET page. Flash messages carry one-time feedback across that redirect—"Saved successfully" or validation errors—without putting text in the URL.

Flash data lives in its own JWT cookie, separate from the session. Values survive **exactly one** subsequent request, then the cookie is cleared. In Freemarker templates the built-in `flash` object exposes `success`, `warning`, and `error` slots plus arbitrary keys via `put()`.

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

`setSuccess`, `setWarning`, and `setError` store under the keys `success`, `warning`, and `error`. Use `put` for custom keys.

Keys and values must not contain spaces, `|`, `:`, or `&`.

## Templates

Flash is available in Freemarker without passing it from the controller:

```ftl
${flash.success}
${flash.warning}
${flash.error}
${flash.foo}
```

Cookie name, key, and secret are configured under `flash.cookie.*`. The Secure flag follows `session.cookie.secure`. See [Configuration](configuration.md).
