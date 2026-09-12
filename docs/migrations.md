## From 10.12.0 to 10.12.1

Mostly a drop-in replacement, with one behaviour change around request parameters and one removed method on `Messages`.

### Request parameters

A query parameter can no longer override a route parameter of the same name. Previously a request to `/users/1?id=2` bound `id` to `2`, which allowed a client to forge any route parameter. The route value now always wins, so the same request binds `id` to `1`.

Two consequences to check in your application:

* If you passed a route parameter through the query string, that no longer works. On a route `/users/{id}`, a request to `/users/?id=1` now yields an empty `id` rather than `1`. Give such endpoints a route without a placeholder.
* If you derive an operation from the *presence* of a parameter, switch to `request.hasPathParameter(key)`. A null check on `request.getParameter(key)` cannot tell a route parameter from a query parameter a client appended, which makes it unsuitable for authorization decisions.

New in this release: `Request#getPathParameter`, `Request#getQueryParameter`, `Request#hasPathParameter` and the `application.parameter.strict` option, which rejects ambiguous requests with a `400` instead of letting the route value win. It defaults to `false` in 10.x and will default to `true` in 11.0.

### Messages

`Messages#reload(Locale)` has been removed. It mutated the shared `Messages` singleton per request, so the locale of one request could leak into concurrent requests, and it called `Locale.setDefault(Locale.ROOT)`, changing the JVM default locale for the whole application.

Messages are now resolved per request: the locale is passed to the constructor and is immutable for the lifetime of an instance. Replace any call to `reload` by creating an instance instead:

```java
// before
messages.reload(locale);

// now
var messages = new Messages(locale);
```

The locale of an instance is available via `Messages#getLocale`. Bundle resolution no longer falls back to the JVM default locale; a locale without a matching `messages_xx.properties` falls back to the base `messages.properties`.

## From 10.11.6 to 10.12.0

This is a drop-in replacement.

## From 10.11.5 to 10.11.6

This is a drop-in replacement.

## From 10.11.4 to 10.11.5

This is a drop-in replacement.

## From 10.11.3 to 10.11.4

This is a drop-in replacement.

## From 10.11.2 to 10.11.3

This is a drop-in replacement.

## From 10.11.1 to 10.11.2

This is a drop-in replacement.

## From 10.10.0 to 10.11.1

This is a drop-in replacement.

## From 10.9.0 to 10.10.0

This is a drop-in replacement.

## From 10.8.0 to 10.9.0

This is a drop-in replacement.

## From 10.7.0 to 10.8.0

This is a drop-in replacement.

## From 10.6.0 to 10.7.0

This is a drop-in replacement.

## From 10.5.0 to 10.6.0

This is a drop-in replacement.

## From 10.4.0 to 10.5.0

This is a drop-in replacement.

## From 10.3.0 to 10.4.0

This is a drop-in replacement.

## From 10.2.0 to 10.3.0

This is a drop-in replacement.

## From 10.1.0 to 10.2.0

This is a drop-in replacement.

## From 10.0.0 to 10.1.0

This is a drop-in replacement.

## From 9.x to 10.0.0
mangoo I/O 10.0.0 is a major release and contains changes that break API compatibility. These are the changes you need to consider when upgrading from 9.x:

**Removed cryptex{}**

`cryptex{}` in `config.yaml` is replaced by a vault based on a Java KeyStore. On first start the application creates `vault.p12` with predefined cookie keys. See [Secrets](secrets.md) for `vault{}`, vault location, and HTTPS certificates.

**Switched Paseto to JWT**

Cookie tokens now use Nimbus JOSE+JWT instead of Paseto. If you called the Paseto library yourself, either keep using that library directly or switch to [JwtUtils](utils.md).

## From 9.9.0 to 9.10.0
This is a drop-in replacement.

## From 9.8.0 to 9.9.0
This is a drop-in replacement.

## From 9.7.0 to 9.8.0
This is a drop-in replacement.

## From 9.6.0 to 9.7.0
This is a drop-in replacement.

## From 9.5.0 to 9.6.0
This is a drop-in replacement.

## From 9.4.0 to 9.5.0
This is a drop-in replacement.

## From 9.3.0 to 9.4.0
This is a drop-in replacement.

## From 9.2.0 to 9.3.0
This is a drop-in replacement.

## From 9.1.0 to 9.2.0
This is a drop-in replacement.

## From 9.0.0 to 9.1.0
This is a drop-in replacement.

## From 8.11.0 to 9.0.0
mangoo I/O 9.0.0 is a major release and contains code that will break API compatibility. These are the changes you need to consider when upgrading from 8.x:

**Removed Basic HTTP authentication**

The basic HTTP authentication that came with mangoo I/O has been removed. This should be done in an HTTP proxy in front of your application instead.

**Refactored Response class**

The Response class and the handling of a response in a controller has been changed. Previously, when a Response was returned in a controller, mangoo I/O automatically looked up the corresponding .ftl template and rendered it. Now, returning `Response.ok()` returns an empty response. Rendering only takes place when calling `Response.ok().render()` or when passing a variable to the template via `Response.ok().render("foo", "bar")`.

**Removed @admin/health endpoint**

The @admin/health endpoint is not available anymore.

**Switched from props based configuration to yaml based configuration**

Please check the [updated documentation](configuration.md) for further details.

## From 8.10.0 to 8.11.0
This is a drop-in replacement.

## From 8.9.0 to 8.10.0
This is a drop-in replacement.

## From 8.8.0 to 8.9.0
This is a drop-in replacement.

## From 8.7.0 to 8.8.0
This is a drop-in replacement.

## From 8.6.0 to 8.7.0
This is a drop-in replacement.

## From 8.5.0 to 8.6.0
This is a drop-in replacement.

## From 8.4.0 to 8.5.0
This is a drop-in replacement.

## From 8.3.0 to 8.4.0
This is a drop-in replacement.

## From 8.2.0 to 8.3.0
This is a drop-in replacement.

## From 8.1.0 to 8.2.0
This is a drop-in replacement.

## From 8.0.0 to 8.1.0
This is a drop-in replacement.

## From 7.19.0 to 8.0.0
mangoo I/O 8.0.0 is a major release and contains code that will break API compatibility. These are the changes you need to consider when upgrading from 7.x:

**Java 21**

mangoo I/O now requires and uses Java 21.

**Removed Morphia in favor of direct MongoDB integration**

One of the major changes in 8.0.0 is the removal of Morphia in favor of a direct MongoDB integration. mangoo I/O now works with the native MongoDB Java driver. See [Persistence](persistence.md).