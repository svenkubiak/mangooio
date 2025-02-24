# Authentication

mangoo I/O provides built-in authentication support, including two-factor authentication, ensuring secure access control.

## Custom Authentication

mangoo I/O allows for custom registration and login processes. While it does not store user credentials, it provides functions to simplify authentication handling.

The `Authentication` class can be injected into a controller class for streamlined authentication management:

```java
public Response login(Authentication authentication) {
    ...
    return Response.withOk();
}
```

### Password Hashing

Built-in authentication in mangoo I/O utilizes Argon2 for password hashing. You can generate a hashed password using:

```java
CodecUtils.hashArgon2("password", "salt");
```

Once you have hashed the password during registration, store it securely with your user data. mangoo I/O does not manage password storage.

### User Authentication

To authenticate users, compare the stored hashed password with the provided clear-text password:

```java
if (authentication.validLogin("subject", "password", "salt", "hash")) {
    authentication.login("subject");
}
```

### Authentication Methods

The `Authentication` class provides essential methods for managing user authentication:

```java
getAuthenticatedUser(); // Retrieves the logged-in user
logout(); // Logs out the user
remember(boolean remember); // Extends cookie and token lifetime
```

### Route Protection

To secure specific classes or methods, use the predefined authentication method in `Bootstrap.java`:

```java
Bind.controller(BasicAuthenticationController.class).withRoutes(
        On.get().to("/").respondWith("index").withAuthentication()
);
```

Authentication can be enforced at both the controller and request levels.

## Two-Factor Authentication

The `Authentication` class supports Two-Factor Authentication (2FA) for enhanced security. You can integrate it into your authentication workflow with methods such as:

```java
validSecondFactor(String secret, int number);
```

This feature provides an additional layer of security, ensuring robust authentication mechanisms in your application.
