## From 9.x to 10.0.0
mangoo I/O 9.0.0 is a major release and contains code that will break API compatibility. These are the changes you need to consider when upgrading from 9.x:

**Removed cryptex{}**

cryptex{} usage in the config.yml has been replace by a new vault implementation based on Java KeyStore. Up on first
start the application will create a new vault.p12 files with already predefined keys. See the documentation on
how to use the new vault features with config.yaml.

**Switched Paseto to JWT**
The Paseto based implementation for cookie has been replaced by JWT. If you have previously used the Paseto library
you need to either use the library directly or adopt to the new JWT handling based on Nimbus JOSE+JWT.

## From 9.9.0 to 9.10.0
This is a drop-in replacement.

## From 9.8.0 to 9.9.0
This is a drop-in replacement.

## From 9.7.0 to 9.8.0
This is a drop-in replacement.

## From 9.6.0 to 9.7.0
This is a drop-in replacement.

## From 9.5.0 to 9.6.0
This is a drop-in replacement.

## From 9.4.0 to 9.5.0
This is a drop-in replacement.

## From 9.3.0 to 9.4.0
This is a drop-in replacement.

## From 9.2.0 to 9.3.0
This is a drop-in replacement.

## From 9.1.0 to 9.2.0
This is a drop-in replacement.

## From 9.0.0 to 9.1.0
This is a drop-in replacement.

## From 8.11.0 to 9.0.0
mangoo I/O 9.0.0 is a major release and contains code that will break API compatibility. These are the changes you need to consider when upgrading from 8.x:

**Removed Basic HTTP authentication**

The basic HTTP authentication that came with mangoo I/O has been removed. This should be done in a HTTP Proxy in front of your application.

**Refactored Response class**

The Response class and the handling of a response in a controller has been changed. Previously when a Response was returned in a controller, mangoo I/O automatically looked up the corresponding .ftl template and rendered it. Now when returning a Response.ok() it returns an empty response. Rendering only takes place when calling Response.ok().render() or when passing a variable to the template via Response.ok().render ("foo", "bar").

**Removed @admin/health endpoint**

The @admin/health endpoint is not available anymore.

**Switched from props based configuration yaml based configuration**

Please check the [updated documentation](configuration.md) for further details.

## From 8.10.0 to 8.11.0
This is a drop-in replacement.

## From 8.9.0 to 8.10.0
This is a drop-in replacement.

## From 8.8.0 to 8.9.0
This is a drop-in replacement.

## From 8.7.0 to 8.8.0
This is a drop-in replacement.

## From 8.6.0 to 8.7.0
This is a drop-in replacement.

## From 8.5.0 to 8.6.0
This is a drop-in replacement.

## From 8.4.0 to 8.5.0
This is a drop-in replacement.

## From 8.3.0 to 8.4.0
This is a drop-in replacement.

## From 8.2.0 to 8.3.0
This is a drop-in replacement.

## From 8.1.0 to 8.2.0
This is a drop-in replacement.

## From 8.0.0 to 8.1.0
This is a drop-in replacement.

## From 7.19.0 to 8.0.0
mangoo I/O 8.0.0 is a major release and contains code that will break API compatibility. These are the changes you need to consider when upgrading from 7.x:

**Java 21**

mangoo I/O now requires and uses Java 21.

**Removed Morphia in favour of direct MongoDB integration**

One of the major changes in 8.0.0 is the removal of Morphia in favour of a direct MongoDB integration. mangoo I/O now directly works with the native MongoDB Java Driver. Please check the documentation on how to handle persistence from now on.