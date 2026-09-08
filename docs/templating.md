# Templating 🖼️

Server-rendered HTML uses [Freemarker](https://freemarker.apache.org/). Templates are plain `.ftl` files under `src/main/resources/templates/`. When a controller returns `Response.ok().render()`, the framework resolves `templates/<ControllerSimpleName>/<methodName>.ftl` unless you override the path with `template(...)`. The convention exists so that finding the template for a given endpoint never requires digging through routing code: the controller class and method name tell you exactly where to look.

Beyond simple `${variable}` substitution, mangoo I/O registers **built-in objects and methods** on every render: the current `form`, `session`, and `flash`; an `i18n(...)` helper; reverse routing via `route(...)`; and CSRF directives. These exist because almost every page needs at least one of them, and requiring every controller to pass them in manually would mean repeating the same few lines of code in every single method that renders a template.

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

Do not pass a controller value with the same name; it overwrites the built-in silently, and rendering can then fail in confusing ways further down the template, usually somewhere that has nothing to do with the actual name collision.

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

`location(...)` is true when the current request is mapped to that controller method (matched case-insensitively). It is mainly useful for things like highlighting the active tab in a navigation partial that gets included on every page:

```ftl
<#if location("ApplicationController:index")>current</#if>
```

## Reverse routes

```ftl
<a href="${route("ApplicationController:show")}">Show</a>
<a href="${route("ApplicationController:show", "42")}">Show 42</a>
```

If `show` is mapped to `/users/{id}`, the second example becomes `/users/42`. Building the URL this way, instead of writing `/users/42` directly in the template, means renaming a route in `Bootstrap.java` does not turn into a hunt through every template that happens to link to it.

## Internationalization

```ftl
${i18n("welcome")}
${i18n("hello", "Ada")}
```

See [Internationalization](internationalization.md) for how message keys and locale resolution work.
