# Cross-Origin Resource Sharing (CORS)

Enable CORS in `config.yaml`. Headers are added only when the request URL matches `cors.urlpattern`.

```yaml
cors:
  enable: true
  alloworigin: ^https://app\.example\.com$
  urlpattern: ^http(s)?://([^/]+)(:([^/]+))?(/([^/])+)?/api(/.*)?$
  headers:
    allowcredentials: true
    allowheaders: Content-Range,ETag
    allowmethods: GET,POST,PATCH
    exposeheaders: Authorization,Content-Type
    maxage: 86400
```

| Key | Role |
|---|---|
| `alloworigin` | Regex tested against the request `Origin` |
| `urlpattern` | Regex of request URLs that receive CORS headers |
| `headers.allowcredentials` | `Access-Control-Allow-Credentials` |
| `headers.allowheaders` | `Access-Control-Allow-Headers` |
| `headers.allowmethods` | `Access-Control-Allow-Methods` |
| `headers.exposeheaders` | `Access-Control-Expose-Headers` |
| `headers.maxage` | `Access-Control-Max-Age` in seconds |

Defaults are listed in [Configuration](configuration.md). See the [MDN CORS guide](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS).
