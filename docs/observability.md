# Observability

Production systems need visibility beyond log lines. mangoo I/O exposes **request metrics** (counts and latency) for the admin dashboard and optional **OpenTelemetry** export for traces. Both are off by default so local development stays lightweight; enable them explicitly when you deploy.

Metrics answer "how much traffic and how slow?" inside the app. OTLP sends spans to a collector (Jaeger, Grafana Tempo, vendor backends) for distributed tracing. See [Operating](operating.md) for JVM and vault setup alongside these features.

## Metrics

Set `metrics.enable` to `true` to count requests and record processing time. Values show up on the [admin dashboard](administration.md). Metrics are disabled by default.

```yaml
metrics:
  enable: true
```

Admin UI traffic is excluded from the counters.

## OpenTelemetry

mangoo I/O can export traces over OTLP/gRPC:

```yaml
otlp:
  enable: true
  endpoint: http://127.0.0.1:4317
```

When enabled, spans are sent with service name `io.mangoo` and the current application mode. Leave `otlp.enable` false unless you run a collector.

There is no public `/health` HTTP endpoint. Use `Datastore.isHealthy()` in your own checks if you need a MongoDB ping.
