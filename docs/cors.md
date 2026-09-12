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

## Escape the dots in `alloworigin` ⚠️

An origin that matches `alloworigin` gets reflected back verbatim in `Access-Control-Allow-Origin`, and with `allowcredentials: true` it may then read authenticated responses. Your regex is therefore the whole access control decision, and the most common way to get it wrong is an unescaped dot. In a regex, `.` matches *any* character, not a literal period:

```yaml
# Wrong: the dot after "app" matches any character
alloworigin: ^https://app.example\.com$

# Right: every literal dot is escaped
alloworigin: ^https://app\.example\.com$
```

The first pattern also accepts `https://appxexample.com` — a domain an attacker can simply register. Escape every literal dot.

Two things work in your favour here. The pattern is applied with `Matcher.matches()`, so it must match the *entire* `Origin` value; a pattern cannot accidentally match just a prefix or suffix. And browsers send only the serialized origin (scheme, host, optional port), never a path, so there is no path component to smuggle a match through.

What remains is deciding which hosts to trust. Prefer listing them explicitly, as in `^https://(app|admin)\.example\.com$`, over a blanket subdomain wildcard like `^https://[a-z0-9-]+\.example\.com$`. A wildcard makes every current and future subdomain part of your trust boundary, so a forgotten marketing subdomain with an XSS flaw, or a dangling DNS record someone else can claim, becomes a way to read credentialed responses from your API.

Every response to a URL matching `urlpattern` also carries **`Vary: Origin`**, including responses whose origin did *not* match `alloworigin` and therefore received no CORS headers at all. This tells shared caches and CDNs that the response depends on the request's `Origin`. Without it, a cache could store the header-less response produced for a rejected origin and later hand it to an allowed origin, which would then be blocked by the browser until the entry expires. If a controller sets its own `Vary` header, mangoo I/O leaves it untouched.

`headers.maxage` is worth a second look: it tells the browser how long it may cache the result of a preflight `OPTIONS` request before asking again. A higher value means fewer preflight round trips (better for latency), but also means a change to your CORS policy takes longer to reach clients that already cached the old answer.

Defaults for all of these are listed in [Configuration](configuration.md). See the [MDN CORS guide](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS) if you want the full background on preflight requests and credentialed requests.
