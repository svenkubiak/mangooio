# Utils

The framework exposes small, focused helper classes in **`io.mangoo.utils`**. Prefer these over reimplementing crypto, JWT, or file logic yourself in application code. Types under **`io.mangoo.utils.internal`** are not public API and may change without notice, so avoid depending on them directly.

Common tasks covered: **Argon2** password hashing (`CommonUtils`), **JWT** creation and parsing (`JwtUtils`), **TOTP** for admin MFA (`TotpUtils`), JSON helpers (`JsonUtils`), and file/stream utilities (`FileUtils`).

## CommonUtils

Argon2id hashing, encoding, UUIDs, and small helpers:

```java
String hash = CommonUtils.hashArgon2("password", "salt");
boolean ok = CommonUtils.matchArgon2("password", "salt", hash);

String id = CommonUtils.uuidV7();   // also uuidV4(), uuidV6()
String random = CommonUtils.randomString(32);

byte[] encoded = CommonUtils.encodeToBase64("payload");
byte[] decoded = CommonUtils.decodeFromBase64(new String(encoded, StandardCharsets.UTF_8));

CommonUtils.blacklist("subject");
boolean blocked = CommonUtils.isBlacklisted("subject");
```

`hashArgon2(cleartext)` hashes without an explicit salt. `bitLength` checks key material length. `registerSerializable` / `serializeToBase64` use Apache Fory under the hood; register your classes at startup, and only ever deserialize trusted data.

## JwtUtils

Sessions, authentication, and the admin cookie all use Nimbus JOSE+JWT (HS512 + JWE) under the hood. You can also issue tokens yourself for your own use cases:

```java
var data = JwtUtils.JwtData.create()
    .withKey(signingKey)
    .withSecret(encryptionSecret)
    .withIssuer("myapp")
    .withAudience("myapp-api")
    .withSubject("user-1")
    .withTtlSeconds(3600);

String jwt = JwtUtils.createJwt(data);
JWTClaimsSet claims = JwtUtils.parseJwt(jwt, data);
```

Signing and encryption keys must be long enough for HS512 / AES respectively; prefer pulling those values from the [vault](secrets.md) rather than hardcoding them.

## TotpUtils

SHA-512 TOTP with 6 digits and a 30-second period, matching what the admin dashboard uses. See [Authentication](authentication.md).

```java
String secret = TotpUtils.createSecret();
boolean valid = TotpUtils.verifyTotp(secret, code);
String qr = TotpUtils.getQRCode("user@example.com", "My App", secret);
```

## DateUtils

```java
Date date = DateUtils.localDateTimeToDate(LocalDateTime.now());
Date day = DateUtils.localDateToDate(LocalDate.now());
String relative = DateUtils.getPrettyTime(LocalDateTime.now());
String de = DateUtils.getPrettyTime(Locale.GERMAN, LocalDateTime.now());
```

## JsonUtils

See [Working with JSON](working-with-json.md): `toJson`, `toPrettyJson`, `toObject`, `toFlatMap`, `getMapper()`.

## FileUtils

```java
String mime = FileUtils.getMimeType(bytes);
String size = FileUtils.readableFileSize(2048);
String text = FileUtils.readFileToString(path);
FileUtils.closeQuietly(stream);
```

## RequestUtils

```java
Optional<String> bearer = RequestUtils.getAuthorizationHeader(request);
Map<String, String> params = RequestUtils.getRequestParameters(exchange);
```

## PersistenceUtils

Maps `@Collection` classes to MongoDB collection names. You will rarely call this yourself, since the framework already registers collections at startup.

## Argument

```java
Argument.requireNonBlank(value, "value");
```

## Crypto

`io.mangoo.crypto.Crypto` encrypts with AES and can also wrap values with RSA (3072-bit, OAEP SHA-512). Inject it whenever you need application-level encryption that goes beyond what the vault already covers.
