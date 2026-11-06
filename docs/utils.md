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
```

`hashArgon2(cleartext)` uses `application.secret` as salt and embeds it in the stored hash. Use the two-argument form with a random salt per user. Both `hashArgon2` and `matchArgon2` throw an unchecked `MangooHashingException` when no hashing slot becomes free within `authentication.hashing.timeout`. `matchArgon2` does not report that as a mismatch. See [Argon2 hashing](configuration.md#argon2-hashing).

`hexSHA512` is SHA3-512 and returns `null` on failure. `randomString(int)` requires a length from 22 to 512 and uses URL-safe Base64. `serializeToBase64` and `deserializeFromBase64` need `registerSerializable(Class)` first, and only for data you trust. `bitLength` checks key material.

## JwtUtils

Sessions, authentication, and the admin cookie all use Nimbus JOSE+JWT (HS512 + JWE) under the hood. You can also issue tokens yourself for your own use cases:

`withKey` and `withSecret` take `byte[]`. `createJwt` and `parseJwt` throw the checked `MangooJwtException`. The key is HMAC-SHA-512 and must be at least 64 bytes. The secret is the direct `A256CBC-HS512` key and must be exactly 64 bytes. `JwtUtils` does not check those lengths itself. Nimbus fails later. Clock skew is fixed at 30 seconds, and `nbf` is set to now minus 30 seconds. Reserved claim names are rejected. `withClaims(Map<String, String>)` and `withJwtID(String)` add claims. `extractCustomClaims(JWTClaimsSet)` reads the ones that are not reserved.

```java
byte[] signingKey = ...;       // at least 64 bytes
byte[] encryptionSecret = ...; // exactly 64 bytes

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

Pull the key material from `env{}` or `file{}`. See [Secrets](secrets.md#secret-and-key-length).

## TotpUtils

SHA-512 TOTP with 6 digits and a 30-second period, matching what the admin dashboard uses. See [Authentication](authentication.md).

```java
String secret = TotpUtils.createSecret();
boolean valid = TotpUtils.verifyTotp(secret, code);
String qr = TotpUtils.getQRCode("user@example.com", "My App", secret);
```

`verifyTotp(secret, code)` accepts a valid code any number of times. `verifyTotp(identifier, secret, code)` accepts each step once and keeps it until `(counter + 2) * 30` seconds. `createSecret()` is 64 Base32 characters. `getQRCode` returns a Base64 PNG, not a data URI. `getTotp(secret)` returns the current code. There is no adjacent-step window. Only the current 30-second step matches.

## DateUtils

```java
Date date = DateUtils.localDateTimeToDate(LocalDateTime.now()); // application.timezone
Date day = DateUtils.localDateToDate(LocalDate.now());
String relative = DateUtils.getPrettyTime(LocalDateTime.now()); // fixed locale en-EN
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

`io.mangoo.crypto.Crypto` encrypts strings with AES-256-GCM. The key is SHA-256 of the secret after non-ASCII characters are stripped, and at least 32 characters have to remain. `encrypt(String, String)` and `decrypt(String, String)` return `null` on failure. They do not throw. RSA is 3072-bit `RSA/None/OAEPWITHSHA-512ANDMGF1PADDING` and throws the checked `MangooEncryptionException`. `generateKeyPair()`, `getKeyAsString(Key)`, `getPrivateKeyFromString` (PKCS#8 Base64), and `getPublicKeyFromString` (X.509 Base64) are the key helpers.
