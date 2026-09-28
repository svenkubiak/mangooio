## From 10.12.2 to 10.13.0

A drop-in replacement in terms of API, with a changed meaning of `Authentication#isValid`, three behaviour changes around the failed attempt budget in `Authentication`, and a new limit on concurrent Argon2 hashing.

### What has to change in a controller

A login flow **without** two-factor authentication needs no change at all. `isValidLogin(identifier, password, salt, hash)`, `login(subject)` and `rememberMe()` keep their signature and their meaning.

A flow **with** two-factor authentication has exactly two places to touch, both on the page that accepts the TOTP:

```java
// before
if (authentication.isValid() && authentication.isValidSecondFactor(secret, totp)) {

// now
if (authentication.hasSubject() && authentication.isValidSecondFactor(subject, secret, totp)) {
```

`isValid()` is false while a second factor is outstanding, so it no longer works as the guard of that page, and the two-argument `isValidSecondFactor` is deprecated because it verifies the code unthrottled. See [isValid now means fully authenticated](#isvalid-now-means-fully-authenticated) and [Second factor throttling](#second-factor-throttling) for the reasoning.

Three things that look like they might have changed, but did not:

- **The lock handling stays inside the framework.** `isValidLogin` and `isValidSecondFactor` check the lock before doing any work and count the failed attempt afterwards, on their own, exactly as before. `userHasLock` and `userHasSecondFactorLock` are read-only queries for the error message, not a step you have to add.
- **`update()` is needed in the same place as before.** The authentication cookie is written when none exists or when `update()` asked for it, which is unchanged. The second factor step still has to call it so the cleared flag reaches the cookie.
- **Authorization checks stay as they are.** `isValid()` becomes stricter on its own; that is the point of redefining it rather than adding a new method.

What does change without any code edit is the behaviour of the failed attempt budget, see the three sections below, and the fact that `isValidLogin` can now return `false` because the machine is out of hashing slots rather than because the password was wrong, see [Argon2 hashing is now gated](#argon2-hashing-is-now-gated).

### isValid now means fully authenticated

`isValid()` used to be nothing but `isNotBlank(subject)`. A subject is set as soon as the password step has succeeded, which is also when the authentication cookie is issued — at that point a required second factor is still outstanding. The name reads as "this authentication is valid", and an `Authentication` object is bound on every request, not only on routes bound with `withAuthentication()`. Application code that builds its own filter or derives authorization from `getSubject()` on an unbound route therefore granted access to a visitor who knew the password but not the second factor.

`isValid()` now additionally requires that no second factor is outstanding:

```java
// before, and still available under the new name
authentication.hasSubject();   // isNotBlank(subject)

// now
authentication.isValid();      // hasSubject() && !isTwoFactor()
```

Nothing has to be changed for authorization checks; they become stricter on their own, which is the point of redefining the existing method rather than adding a new one.

Code that runs **during** the second factor step has to switch. The page that accepts the TOTP sees an authentication whose subject is set and whose second factor is still pending, so `isValid()` is false there:

```java
// before
if (authentication.isValid() && authentication.isValidSecondFactor(subject, secret, totp)) {

// now
if (authentication.hasSubject() && authentication.isValidSecondFactor(authentication.getSubject(), secret, totp)) {
```

Routes bound with `withAuthentication()` are unaffected and keep redirecting to `authentication.redirect.login` when no subject is present and to `authentication.redirect.mfa` when the second factor is pending.

### Second factor throttling

`isValidSecondFactor(secret, totp)` verifies a TOTP without any limit on the number of attempts. A TOTP has six digits and is checked without a tolerance window, so exactly one of a million codes is valid per 30-second window — that is only a second factor as long as something bounds how often it may be guessed. An upstream reverse proxy counts requests per source address and does not bound the total, which an attacker sidesteps by spreading the attempts over more addresses.

The method still exists but is deprecated. Pass the identifier the code is checked for and the same `authentication.lock` budget as for the password step applies:

```java
// before
authentication.isValidSecondFactor(secret, totp);

// now
authentication.isValidSecondFactor(subject, secret, totp);
```

Use `userHasSecondFactorLock(identifier)` to query that lock; `userHasLock(identifier)` keeps referring to the password step only. Both steps are counted under separate keys, so neither consumes the budget of the other.

### Lockouts are absolute

A lockout used to rely on the auth cache TTL, which was reset on every write — and every failed attempt is a write. An attacker who never guessed the code could therefore keep the rightful owner of an account locked out indefinitely, one failed attempt every 59 minutes being enough, turning the protection into a denial of service against the account.

The unlock timestamp is now stored with the counter, set once when the budget is used up, and not extended by further failed attempts. Its duration is configurable through the new `authentication.lock.duration` in minutes, default 60, which matches the previous cache TTL.

### authentication.lock is off by one

`authentication.lock` locked one attempt later than its value suggested. With the default of `10`, the lock took effect after the eleventh failed attempt. It now takes effect after the tenth, so the value is the number of failed attempts that are allowed. Raise the value by one if you depended on the old count.

### Argon2 hashing is now gated

A single Argon2id computation holds around 78 MB of heap for as long as it runs, and nothing limited how many of them could run at the same time. Undertow starts with eight worker threads per core, so on an eight core machine 64 requests can sit in a hash at once — roughly 5 GB of heap demand. A handful of parallel logins is enough to take a small heap down with an OutOfMemoryError.

The hashing now runs through the new `io.mangoo.crypto.PasswordHasher`, which caps the number of concurrent computations. `CommonUtils.hashArgon2` and `CommonUtils.matchArgon2` keep their signature and delegate, so no call site has to change. What does change is that they can now throw:

```java
// unchecked, no signature change, but callers under load will see it
throw new MangooHashingException("No Argon2 hashing slot became available within 5000 ms");
```

`Authentication#isValidLogin` catches it, logs it and returns `false` — the login path stays fail-closed, and the failed attempt budget is not touched, so an overload situation cannot lock a user out. Every other caller gets the exception passed through to the `ExceptionHandler`. For registration or a password change that is the honest outcome: a `503` rather than a silent `false` that looks like "wrong password".

The default `authentication.hashing.concurrency: 0` derives the number of slots from the configured memory cost and half of the heap, clamped to a range of 2 to 8. The effective value is logged on INFO at startup. Raise it if your application hashes on more paths than login, or set it high to effectively switch the gate off:

```yaml
authentication:
  hashing:
    concurrency: 0
    timeout: 5000
```

If your application built this gating itself — a semaphore around the hashing calls, sized from a hardcoded copy of the framework's memory constant — remove it in the same step. Double gating is the worst of both worlds: the two limits multiply into unnecessary waiting, and the application side copy goes silently wrong the moment the framework changes its memory cost.

### Argon2 parameters are configurable

`authentication.hashing.memory` (80000 KiB), `authentication.hashing.iterations` (6) and `authentication.hashing.parallelism` (2) expose the Argon2id parameters. The defaults are exactly the values the framework used before, so nothing changes unless you set them.

!!! warning
    Changing any of these three invalidates every hash that has already been stored. The affected users can no longer log in, and there is no way to recover the old hash. A change is only feasible together with a mechanism that rehashes the password on the next successful login, while still verifying against the old parameters until that has happened.

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