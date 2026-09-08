# Secrets 🔐

Production applications need secrets that must never end up committed to git: cookie signing keys, database passwords, SMTP credentials, and the vault password itself. Rather than inventing yet another secrets format, mangoo I/O supports three resolution strategies you write directly into `config.yaml` as placeholders:

| Placeholder | Source |
|---|---|
| `vault{}` | PKCS12 keystore (`vault.p12`) created and managed by the framework |
| `env{}` | Operating-system environment variable |
| `arg{}` | JVM system property (`-D...`) |

The vault is the recommended approach for cookie keys and other values the framework reads on every request, mainly because it generates and stores strong random secrets for you, so nobody has to invent a 64-character string by hand. Environment variables and JVM arguments suit deployment-specific overrides instead, for example a MongoDB password injected by your platform's secret manager at container start.

Older versions used `cryptex{}` encryption directly in the YAML file; that was removed in 10.0 in favor of the vault, mostly because an encrypted value in a config file is still a value an attacker can attack offline if they get the file, while the vault keeps the actual secret material in a separate, restricted-permission file. See [Migrations](migrations.md) if you are upgrading from 9.x.

## Vault

The vault is a PKCS12 keystore file named `vault.p12`. Enable it in `config.yaml`:

```yaml
default:
  application:
    secret: this-must-be-at-least-64-characters-long-and-kept-secret
    vault:
      enable: true
```

On first start, the application creates `vault.p12` and fills it with random 64-character secrets for:

- `authentication.cookie.secret` / `authentication.cookie.key`
- `session.cookie.secret` / `session.cookie.key`
- `flash.cookie.secret` / `flash.cookie.key`

Each mode (`dev`, `test`, `prod`) gets its own prefixed copies of those keys, so a `vault.p12` generated on your laptop in dev mode cannot be used to forge a session cookie against a production deployment, even if a developer machine gets compromised. That isolation is the entire reason the vault is keyed per mode instead of once per application.

### Vault password

The keystore password must be at least 64 characters, long enough that it is not realistically guessable and comfortably exceeds what PBKDF2-style key stretching needs to be effective. Resolution order:

1. Environment variable `APPLICATION_VAULT_SECRET`
2. JVM property `application.vault.secret`
3. `application.vault.secret` in `config.yaml`
4. `application.secret`

### Vault location

In **dev** and **test**, `vault.p12` is created in the application root, right next to `pom.xml`, which is convenient for local development but not something you want in production, where you usually do not want secret material sitting inside the deployable artifact's working directory.

In **prod**, the directory is resolved in this order:

1. Environment variable `APPLICATION_VAULT_PATH`
2. JVM property `application.vault.path`
3. `application.vault.path` in `config.yaml`
4. The current working directory

The file name is always `vault.p12`. Restrict its file permissions; the framework already sets owner read/write only when it creates the file, but it is worth double-checking after a deploy, especially if your deployment tooling copies files around and resets permissions in the process.

### Using vault values in config.yaml

Set a key to `vault{}` to load the matching vault entry at runtime:

```yaml
session:
  cookie:
    secret: vault{}
    key: vault{}
```

`vault{fallback}` stores the literal fallback string instead of reading the keystore. Use that only for non-secret defaults; if you write an actual secret as a fallback, you have just put a secret in `config.yaml` again, which defeats the point of using the vault in the first place.

### HTTPS certificates

When an HTTPS connector is configured, mangoo I/O builds an SSL context from the vault. The alias defaults to `certificate` (`connector.https.certificate.alias`), and a self-signed certificate is generated automatically on first start if none exists yet, so you can get an HTTPS connector running locally without first going and generating a certificate by hand.

See [Configuration](configuration.md) for connector keys and [Operating](operating.md) for production notes, including how to bring your own certificate instead of the self-signed one.

## Environment variables

Use `env{}` to read a value from the process environment. The environment name is the config key in uppercase with dots replaced by underscores:

```yaml
application:
  db:
    username: env{}
```

This reads `APPLICATION_DB_USERNAME`. `env{defaultuser}` uses the literal default when you do not want to require an environment variable, which is handy in dev where you would rather not export half a dozen variables just to start the app.

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

`arg{defaultuser}` uses the literal default when the property is absent, same idea as `env{defaultuser}` above.

You can also point the whole configuration file elsewhere, which is useful when your deployment mounts config outside the JAR entirely (a Kubernetes ConfigMap, for instance) instead of baking it into the artifact:

```shell
java -Dapplication.config=/etc/myapp/config.yaml -jar myapp.jar
```
