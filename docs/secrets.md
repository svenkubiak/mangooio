# Secrets

Cookie keys, the admin password, SMTP credentials, and similar values should not live in source control as clear text. mangoo I/O can resolve them from the application vault, environment variables, or JVM arguments.

## Vault

The vault is a PKCS12 keystore file named `vault.p12`. Enable it in `config.yaml`:

```yaml
default:
  application:
    secret: this-must-be-at-least-64-characters-long-and-kept-secret
    vault:
      enable: true
```

On first start the application creates `vault.p12` and fills it with random 64-character secrets for:

- `authentication.cookie.secret` / `authentication.cookie.key`
- `session.cookie.secret` / `session.cookie.key`
- `flash.cookie.secret` / `flash.cookie.key`

Each mode (`dev`, `test`, `prod`) gets its own prefixed copies of those keys.

### Vault password

The keystore password must be at least 64 characters. Resolution order:

1. Environment variable `APPLICATION_VAULT_SECRET`
2. JVM property `application.vault.secret`
3. `application.vault.secret` in `config.yaml`
4. `application.secret`

### Vault location

In **dev** and **test**, `vault.p12` is created in the application root.

In **prod**, the directory is resolved in this order:

1. Environment variable `APPLICATION_VAULT_PATH`
2. JVM property `application.vault.path`
3. `application.vault.path` in `config.yaml`
4. The current working directory

The file name is always `vault.p12`. Restrict file permissions; the framework sets owner read/write when it creates the file.

### Using vault values in config.yaml

Set a key to `vault{}` to load the matching vault entry at runtime:

```yaml
session:
  cookie:
    secret: vault{}
    key: vault{}
```

`vault{fallback}` stores the literal fallback string instead of reading the keystore. Use that only for non-secret defaults.

### HTTPS certificates

When an HTTPS connector is configured, mangoo I/O builds an SSL context from the vault. The alias defaults to `certificate` (`connector.https.certificate.alias`). A self-signed certificate is created on first start if none exists.

See [Configuration](configuration.md) for connector keys and [Operating](operating.md) for production notes.

## Environment variables

Use `env{}` to read a value from the process environment. The environment name is the config key in uppercase with dots replaced by underscores:

```yaml
application:
  db:
    username: env{}
```

This reads `APPLICATION_DB_USERNAME`. `env{defaultuser}` uses the literal default when you do not want to require an environment variable.

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

`arg{defaultuser}` uses the literal default when the property is absent.

You can also point the whole configuration file elsewhere:

```shell
java -Dapplication.config=/etc/myapp/config.yaml -jar myapp.jar
```
