# Administration

The **admin dashboard** is a built-in UI for operators: cache statistics, registered scheduler jobs, security-related helpers, and request metrics when enabled. It is **disabled by default** because it exposes internal state—turn it on only in environments where access is controlled.

Access uses a **login form** and JWT cookie (not HTTP Basic). Optional **TOTP** second factor is configured with `application.admin.secret`. Protect admin routes in production with network policy as well as strong credentials stored in the [vault](secrets.md).

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

Set `application.admin.secret` to a TOTP secret. After username/password, `/@admin/twofactor` asks for a 6-digit code (SHA-512, 30-second period). Generate secrets with `TotpUtils` as described in [Authentication](authentication.md).

## Metrics

Request counts and timings are off by default:

```yaml
metrics:
  enable: true
```

When enabled, they appear on the dashboard. There is no `/@admin/health` endpoint.

See [Observability](observability.md) for OpenTelemetry.
