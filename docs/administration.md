# Administration

The built-in dashboard shows cache stats, scheduled tasks, security helpers, and (optionally) request metrics. It is disabled by default.

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
