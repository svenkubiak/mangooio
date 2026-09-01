# Authentication

mangoo I/O provides **cookie-based authentication**, not a full user-management system. You remain responsible for storing users, validating passwords, and deciding who may log in. After a successful check, you call `authentication.login(subject)` and the framework sets a signed, encrypted JWT cookie on the response.

That design fits applications that already have a user table (or an external identity source) and want session-like behavior without server-side session storage for auth state. The cookie works in a share-nothing setup: any node can validate it with the configured keys.

Optional pieces build on the same model:

- **Remember me** — Longer cookie lifetime via `rememberMe()`.
- **Two-factor** — Flag the cookie until TOTP is verified; use `TotpUtils` and `isValidSecondFactor`.
- **Route protection** — `withAuthentication()` on routes or controllers.
- **API keys** — `ApiKeyFilter` for machine clients using the `Authorization` header.

Inject `Authentication` into a controller method:

```java
public Response login(Authentication authentication) {
    return Response.ok().render();
}
```

## Password hashing

Hash passwords with Argon2id via `CommonUtils`:

```java
String hash = CommonUtils.hashArgon2("password", "salt");
```

Store the hash (and the salt) with your user record.

## Login

```java
if (authentication.isValidLogin("subject", "password", "salt", "hash")) {
    authentication.login("subject");
}
```

`isValidLogin` also applies lockout: after `authentication.lock` failed attempts (default 10) the identifier is locked in the auth cache.

Then:

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

## Protecting routes

```java
Bind.controller(AccountController.class).withRoutes(
    On.get().to("/account").respondeWith("index").withAuthentication()
);
```

You can also call `withAuthentication()` on the controller binder so every route requires a cookie. Missing authentication redirects to `authentication.redirect.login`, or returns HTTP 403 if that key is unset. When two-factor is enabled on the cookie, the user is sent to `authentication.redirect.mfa` (or the login redirect). Set `authentication.origin` to append `?origin=<request-uri>` on those redirects.

Cookie names, SameSite, Secure, signing keys, and lifetimes are described in [Configuration](configuration.md). Prefer `vault{}` for `authentication.cookie.key` and `authentication.cookie.secret`. See [Secrets](secrets.md).

## Two-factor authentication

Generate a secret and verify TOTP codes with `TotpUtils` (SHA-512, 6 digits, 30-second period):

```java
String secret = TotpUtils.createSecret();
String qr = TotpUtils.getQRCode("user@example.com", "My App", secret);
String url = TotpUtils.getOtpAuthURL("user@example.com", "My App", secret);

if (authentication.isValidSecondFactor(secret, totpFromUser)) {
    authentication.twoFactorAuthentication(false);
    authentication.login(subject);
}
```

## API keys

For machine clients, use `ApiKeyFilter` instead of the cookie:

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

The filter accepts `Authorization: Bearer super-secret`.

## Blacklist

Set `authentication.blacklist` to `true` to enable a dedicated cache used by `CommonUtils.blacklist(id)` / `CommonUtils.isBlacklisted(id)`.
