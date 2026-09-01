# Utils

The framework exposes small, focused helper classes in **`io.mangoo.utils`**. Prefer these over duplicating crypto, JWT, or file logic in application code. Types under **`io.mangoo.utils.internal`** are not public API and may change without notice.

Common tasks: **Argon2** password hashing (`CommonUtils`), **JWT** creation and parsing (`JwtUtils`), **TOTP** for admin MFA (`TotpUtils`), JSON helpers (`JsonUtils`), and file/stream utilities (`FileUtils`).

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

`hashArgon2(cleartext)` hashes without an explicit salt. `bitLength` checks key material. `registerSerializable` / `serializeToBase64` use Apache Fory; register classes at startup and only deserialize trusted data.

## JwtUtils

Sessions, authentication, and the admin cookie use Nimbus JOSE+JWT (HS512 + JWE). You can issue tokens yourself:

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

Signing and encryption keys must be long enough for HS512 / AES. Prefer values from the [vault](secrets.md).

## TotpUtils

SHA-512 TOTP, 6 digits, 30-second period. See [Authentication](authentication.md).

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

Maps `@Collection` classes to MongoDB collection names. You rarely call this; the framework registers collections at startup.

## Argument

```java
Argument.requireNonBlank(value, "value");
```

## Crypto

`io.mangoo.crypto.Crypto` encrypts with AES and can wrap values with RSA (3072-bit, OAEP SHA-512). Inject it when you need application-level encryption beyond the vault.
