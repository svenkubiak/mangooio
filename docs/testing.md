# Testing

mangoo I/O provides tools for testing applications. These utilities are not part of the core framework and require an additional dependency. This allows setting the scope to `test` in the Maven configuration, ensuring the dependency is only included for testing.

```xml
<dependency>
    <groupId>io.mangoo</groupId>
    <artifactId>mangooio-test</artifactId>
    <version>LATEST</version>
    <scope>test</scope>
</dependency>
```

## Backend Testing

mangoo I/O includes convenient classes for unit testing applications. Below is an example of how a unit test using these utilities might look:

```java
import io.mangoo.test.utils.TestRequest;
import io.mangoo.test.utils.TestResponse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Test
public void testIndex() {
    // Given
    TestResponse response = TestRequest.get("/").execute();

    // Then
    assertThat(response, not(nullValue()));
    assertThat(response.getContentType(), equalTo(TEXT_HTML));
    assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
}
```

### Request-Response Testing

A common use case for testing is verifying request-response interactions. mangoo I/O provides utilities for constructing test requests and processing responses. The request utility supports authentication, headers, and other modifications via a fluent API.

### Simulating Browser Sessions

For scenarios where request data needs to persist across multiple requests, mangoo I/O provides the `TestBrowser` class:

```java
TestBrowser browser = TestBrowser.open();
```

This allows session persistence across multiple requests, simulating real browser interactions. Below is an example:

```java
import io.mangoo.test.utils.TestBrowser;
import io.mangoo.test.utils.TestRequest;
import io.mangoo.test.utils.TestResponse;

// Given
TestBrowser browser = TestBrowser.open();

// When
TestResponse response = browser.withUri("/dologin")
    .withMethod(Methods.POST)
    .execute();

// Then
assertThat(response, not(nullValue()));
assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));

// When
response = browser.withUri("/authenticationrequired")
    .withDisableRedirects(true)
    .withMethod(Methods.GET)
    .execute();
```

In this example, session data (such as cookies) from the first request persists into subsequent requests, allowing realistic browser-like testing of the application.
