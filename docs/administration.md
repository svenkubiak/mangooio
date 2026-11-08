# Administration

The **admin dashboard** is a built-in UI for operators. It shows uptime, the Java version, heap, open connections, active features, event queues, routes, cache statistics, scheduler jobs, a few security helpers, and request metrics when those are enabled. Clearing a cache and resetting metrics go through the `MessageBroker` when persistence is on, so they run on every instance. Without persistence they run on the instance that served the page. The instance list is every instance's last heartbeat. The blocks under that list are the instance that served the page. The dashboard is **disabled by default**. It exposes internal state, so turn it on only where the network is already closed off.

Access uses a **login form** and JWT cookie rather than HTTP Basic. An optional **TOTP** second factor is configured with `application.admin.secret`. In production, protect admin routes with network policy as well as strong credentials kept outside the artifact (see [Secrets](secrets.md)), since the login form alone is not a substitute for keeping the dashboard off the public internet.

Enable and configure:

```yaml
application:
  admin:
    enable: true
    username: admin
    password: env{}
    locale: en_EN
```

`application.admin.password` stores the Argon2id hash, not the cleartext. From the application project:

```shell
mvn -q mangooio:hash
```

The goal reads `src/main/resources/config.yaml` and hashes with `authentication.hashing.memory`, `authentication.hashing.iterations` and `authentication.hashing.parallelism`. It merges `default` with the `dev` environment. `-Dapplication.mode=prod` (or `test`) uses that environment instead; any other value uses `prod`. `-Dapplication.config=/path/to/config.yaml` reads another file. The password is prompted twice and is not echoed, so run the goal from a terminal. With `-q`, the only line on standard output is the PHC string. The application does not have to be running, and no login is required.

Pass that string as `application.admin.password`, for example in `APPLICATION_ADMIN_PASSWORD`. The salt is part of the hash, and the goal picks a random one with `CommonUtils.uuidV4`. There is no separate salt setting. The goal calls `CommonUtils.hashArgon2` with the three parameters from the config file. The two-argument form still needs a started application, because it uses that application's hasher. Passing `application.secret` as the salt embeds that secret in the stored hash.

Open:

```
http://<host>:<port>/@admin
```

You sign in with a form at `/@admin/login`. A JWT cookie is issued for later requests. This is not HTTP Basic authentication.

The cookie name is `mangooio-admin`. In prod it is `__Host-mangooio-admin`, which browsers accept only on HTTPS, with `Secure` and `Path=/`. It is `HttpOnly` and `SameSite=Strict`. `Secure` is set only in prod mode, so a prod deployment on plain HTTP will not keep the operator signed in. The full session lasts 1800 seconds. After the password and before TOTP, the TTL is 120 seconds and the cookie is single-use (`OneTimeStore` key `admin-pre-auth:` plus the JWT id). Signing and encryption use `application.secret` for both the HMAC key and the AES secret. The issuer is `application.name`. The audience is the cookie name.

Routes registered when `application.admin.enable` is true: `GET /@admin`, `/@admin/cache`, `/@admin/login`, `/@admin/twofactor`, `/@admin/scheduler`, `/@admin/routes`, `/@admin/security`, `/@admin/logout`, and `POST /@admin/authenticate`, `/@admin/verify`, `/@admin/cache/clear`, `/@admin/metrics/reset`. POST mutations use `CsrfFilter`. `/@admin/assets/` is a public classpath resource handler. `/@admin/stream` sits outside `AdminFilter` and returns 401 with no body when the cookie is missing, invalid, or still the pre-auth cookie. The page script then shows the login form. A hidden tab closes the `EventSource`.

Clearing the application cache keeps the `MANGOOIO-WARNINGS` entry. There is no unlock route for a locked admin login. The documents are `_id` `admin:pwd` and `admin:2fa` in `mangooio_attempts`. A password that is not a PHC hash is rejected and still consumes an attempt. `MangooHashingException` releases the attempt.

## MFA

Set `application.admin.secret` to a TOTP secret and a second factor kicks in. After the password, `/@admin/twofactor` asks for a 6-digit code (SHA-512, 30-second period, no adjacent step). Each code and each pre-authentication cookie is accepted once. The lock is not `authentication.lock`. It is fixed at 10 attempts and 60 minutes, on the keys `admin:pwd` and `admin:2fa`, shared across instances when persistence is enabled. The "Login lock" row on the dashboard is the user setting, not this one. A bad password and a lock both send you back to the login form.

`GET /@admin/security` draws a new TOTP secret on every load while `application.admin.secret` is unset. That secret is not saved. The QR code is useless until you put the value in config and restart. Generate a secret you intend to keep with `TotpUtils`, as in [Authentication](authentication.md).

## Several instances

The dashboard starts with the list of instances: id, version, last heartbeat, heap, open connections, and the requests, errors, average time, and failed events of the last five minutes. Every instance reports every ten seconds while the dashboard is enabled, including once at start. With persistence the reports are `mangooio_instances`. Without persistence only this process is known. An instance that stopped removes itself. One that crashed is marked after 30 seconds without a heartbeat and disappears after a minute. `getInstances` drops a document whose `expireAt` is in the past, so the MongoDB TTL monitor is not required for that. If the registry read fails, the page still shows the instance that served it. A rolling deploy shows up as instances with different versions.

Requests, errors, and average time come from `Metrics` and stay at zero unless `metrics.enable` is true. Errors are status codes of 500 and above. Times are milliseconds. At most 10 failed routes are listed. Past 1000 distinct route templates, the rest are bucketed as `Other routes`.

Clearing a cache and resetting the metrics are sent through the `MessageBroker` and run on every instance. Everything below the list of instances, as well as the pages for routes, scheduler and cache, shows the instance that served the page; its id is marked in the list and shown in the footer.

## Live updates

While the dashboard is open, it connects to `/@admin/stream` and replaces the instance list, this instance, and the event queues every five seconds. Requests, status codes, routes, and failures are included only when `metrics.enable` is true. A badge next to the title shows the open stream. The stream requires a valid admin cookie and closes when the cookie expires. The page then shows the login form. A background tab closes the stream and opens it again when the tab is visible. The connection stays on one instance. After that instance restarts, the page connects to whichever instance answers next.

## Metrics

Request counts and timings are off by default, so enable them explicitly:

```yaml
metrics:
  enable: true
```

Once enabled, they appear on the dashboard itself: requests, error rate and processing times of the last five minutes with the values since the start below, the responses grouped by status code since the start, the routes with the most client and server errors, and the last 50 server errors with time, route and status. Both show the route template, e.g. `GET /user/{id}`, never the requested path of a matched route, and no exceptions. A request without a route is shown with its path and without the query string. The exceptions are in the log. They are collected per instance, and the dashboard footer names the instance that served the page. For load balancer probes use `/@health/live` and `/@health/ready`, see [Operating](operating.md#health-checks).

See [Observability](observability.md) for OpenTelemetry.
