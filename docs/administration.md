# Administration

The **admin dashboard** is a built-in UI for operators. It shows cache statistics, registered scheduler jobs, security-related helpers, and request metrics when enabled. It is **disabled by default** because it exposes internal state, so turn it on only in environments where access is actually controlled.

Access uses a **login form** and JWT cookie rather than HTTP Basic. An optional **TOTP** second factor is configured with `application.admin.secret`. In production, protect admin routes with network policy as well as strong credentials stored in the [vault](secrets.md), since the login form alone is not a substitute for keeping the dashboard off the public internet.

Enable and configure:

```yaml
application:
  admin:
    enable: true
    username: admin
    password: vault{}
    locale: en_EN
```

Open:

```
http://<host>:<port>/@admin
```

You sign in with a form at `/@admin/login`. A JWT cookie is issued for later requests. This is not HTTP Basic authentication.

## MFA

Set `application.admin.secret` to a TOTP secret and a second factor kicks in automatically. After username/password, `/@admin/twofactor` asks for a 6-digit code (SHA-512, 30-second period). Generate secrets with `TotpUtils` as described in [Authentication](authentication.md).

## Metrics

Request counts and timings are off by default, so enable them explicitly:

```yaml
metrics:
  enable: true
```

Once enabled, they appear on the dashboard itself. Note that there is no separate `/@admin/health` endpoint to scrape.

See [Observability](observability.md) for OpenTelemetry.
