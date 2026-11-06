# Templating 🖼️

Server-rendered HTML uses [Freemarker](https://freemarker.apache.org/). Templates are `.ftl` files under `src/main/resources/templates/`. `Response.ok().render()` resolves `templates/<ControllerSimpleName>/<methodName>.ftl` unless you pass `template(...)`.

The engine uses HTML output format, so `${...}` is escaped. Raw markup needs `?no_esc`. `?api` is enabled. The number format is `0.######`. In dev mode templates are checked about once a second. A `.ftl` change does not restart `mangooio:run`. Outside dev mode the template cache does not expire.

Every render gets `form`, `session`, `flash`, `i18n(...)`, `route(...)`, `location(...)`, `prettytime(...)`, and the CSRF directives. Do not pass a model value with one of those names. The built-in replaces it.

See [Controllers](controllers.md) for how to pass model data with `.render("key", value)`. For forms and CSRF, combine templating with [Forms](forms.md) and [CSRF](csrf.md).

## Built-in variables

These names are injected into every template automatically:

```
form
flash
session
i18n
route
location
prettytime
```

A model value with one of these names is replaced by the built-in. The failure usually shows up later in the template, not on the `render` call.

CSRF helpers (see [CSRF](csrf.md)):

```ftl
<@csrfform/>
<@csrftoken/>
```

## Pretty time

Turns a date or timestamp into a relative description ("3 hours ago"), localized from the request locale rather than a fixed locale, so the same template reads naturally for users in different languages:

```ftl
${prettytime(localDateTime)}
${prettytime(localDate)}
${prettytime(date)}
```

## Location

`location(...)` is true when the current request is mapped to that controller method. The match is case-insensitive. `location("ApplicationController")` with no method is true for every method on that controller:

```ftl
<#if location("ApplicationController:index")>current</#if>
```

## Reverse routes

```ftl
<a href="${route("ApplicationController:show")}">Show</a>
<a href="${route("ApplicationController:show", "42")}">Show 42</a>
```

If `show` is mapped to `/users/{id}`, the second example becomes `/users/42`. Pass one argument per placeholder. A missing argument fails the render. The key is `simpleName:method`, case-insensitive. Two controllers with the same simple name share one reverse route.

## Internationalization

```ftl
${i18n("welcome")}
${i18n("hello", "Ada")}
```

See [Internationalization](internationalization.md) for how message keys and locale resolution work.
