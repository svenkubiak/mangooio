# Templating

Mangoo I/O leverages [Freemarker](https://freemarker.apache.org/) for rendering HTML pages.

## Template Variables

Mangoo I/O provides several default variables automatically assigned to templates, offering useful functionality out of the box:

```injectedfreemarker
Form
Flash
Session
i18n
Route
Location
Prettytime
```

!!! note
    These variable names cannot be passed into a template manually. Assigning a variable with the same name in your controller would overwrite the built-in ones, leading to rendering failures and exceptions.

## Pretty Time

**Pretty Time** allows you to format localized relative dates dynamically. For example, a date representing "yesterday" would be formatted as **"1 day ago"** based on the request's preferred locale.

### Usage

```injectedfreemarker
${prettytime(localDateTime)}  // Based on LocalDateTime
${prettytime(localDate)}      // Based on LocalDate
${prettytime(date)}           // Based on Date
```

## Location

**Location** helps determine the current URL accessing the template. This is useful for dynamic navigation highlighting.

### Usage

```injectedfreemarker
<#if location("ApplicationController:location")>DO SOMETHING</#if>
```

To validate the above, provide the **Controller** and **method**. The check is case-insensitive.

## Route

**Route** enables reverse routing, allowing URLs to be dynamically generated instead of being hardcoded. This ensures that links always align with mapped controller methods.

### Basic Usage

```injectedfreemarker
<a href="${route("ApplicationController:route")}">Route</a>
```

If `ApplicationController.class` contains a method `route()` mapped to `/route`, the above will render as:

```html
<a href="/route">Route</a>
```

### Passing Attributes

You can pass additional attributes to the function:

```injectedfreemarker
<a href="${route("ApplicationController:route", "foo")}">Route</a>
```

This outputs:

```html
<a href="/route/foo">Route</a>
```
