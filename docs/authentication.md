mangoo I/O comes with two authentication implementations out of the box: HTTP Basic authentication and build-in functionalities for for authentication.

## Basic authentication

The HTTP Basic authentication in mangoo I/O can be handled on a per controller method basis or on a per request method. This gives you the option to use different credentials. To add Basic authentication to your request, just add a username and password to the request in your Bootstrap.java as done in the following example:

```java
Bind.controller(BasicAuthenticationController.class).withRoutes(   								On.get().to("/").respondeWith("index").withBasicAuthentication("foo", "bar")
);
```

It is recommended to encrypt at least the password using the build in public/private key encryption.

## Custom authentication

mangoo I/O supports you when a custom registration with a custom login process is required. Although mangoo I/O does not store any credentials or user data for you, it gives you some handy functions to make handling of authentication as easy as possible.

mangoo I/O offers the Authentication class which can be simply injected into a controller class.

```java
public Response login(Authentication authentication) {
    ...
    return Response.withOk();
}
```

The authentication uses BCrypt provided by jBCrypt for password hashing. This means, that you don’t have to store a salt along with the user data, just the hashed password. This hashed value can be created with the following method

```java
CodecUtils.hexJBcrypt(...);
```

After you have created the hash of the cleartext password of your user, you have to store it with your user data. mangoo I/O does not do that for you.

The Authentication class offers convenient functions to perform authentication. The main methods are

```java
getAuthenticatedUser()
validLogin(String username, String password, String hash)
logout()
remember(boolean remember)
```

To protect class and methods to require an authenticated user, mangoo I/O offers a predefined method that can be used when defining routes in your Bootstrap.java file.

```java
Bind.controller(BasicAuthenticationController.class).withRoutes(   								On.get().to("/").respondeWith("index").withAuthentication()
);
```

Authentication can be handled on a per controller as well as a per request level.

#### Two Factor Authentication

The Authentication class also provides some convinent methods for Two Factor Authentication which can be integrated into your authentication workflow. For example:

```java
validSecondFactor(String secret, int number)
```
