# Cross-Origin Resource Sharing (CORS)

Mangoo I/O provides built-in support for [Cross-Origin Resource Sharing (CORS)](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing) on the backend. Before using CORS, you must enable it in the `config.yaml` file:

```yaml
cors:
  enable: true
```

## Configuring CORS

Once enabled, you can define specific CORS headers in the `config.yaml` file to fine-tune access control. Below is an example configuration:

```yaml
cors:
  enable: true
  alloworigin: ^localhost$|^127(?:\.[0-9]+){0,2}\.[0-9]+$|^(?:0*\:)*?:?0*1$
  urlpattern: ^http(s)?://([^/]+)(:([^/]+))?(/([^/])+)?/api(/.*)?$
  headers:
    allowcredentials: true
    allowheaders: Content-Range,ETag
    allowmethods: GET,POST,PATCH
    exposeheaders: Authorization,Content-Type
    maxage: 86400
```

### Explanation of Configuration:

- **`alloworigin`**: Defines allowed origins using a regex pattern.
- **`urlpattern`**: Specifies which URLs should be matched for CORS rules.
- **`headers`**:
    - `allowcredentials`: Enables credentials such as cookies or authorization headers.
    - `allowheaders`: Lists HTTP headers allowed in requests.
    - `allowmethods`: Defines HTTP methods permitted for cross-origin requests.
    - `exposeheaders`: Specifies response headers that can be exposed to the client.
    - `maxage`: Sets the maximum age (in seconds) for caching preflight requests.

For more details, refer to the [CORS specification](https://developer.mozilla.org/en-US/docs/Web/HTTP/CORS).
