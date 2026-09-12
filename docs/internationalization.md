# Internationalization

User-visible strings belong in **resource bundles**, not hard-coded inside templates or controllers. mangoo I/O loads `messages*.properties` from `src/main/resources/translations/` and resolves the active locale for you on every request.

Templates call `${i18n("key")}`, with arguments if you need them. Controllers take `Messages` as a method parameter to reach the same keys from Java. Locale selection follows a fixed order: an explicit `lang` parameter first, then the cookie, then the `Accept-Language` header, and finally the configured default.

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

Take `Messages` as a controller method parameter. That instance is created per request and carries the locale resolved by the order above.

```java
package controllers;

import io.mangoo.i18n.Messages;
import io.mangoo.routing.Response;

public class I18nController {
    public Response translation(Messages messages) {
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

`{0}` is replaced with the extra argument, and further placeholders follow the same pattern for additional arguments.

> Do not inject `Messages` as a field to translate a request. An injected `Messages` is an application-wide singleton bound to the JVM default locale, not to the locale of the current request. Only the method parameter carries the request locale.

## Messages outside of a request

Background jobs, scheduled tasks and emails have no request locale to fall back on. Create an instance for the locale you actually want, for example the language stored on the recipient:

```java
var messages = new Messages(Locale.of(user.getLanguage()));

Mail.newMail()
        .from(from)
        .subject(messages.get("email.forgot.password.subject"))
        .to(user.getUsername())
        .textMessage("emails/forgot_password.ftl", Map.of("messages", messages))
        .send();
```

The locale is fixed for the lifetime of an instance and readable via `getLocale()`. A locale without a matching `messages_xx.properties` falls back to the base `messages.properties`, never to the JVM default locale.

## Templates

```ftl
${i18n("welcome")}
${i18n("hello", "Ada")}
```

The varargs `Messages.get(key, args)` returns an empty string when the key is absent, rather than throwing, so a missing translation degrades quietly instead of breaking the page. See [Templating](templating.md).
