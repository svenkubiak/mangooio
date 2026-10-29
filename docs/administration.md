# Administration

The **admin dashboard** is a built-in UI for operators. It shows cache statistics, registered scheduler jobs, security-related helpers, and request metrics when enabled. It is **disabled by default** because it exposes internal state, so turn it on only in environments where access is actually controlled.

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

`application.admin.password` stores the Argon2id hash, not the cleartext:

```java
String hash = CommonUtils.hashArgon2("the-password", CommonUtils.uuidV4());
```

Pass `hash` as `application.admin.password`, for example in `APPLICATION_ADMIN_PASSWORD`. The salt is part of the hash, so there is no separate salt setting. Use a random salt. Passing `application.secret` embeds that secret in the stored hash.

Open:

```
http://<host>:<port>/@admin
```

You sign in with a form at `/@admin/login`. A JWT cookie is issued for later requests. This is not HTTP Basic authentication.

## MFA

Set `application.admin.secret` to a TOTP secret and a second factor kicks in automatically. After username/password, `/@admin/twofactor` asks for a 6-digit code (SHA-512, 30-second period). Each code and each pre-authentication cookie is accepted once, and the failed attempt budget of both steps (10 attempts, then locked for 60 minutes) is shared by all instances when persistence is enabled. Generate secrets with `TotpUtils` as described in [Authentication](authentication.md).

## Metrics

Request counts and timings are off by default, so enable them explicitly:

```yaml
metrics:
  enable: true
```

Once enabled, they appear on the dashboard itself. They are collected per instance, and the dashboard footer names the instance that served the page. For load balancer probes use `/@health/live` and `/@health/ready`, see [Operating](operating.md#health-checks).

See [Observability](observability.md) for OpenTelemetry.
