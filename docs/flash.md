# Flash

Flash values survive **one** subsequent request, then the cookie is discarded. Use them for success or error messages after a redirect.

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
