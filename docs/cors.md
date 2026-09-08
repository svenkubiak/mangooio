# Cross-Origin Resource Sharing (CORS) 🌐

Browsers block JavaScript on one origin from reading responses from another unless the server explicitly opts in with CORS headers. That is a browser-enforced restriction, not something the server can see or refuse on its own, so the only way to make cross-origin calls work is to tell the browser, through headers, exactly which origins and methods are allowed. Enable CORS when your **API** is consumed by a single-page app or mobile web app hosted on a different domain than the mangoo I/O backend.

mangoo I/O adds these headers only when the request URL matches **`cors.urlpattern`** and the origin matches **`cors.alloworigin`** (both regular expressions). That keeps accidental wide-open CORS off by default: nothing gets a CORS header at all until you configure both patterns explicitly, so enabling CORS for your `/api/` paths cannot accidentally also expose an internal admin route you forgot was on the same domain.

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

`headers.maxage` is worth a second look: it tells the browser how long it may cache the result of a preflight `OPTIONS` request before asking again. A higher value means fewer preflight round trips (better for latency), but also means a change to your CORS policy takes longer to reach clients that already cached the old answer.

Defaults for all of these are listed in [Configuration](configuration.md). See the [MDN CORS guide](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) if you want the full background on preflight requests and credentialed requests.
