# Templating

mangoo I/O renders HTML with [Freemarker](https://freemarker.apache.org/). Templates live under `src/main/resources/templates/` and use the `.ftl` suffix. See [Controllers](controllers.md) for the naming convention.

## Built-in variables

These names are injected into every template:

```
form
flash
session
i18n
route
location
prettytime
```

Do not pass a controller value with the same name; it overwrites the built-in and rendering can fail.

CSRF helpers (see [CSRF](csrf.md)):

```ftl
<@csrfform/>
<@csrftoken/>
```

## Pretty time

Relative dates, localized from the request locale:

```ftl
${prettytime(localDateTime)}
${prettytime(localDate)}
${prettytime(date)}
```

## Location

True when the current request is mapped to that controller method (case-insensitive):

```ftl
<#if location("ApplicationController:index")>current</#if>
```

## Reverse routes

```ftl
<a href="${route("ApplicationController:show")}">Show</a>
<a href="${route("ApplicationController:show", "42")}">Show 42</a>
```

If `show` is mapped to `/users/{id}`, the second example becomes `/users/42`.

## Internationalization

```ftl
${i18n("welcome")}
${i18n("hello", "Ada")}
```

See [Internationalization](internationalization.md).
