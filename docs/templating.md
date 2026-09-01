# Templating

Server-rendered HTML uses [Freemarker](https://freemarker.apache.org/). Templates are plain `.ftl` files under `src/main/resources/templates/`. When a controller returns `Response.ok().render()`, the framework resolves `templates/<ControllerSimpleName>/<methodName>.ftl` unless you override the path with `template(...)`.

Beyond simple `${variable}` substitution, mangoo I/O registers **built-in objects and methods** on every render: the current `form`, `session`, and `flash`; an `i18n(...)` helper; reverse routing via `route(...)`; and CSRF directives. That reduces boilerplate in layouts and partials.

See [Controllers](controllers.md) for how to pass model data with `.render("key", value)`. For forms and CSRF, combine templating with [Forms](forms.md) and [CSRF](csrf.md).

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
