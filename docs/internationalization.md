# Internationalization

User-visible strings belong in **resource bundles**, not hard-coded in templates or controllers. mangoo I/O loads `messages*.properties` from `src/main/resources/translations/` and resolves the active locale per request.

Templates call `${i18n("key")}` (or with arguments). Controllers inject `Messages` for the same keys in Java. Locale selection follows a fixed order: explicit `lang` parameter, then cookie, then `Accept-Language`, then the configured default.

## Locale order

1. **`lang` request parameter** (query or path). A path parameter must be mapped:

    ```java
    On.get().to("/page/{lang}").respondeWith("page")
    ```

    Examples: `/page?lang=en`, `/page/en`

2. **i18n cookie** (`i18n.cookie.name`, default `mangooio-i18n`)

3. **`Accept-Language` header**

4. **`application.language`** in `config.yaml` (default `en`)

5. Hardcoded fallback `en`

## Setting the cookie

```java
import io.mangoo.constants.Default;
import io.mangoo.routing.Response;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;

public Response localize() {
    Cookie cookie = new CookieImpl(Default.I18N_COOKIE_NAME, "en");
    return Response.ok().cookie(cookie).render();
}
```

## Messages in Java

```java
package controllers;

import io.mangoo.i18n.Messages;
import io.mangoo.routing.Response;
import jakarta.inject.Inject;

public class I18nController {
    @Inject
    private Messages messages;

    public Response translation() {
        String text = messages.get("welcome");
        String named = messages.get("hello", "Ada");
        return Response.ok().bodyText(text);
    }
}
```

```properties
welcome=Welcome
hello=Hello {0}
```

`{0}` is replaced with the extra argument.

You can also take `Messages` as a controller method parameter.

## Templates

```ftl
${i18n("welcome")}
${i18n("hello", "Ada")}
```

The varargs `Messages.get(key, args)` returns an empty string when the key is absent. See [Templating](templating.md).
