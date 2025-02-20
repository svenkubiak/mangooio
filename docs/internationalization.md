# Internationalization

Translations in mangoo I/O are based on the standard `Locale` of Java.

## Locale Determination Order

The `Locale` is determined in the following order:

**Language Parameter in the URL**  
   The language can be specified as either a URL parameter or a query parameter, e.g.:

   ```java
   /my/path?lang=en
   /my/path/en
   ```

   If a URL parameter is used, the URL must be configured accordingly in the `Bootstrap` class:

   ```java
   On.get().to("/foo/bar/{lang}").respondWith("foobar");
   ```

**i18n Cookie in the Request**  
   A language preference can be stored in a cookie. You can create such a cookie using the `CookieBuilder`, e.g.:

   ```java
   public Response localize() {
       Cookie cookie = CookieBuilder.create()
               .name(Default.COOKIE_I18N_NAME.toString())
               .value("en")
               .build();
   
       return Response.withOk().andCookie(cookie);
   }
   ```

**Accept-Language Header in the Request**  
   The language is determined from the `Accept-Language` header sent by the client.

**Default application language**

The application’s default language can be set in the configuration file:

   ```yaml
   application:
      language: en
   ```

**Hardcoded Default Value**  
   If no other sources define a language, the application defaults to `en`.

## Accessing Translated Values

mangoo I/O provides a convenient way to access translations by injecting the `Messages` class.

```java
package controllers;

import com.google.inject.Inject;
import io.mangoo.i18n.Messages;
import io.mangoo.routing.Response;

public class I18nController {

    @Inject
    private Messages messages;

    public Response translation() {
        messages.get("my.translation");
        messages.get("my.othertranslation", "foo");
        ...
    }
}
```

The `Messages` class offers two methods for retrieving translations from the resource bundle: with or without optional parameters.

Example resource bundle entries:

```properties
my.translation=This is a translation
my.othertranslation=This is a translation with the parameter: {0}
```

Note: `{0}` is a placeholder that will be replaced by the passed parameter (`"foo"`).

## Translation in Templates

To access translations in a template, use the special `i18n` tag along with the translation key:

```html
${i18n("my.translation")}
```

To pass a parameter to the translation, append it to the function call:

```html
${i18n("my.othertranslation", "foo")}
```

If no matching key is found in the resource bundle, the template will output an empty value.
