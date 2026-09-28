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

Because Argon2id is memory-hard, every call holds `authentication.hashing.memory` KiB of heap (78 MB by default) for as long as it runs. The framework limits how many of these computations may run at the same time, so that a burst of concurrent logins cannot exhaust the heap. A call that finds every slot taken waits up to `authentication.hashing.timeout` milliseconds and is then rejected with a `MangooHashingException`. See [Argon2 hashing](configuration.md#argon2-hashing) for the configuration and for what happens on the individual call sites.

!!! warning
    `authentication.hashing.memory`, `authentication.hashing.iterations` and `authentication.hashing.parallelism` make the Argon2 parameters configurable. Changing any of them invalidates every hash that has already been stored, and the affected users can no longer log in. Only change them together with a mechanism that rehashes a password on the next successful login.

## Login

```java
if (authentication.isValidLogin("subject", "password", "salt", "hash")) {
    authentication.login("subject");
}
```

`isValidLogin` also applies lockout: after `authentication.lock` failed attempts (default 10), the identifier is locked for `authentication.lock.duration` minutes (default 60). This happens automatically so that a brute-force attempt against one account gets throttled without you having to implement rate limiting by hand. A successful login clears the budget.

If no hashing slot becomes available within `authentication.hashing.timeout`, `isValidLogin` fails closed and returns `false`. The failed attempt budget stays untouched in that case, an overload situation must not lock a user out.

The lockout is an **absolute** point in time, set once when the budget is used up. Further failed attempts during the lockout are rejected without extending it, so an attacker cannot keep the rightful owner of an account locked out indefinitely by simply continuing to guess.

The same budget applies to the second factor, see [Two-factor authentication](#two-factor-authentication). Both steps are counted separately per identifier, so a failed password attempt never consumes the budget of the second factor and vice versa.

The counter lives in the auth cache and is therefore per process. An application running several instances behind a load balancer gets one budget per instance; if you need a shared or restart-safe budget, keep it in your own data store instead.

Then, once logged in, you can adjust the cookie further:

```java
authentication.rememberMe();                 // longer cookie lifetime
authentication.twoFactorAuthentication(true); // require TOTP next
```

## Methods

```java
authentication.getSubject();   // logged-in subject, or null
authentication.isValid();      // authentication is complete, use this for authorization
authentication.hasSubject();   // a subject is present, second factor may still be pending
authentication.logout();       // expire the cookie
authentication.invalidate();   // drop the cookie immediately
authentication.update();       // refresh the cookie on this response
authentication.rememberMe(true);
authentication.twoFactorAuthentication(true);
authentication.userHasLock("subject");              // password step
authentication.userHasSecondFactorLock("subject");  // second factor step
authentication.isValidSecondFactor("subject", secret, totp);
```

`isValid()` and `hasSubject()` are not interchangeable. `hasSubject()` is the raw check whether a subject is set, which is already the case right after the password step, because that is when the authentication cookie is issued. `isValid()` additionally requires that no second factor is outstanding, and it is the one to base an authorization decision on. Use `hasSubject()` only where the incomplete state is exactly the state you are working on, which in practice is the page that asks for the TOTP.

This matters because an `Authentication` object is bound on every request, not only on routes bound with `withAuthentication()`. A filter or controller method on an unprotected route that derives access from `getSubject()` alone lets a visitor in who knows the password but not the second factor.

`userHasLock()` and `userHasSecondFactorLock()` are queries, not a step you have to perform. `isValidLogin()` and `isValidSecondFactor()` check the lock before doing any work and count the failed attempt afterwards, both on their own. Query the lock only where you want to show "this account is locked" instead of the same "login failed" that a wrong password produces.

`logout()` and `invalidate()` sound similar but differ in timing: `logout()` marks the cookie to expire through the normal response cycle, while `invalidate()` drops it immediately, which matters if you need the effect to be visible before the method returns, for example before redirecting.

## Protecting routes

```java
Bind.controller(AccountController.class).withRoutes(
    On.get().to("/account").respondeWith("index").withAuthentication()
);
```

You can also call `withAuthentication()` on the controller binder so every route on it requires a cookie, see [Routing](routing.md). Missing authentication redirects to `authentication.redirect.login`, or returns HTTP 403 if that key is unset, so an API-only application without a login page still fails safely instead of redirecting somewhere that does not exist. When two-factor is enabled on the cookie, the user is sent to `authentication.redirect.mfa` instead (or the login redirect if `mfa` is not configured). Set `authentication.origin` to append `?origin=<request-uri>` on those redirects, which is what lets a login page send the user back to whatever page they originally asked for. The value contains the request URI including its query string, is URL encoded, and always begins with exactly one slash, so it can neither inject extra parameters into the redirect nor arrive as a protocol-relative URL such as `//evil.com` pointing at a foreign host.

That last point still leaves the final decision to you: `origin` is a value a visitor chose, and it is only guaranteed to be a path on your own host, not a path that visitor is allowed to reach. Before redirecting to it after a successful login, check it against the routes your application actually serves rather than passing it straight to a redirect.

Route protection covers controller routes only. SSE and WebSocket routes are registered straight on Undertow's path handler and never reach the authentication check, so `withAuthentication()` does not exist on them and no cookie is verified when a client connects. See [Routing](routing.md) for what that means in practice.

Cookie names, SameSite, Secure, signing keys, and lifetimes are described in [Configuration](configuration.md). Prefer `vault{}` for `authentication.cookie.key` and `authentication.cookie.secret` rather than writing them out in `config.yaml`. See [Secrets](secrets.md).

## Two-factor authentication

Generate a secret and verify TOTP codes with `TotpUtils` (SHA-512, 6 digits, 30-second period, the same parameters most authenticator apps expect out of the box):

```java
String secret = TotpUtils.createSecret();
String qr = TotpUtils.getQRCode("user@example.com", "My App", secret);
String url = TotpUtils.getOtpAuthURL("user@example.com", "My App", secret);

if (authentication.isValidSecondFactor(subject, secret, totpFromUser)) {
    authentication.twoFactorAuthentication(false);
    authentication.update();
}
```

A typical flow: after the password check succeeds, call `login(subject)` with `twoFactorAuthentication(true)` set, redirect to your TOTP entry page, and only clear the flag once `isValidSecondFactor` confirms the code the user typed in actually matches their secret. Call `update()` afterwards so the cleared flag reaches the cookie on that same response. See [Complete login flow](#complete-login-flow) for both steps written out.

While the flag is set, `isValid()` returns false, so the TOTP entry page itself has to use `hasSubject()` to find the subject the code is being checked for.

Pass the identifier as the first argument. A TOTP has six digits and is verified without a tolerance window, so exactly one of a million codes is valid per 30-second window — that is only a second factor as long as something limits how often it may be guessed. With the identifier, the same `authentication.lock` budget as for the password step applies, counted under its own key, and a successful check clears it.

The two-argument `isValidSecondFactor(secret, totp)` is **deprecated**: it verifies the code unthrottled, which makes guessing it feasible. An upstream reverse proxy only counts requests per source address and does not bound the total number of guesses; a budget per identity does.

## Complete login flow

The flows below show every call to `Authentication` a controller needs — and nothing else. In particular there is no lock handling in them: `isValidLogin` and `isValidSecondFactor` check the lock before they do any work and count the failed attempt afterwards, both on their own. Querying the lock is a separate, optional step, see [Telling a lock apart](#telling-a-lock-apart).

### Without two-factor

```java
public Response doLogin(Form form, Authentication authentication, Flash flash) {
    String username = form.get("username");
    User user = userService.findByUsername(username);

    if (user == null || !authentication.isValidLogin(username, form.get("password"), user.getSalt(), user.getPassword())) {
        flash.putError("message", "Login failed");
        return Response.redirect("/login");
    }

    authentication.login(username);
    if (form.getBoolean("remember").orElse(false)) {
        authentication.rememberMe();
    }

    return Response.redirect("/dashboard");
}
```

No `update()` here: no authentication cookie exists yet, so the response writes a fresh one on its own.

### With two-factor

The password step is the same up to the last two calls:

```java
    authentication.login(username);
    authentication.twoFactorAuthentication(true);

    return Response.redirect("/twofactor");
```

From here on `isValid()` is false and `hasSubject()` is true. The cookie is written regardless and carries the flag, which is what keeps the second step stateless.

The page that asks for the code must **not** be bound with `withAuthentication()`. That route protection redirects to `authentication.redirect.mfa` exactly while the flag is set, so the TOTP page would redirect to itself:

```java
public Response twofactor(Authentication authentication) {
    if (!authentication.hasSubject()) {
        return Response.redirect("/login");
    }

    return Response.ok().render();
}
```

```java
public Response doTwofactor(Form form, Authentication authentication, Flash flash) {
    if (!authentication.hasSubject()) {
        return Response.redirect("/login");
    }

    String subject = authentication.getSubject();
    User user = userService.findByUsername(subject);

    if (!authentication.isValidSecondFactor(subject, user.getTotpSecret(), form.get("totp"))) {
        flash.putError("message", "Invalid code");
        return Response.redirect("/twofactor");
    }

    authentication.twoFactorAuthentication(false);
    authentication.update();

    return Response.redirect("/dashboard");
}
```

`update()` is mandatory in this step. The authentication cookie already exists at this point and is only rewritten when it is missing or when `update()` asked for it — without it the cookie keeps `twoFactor=true` and the next request sends the user back to `/twofactor`.

### Logout

```java
public Response doLogout(Authentication authentication) {
    authentication.logout();
    return Response.redirect("/");
}
```

### Telling a lock apart

Nothing above has to change for the lockout to work. A locked identifier makes `isValidLogin` return `false` without checking the password, which is the same `false` a wrong password produces — so the user is told "login failed" while the account is in fact locked.

Add a lock query only if you want a different message for that case:

```java
    if (authentication.userHasLock(username)) {
        flash.putError("message", "Account is temporarily locked, try again later");
        return Response.redirect("/login");
    }
```

`userHasSecondFactorLock(subject)` does the same for the TOTP step. Both are read-only: they neither consume an attempt nor set a lock, so an extra query costs nothing but also protects nothing on its own.

Be aware that this is a trade-off, not a free improvement. A message that distinguishes "locked" from "wrong password" confirms to whoever triggered the lock that the identifier exists. On a login form that already reveals existing accounts elsewhere, for example on registration or password reset, that changes nothing; on one that deliberately does not, keep the single generic message.

### What the controller does not see

A hashing overload is reported to `isValidLogin` as `false`, indistinguishable from a wrong password. That is deliberate, see [Argon2 hashing](configuration.md#argon2-hashing); the failed attempt budget stays untouched, so the situation cannot lock anyone out. If you need to tell the two apart, call `CommonUtils.matchArgon2` yourself and catch `MangooHashingException` — at the price of losing the built-in lock handling.

Your own hashing outside the login path, on registration or a password change, needs no `try`/`catch`. The unchecked `MangooHashingException` travels to the `ExceptionHandler` and becomes a `503`. Catching it and returning `false` would turn an overload into a silent "wrong password".

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
