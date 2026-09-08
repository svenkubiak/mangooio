# Authentication 🔑

mangoo I/O provides **cookie-based authentication**, not a full user-management system. You remain responsible for storing users, validating passwords, and deciding who may log in; the framework's job starts once you have already decided "yes, this person may in." After a successful check, you call `authentication.login(subject)` and the framework sets a signed, encrypted JWT cookie on the response.

That design fits applications that already have a user table (or an external identity source) and want session-like behavior without server-side session storage for auth state. Because the cookie itself carries everything needed to verify it, it works in a share-nothing setup: any node behind a load balancer can validate it using the same configured keys, with no shared session store, no sticky sessions, and no "which server did this user log in on" problem to solve.

Optional pieces build on the same model:

- **Remember me.** Longer cookie lifetime via `rememberMe()`, for the "keep me logged in" checkbox users expect.
- **Two-factor.** Flag the cookie until TOTP is verified, using `TotpUtils` and `isValidSecondFactor`, so a stolen password alone is not enough to authenticate.
- **Route protection.** `withAuthentication()` on routes or controllers, so protecting a page is a one-line addition in `Bootstrap.java` rather than a check repeated in every method.
- **API keys.** `ApiKeyFilter` for machine clients using the `Authorization` header, since a script calling your API has no browser to hold a cookie for it.

Inject `Authentication` into a controller method:

```java
public Response login(Authentication authentication) {
    return Response.ok().render();
}
```

## Password hashing

Hash passwords with Argon2id via `CommonUtils`. Argon2id was chosen because it is memory-hard: unlike older algorithms such as bcrypt or SHA-based hashes, it is deliberately expensive to parallelize on GPUs, which is exactly the kind of attack password hashes need to resist once a database leaks.

```java
String hash = CommonUtils.hashArgon2("password", "salt");
```

Store the hash (and the salt) with your user record; never store the plain password anywhere, not even temporarily in a log line.

## Login

```java
if (authentication.isValidLogin("subject", "password", "salt", "hash")) {
    authentication.login("subject");
}
```

`isValidLogin` also applies lockout: after `authentication.lock` failed attempts (default 10), the identifier is locked in the auth cache. This happens automatically so that a brute-force attempt against one account gets throttled without you having to implement rate limiting by hand.

Then, once logged in, you can adjust the cookie further:

```java
authentication.rememberMe();                 // longer cookie lifetime
authentication.twoFactorAuthentication(true); // require TOTP next
```

## Methods

```java
authentication.getSubject();   // logged-in subject, or null
authentication.isValid();      // subject is present
authentication.logout();       // expire the cookie
authentication.invalidate();   // drop the cookie immediately
authentication.update();       // refresh the cookie on this response
authentication.rememberMe(true);
authentication.twoFactorAuthentication(true);
authentication.userHasLock("subject");
authentication.isValidSecondFactor(secret, totp);
```

`logout()` and `invalidate()` sound similar but differ in timing: `logout()` marks the cookie to expire through the normal response cycle, while `invalidate()` drops it immediately, which matters if you need the effect to be visible before the method returns, for example before redirecting.

## Protecting routes

```java
Bind.controller(AccountController.class).withRoutes(
    On.get().to("/account").respondeWith("index").withAuthentication()
);
```

You can also call `withAuthentication()` on the controller binder so every route on it requires a cookie, see [Routing](routing.md). Missing authentication redirects to `authentication.redirect.login`, or returns HTTP 403 if that key is unset, so an API-only application without a login page still fails safely instead of redirecting somewhere that does not exist. When two-factor is enabled on the cookie, the user is sent to `authentication.redirect.mfa` instead (or the login redirect if `mfa` is not configured). Set `authentication.origin` to append `?origin=<request-uri>` on those redirects, which is what lets a login page send the user back to whatever page they originally asked for.

Cookie names, SameSite, Secure, signing keys, and lifetimes are described in [Configuration](configuration.md). Prefer `vault{}` for `authentication.cookie.key` and `authentication.cookie.secret` rather than writing them out in `config.yaml`. See [Secrets](secrets.md).

## Two-factor authentication

Generate a secret and verify TOTP codes with `TotpUtils` (SHA-512, 6 digits, 30-second period, the same parameters most authenticator apps expect out of the box):

```java
String secret = TotpUtils.createSecret();
String qr = TotpUtils.getQRCode("user@example.com", "My App", secret);
String url = TotpUtils.getOtpAuthURL("user@example.com", "My App", secret);

if (authentication.isValidSecondFactor(secret, totpFromUser)) {
    authentication.twoFactorAuthentication(false);
    authentication.login(subject);
}
```

A typical flow: after the password check succeeds, call `login(subject)` with `twoFactorAuthentication(true)` set, redirect to your TOTP entry page, and only clear the flag once `isValidSecondFactor` confirms the code the user typed in actually matches their secret.

## API keys

For machine clients that have no browser and therefore nowhere to keep a cookie, use `ApiKeyFilter` instead:

```yaml
application:
  api:
    key: super-secret
```

```java
@FilterWith(ApiKeyFilter.class)
public Response export() {
    return Response.ok().bodyJson(data);
}
```

The filter accepts `Authorization: Bearer super-secret`. This is a single shared key rather than per-client credentials, so treat it the way you would any other shared secret: rotate it if it leaks, and avoid handing the same key to more clients than you can comfortably revoke access from later.

## Blacklist

Set `authentication.blacklist` to `true` to enable a dedicated cache used by `CommonUtils.blacklist(id)` / `CommonUtils.isBlacklisted(id)`. This is useful for revoking a specific token or subject before its natural expiry, for example immediately after a user changes their password and every previously issued cookie for that account should stop working right away.
