# Secrets 🔐

Production applications need secrets that must never end up in the artifact or in git: cookie secrets and keys, database passwords, SMTP credentials. mangoo I/O reads them from outside the JAR with three placeholders you write directly into `config.yaml`:

| Placeholder | Source |
|---|---|
| `env{}` | Operating-system environment variable |
| `arg{}` | JVM system property (`-D...`) |
| `file{}` | File in the secrets directory, as Kubernetes Secrets and Docker secrets provide them |

Every placeholder accepts a fallback in the braces, `env{fallback}`, which is used only if the source has no value. A placeholder without a value and without a fallback fails the startup and names the key, so a missing secret is noticed at deploy time and not on the first request. Use fallbacks only for values that are not secret; a secret written as fallback sits in `config.yaml` again.

For a value that may be missing, write `env{?}`, `arg{?}` or `file{?}`. If the source is not set or empty, the key stays unset, as if it were not in `config.yaml`, and the default of the framework applies. An empty source never replaces a default with an empty string, so an empty `SMTP_PROTOCOL` keeps `smtps` and an empty `PERSISTENCE_MONGO_URL` falls back to `host` and `port`:

```yaml
smtp:
  host: env{?}
  username: env{?}
  password: env{?}
persistence:
  mongo:
    url: env{?}
```

To find out whether an optional value was given, read it without a default, for example `config.getString(Key.SMTP_HOST)`, which returns `null` while `getSmtpHost()` returns the default `localhost`.

Mounted secret files are the safest of the three sources: environment variables are visible in `/proc/<pid>/environ`, `docker inspect` and the pod spec and are inherited by child processes, a file on a tmpfs mount is not.

## Secrets in prod

In prod mode the framework secrets must come from one of the placeholders. A literal value in the `config.yaml` on the classpath fails the startup:

- `application.secret`, `application.admin.secret`, `application.api.key`
- `session.cookie.secret` / `.key`, `authentication.cookie.secret` / `.key`, `flash.cookie.secret` / `.key`
- `persistence.mongo.password` and `persistence.<prefix>.mongo.password`, `smtp.password`
- `connector.https.password`

A fallback in the braces counts as literal, an empty value does not, as it holds no secret. A `config.yaml` passed with `-Dapplication.config` lives outside the JAR and may contain literal values. `application.admin.password` is an Argon2id hash and may stay literal. In dev and test literal values are allowed, a project created with the archetype ships generated secrets for both.

The cookie secrets and keys fall back to `application.secret`, so the minimal prod setup is a single `APPLICATION_SECRET`; the startup warns as long as a cookie uses it, as dedicated values per cookie are recommended.

## Secret and key length

The cookie secrets (`*.cookie.secret`) and `application.secret` are used directly as encryption key for `dir` with `A256CBC_HS512`. That algorithm accepts a key of exactly 512 bit, so these values must be **exactly 64 bytes** long — not more, not less. Bytes count, not characters: a non-ASCII character such as `ä` takes two bytes in UTF-8. The cookie keys (`*.cookie.key`) are used for HS512 signing and must be **at least 64 bytes**. The application refuses to start if a value does not meet these rules.

Generate a value with 64 ASCII characters, for example:

```shell
LC_ALL=C tr -dc 'A-Za-z0-9' < /dev/urandom | head -c 64
```

## Environment variables

Use `env{}` to read a value from the process environment. The environment name is the config key in uppercase with dots replaced by underscores:

```yaml
session:
  cookie:
    secret: env{}
```

This reads `SESSION_COOKIE_SECRET`.

## JVM arguments

Use `arg{}` to read a JVM system property with the same dotted key:

```yaml
application:
  db:
    username: arg{}
```

```shell
java -Dapplication.db.username=myuser -jar myapp.jar
```

## Secret files

Use `file{}` to read a value from a file named like the key in the secrets directory:

```yaml
session:
  cookie:
    secret: file{}
```

This reads `/run/secrets/session.cookie.secret`. The directory is `/run/secrets`, the default of Docker secrets, and can be changed with the environment variable `APPLICATION_SECRETS_PATH` or the JVM property `application.secrets.path`; it can not be set in `config.yaml`, as the files are read while `config.yaml` is resolved.

- The file name is the key as is; Kubernetes and Docker allow dots in secret names.
- Symbolic links are followed, as Kubernetes mounts secrets through them.
- One trailing line break (`\n` or `\r\n`) is removed, as files written with `echo` end with one. Everything else is kept byte for byte.
- Files larger than 64 KiB are rejected.

Kubernetes:

```yaml
volumes:
  - name: secrets
    secret:
      secretName: myapp
containers:
  - name: myapp
    volumeMounts:
      - name: secrets
        mountPath: /run/secrets
        readOnly: true
```

The keys of the Secret `myapp` are the config keys, e.g. `session.cookie.secret`.

## External configuration

You can also point the whole configuration file elsewhere, which is useful when your deployment mounts config outside the JAR entirely (a Kubernetes ConfigMap or Secret, for instance) instead of baking it into the artifact:

```shell
java -Dapplication.config=/etc/myapp/config.yaml -jar myapp.jar
```

## HTTPS certificates

An HTTPS connector reads its key and certificate chain from a PKCS12 file:

```yaml
connector:
  https:
    host: 0.0.0.0
    port: 8443
    keystore: /etc/myapp/https.p12
    password: file{}
```

`connector.https.certificate.alias` selects the entry and may stay empty if the file holds exactly one key entry. The connector accepts TLS 1.3 only. Behind a load balancer TLS usually ends at the proxy and the application runs plain HTTP, see [Operating](operating.md). For local HTTPS a tool like `mkcert` creates a trusted certificate.

See [Configuration](configuration.md) for all keys and [Migrations](migrations.md) for the move from the vault of 10.x.
