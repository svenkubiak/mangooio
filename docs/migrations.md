## From 10.14.1 to 10.15.0

This is a drop-in replacement.

## From 10.13.1 to 10.14.0/1

This is a drop-in replacement.

## From 10.13.0 to 10.13.1

This is a drop-in replacement.

## From 10.12.2 to 10.13.0

API-compatible, with a changed meaning of `Authentication#isValid`, three behaviour changes around the failed attempt budget, and a new limit on concurrent Argon2 hashing.

### What has to change in a controller

A login flow **without** two-factor authentication needs no change. With two-factor authentication, two things change on the page that accepts the TOTP:

```java
// before
if (authentication.isValid() && authentication.isValidSecondFactor(secret, totp)) {

// now
if (authentication.hasSubject() && authentication.isValidSecondFactor(subject, secret, totp)) {
```

`isValid()` is false while a second factor is outstanding, so it no longer guards that page, and the two-argument `isValidSecondFactor` is deprecated because it verifies unthrottled.

Unchanged: lock handling stays inside the framework — `isValidLogin` and `isValidSecondFactor` check the lock and count the failed attempt on their own, `userHasLock` and `userHasSecondFactorLock` are read-only queries for the error message. `update()` is needed in the same place as before, and authorization checks stay as they are; they become stricter on their own.

What changes without any code edit is the failed attempt budget, see the three sections below, and the fact that `isValidLogin` can return `false` because the machine is out of hashing slots rather than because the password was wrong.

### isValid now means fully authenticated

`isValid()` used to be nothing but `isNotBlank(subject)`, which is already true after the password step while a required second factor is still outstanding. Since an `Authentication` is bound on every request, not only on routes bound with `withAuthentication()`, application code with its own filter granted access to a visitor who knew the password but not the second factor.

```java
authentication.hasSubject();   // the old isValid(): isNotBlank(subject)
authentication.isValid();      // now: hasSubject() && !isTwoFactor()
```

Routes bound with `withAuthentication()` are unaffected and keep redirecting to `authentication.redirect.login` when no subject is present and to `authentication.redirect.mfa` when the second factor is pending.

### Second factor throttling

`isValidSecondFactor(secret, totp)` verified a TOTP without any attempt limit — six digits, no tolerance window, so one of a million codes is valid per 30-second window. A reverse proxy counting per source address does not bound the total, which an attacker sidesteps with more addresses.

The method is deprecated. Pass the identifier the code is checked for and the same `authentication.lock` budget as for the password step applies:

```java
// before
authentication.isValidSecondFactor(secret, totp);

// now
authentication.isValidSecondFactor(subject, secret, totp);
```

Query that lock with `userHasSecondFactorLock(identifier)`; `userHasLock(identifier)` keeps referring to the password step only. Both are counted under separate keys, so neither consumes the budget of the other.

### Lockouts are absolute

A lockout used to rely on the auth cache TTL, which every failed attempt reset — an attacker could keep the rightful owner locked out indefinitely with one attempt every 59 minutes. The unlock timestamp is now stored with the counter, set once when the budget is used up, and not extended by further attempts. Its duration is configurable through the new `authentication.lock.duration` in minutes, default 60, matching the previous cache TTL.

### authentication.lock is off by one

`authentication.lock` locked one attempt later than its value suggested: with the default of `10`, after the eleventh failed attempt. It now takes effect after the tenth, so the value is the number of failed attempts that are allowed. Raise it by one if you depended on the old count.

### Argon2 hashing is now gated

A single Argon2id computation held around 78 MB of heap with nothing limiting concurrency. Undertow starts eight worker threads per core, so on an eight core machine 64 requests can hash at once — roughly 5 GB, enough to take a small heap down with an OutOfMemoryError.

Hashing now runs through the new `io.mangoo.crypto.PasswordHasher`, which caps concurrent computations. `CommonUtils.hashArgon2` and `CommonUtils.matchArgon2` keep their signature and delegate, but can now throw:

```java
// unchecked, no signature change, but callers under load will see it
throw new MangooHashingException("No Argon2 hashing slot became available within 5000 ms");
```

`Authentication#isValidLogin` catches it, logs it and returns `false` without touching the failed attempt budget, so an overload cannot lock a user out. Every other caller gets the exception passed through to the `ExceptionHandler` — for registration or a password change a `503` is the honest outcome, rather than a silent `false` that looks like "wrong password".

```yaml
authentication:
  hashing:
    concurrency: 0   # 0 derives the slots from memory cost and half the heap, clamped to 2..8
    timeout: 5000
```

The effective value is logged on INFO at startup. Raise it if your application hashes on more paths than login, or set it high to switch the gate off. If your application built this gating itself, remove it in the same step: the two limits multiply into unnecessary waiting, and a hardcoded copy of the framework's memory constant goes silently wrong the moment that constant changes.

### Argon2 parameters are configurable, and the defaults went down

`authentication.hashing.memory`, `authentication.hashing.iterations` and `authentication.hashing.parallelism` expose the Argon2id parameters, with new defaults:

| | before | now |
| --- | --- | --- |
| `authentication.hashing.memory` | 80000 KiB | 32768 KiB |
| `authentication.hashing.iterations` | 6 | 3 |
| `authentication.hashing.parallelism` | 2 | 1 |

The old values cost about twelve times what OWASP recommends for Argon2id (19 MiB, two iterations) — roughly 230 ms and 91 MB of heap per verification, for around 3.6 bits of effective password strength. The new defaults still sit above the OWASP minimum at about 100 ms and 32 MB. `p=1` because BouncyCastle's `Argon2BytesGenerator` computes the lanes sequentially: a higher parallelism never shortened a hash, it only spread the same memory over more lanes. Values below 8192 KiB, two iterations or one lane are rejected with an `IllegalArgumentException` at startup.

Set the old values explicitly if you want to keep them:

```yaml
authentication:
  hashing:
    memory: 80000
    iterations: 6
    parallelism: 2
```

### Hashes are stored in PHC format

`CommonUtils.hashArgon2` now returns the standard PHC string instead of a bare Base64 encoding of the raw hash bytes:

```
$argon2id$v=19$m=32768,t=3,p=1$<salt-b64>$<hash-b64>
```

`matchArgon2` verifies with the parameters embedded in the stored hash, never with the current configuration — that is what makes the parameter change above safe. The salt argument keeps its meaning: it still has to be the salt the hash was created with, the embedded one is not used as a fallback.

**Existing hashes keep working.** A stored value that does not start with `$` is verified with the parameters the earlier version used (`m=80000,t=6,p=2`) and the salt you pass in. Nobody is locked out by the upgrade.

The new `CommonUtils.needsRehash(hash)` reports a legacy hash, and a PHC hash whose parameters differ from the current configuration, as outdated. Call it after a successful login to migrate transparently:

```java
if (authentication.isValidLogin(identifier, password, salt, hash)) {
    if (CommonUtils.needsRehash(hash)) {
        user.setPassword(CommonUtils.hashArgon2(password, salt));
        datastore.save(user);
    }
    authentication.login(identifier);
}
```

Two things to be aware of:

* **Only evaluate `needsRehash` after a successful verification.** Recomputing the hash needs the clear text, and only a successful login proves it is the right one.
* **The salt is part of the stored string now**, as the PHC format prescribes. If you passed a secret as the salt — `hashArgon2(cleartext)` uses `application.secret` — it stops being secret once the hash is stored. Use a per-user random salt instead.

Column width is worth a look before the upgrade: a PHC string is around 110 characters where the old value was 44.

## From 10.12.1 to 10.12.2

This is a drop-in replacement.

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