# Testing

Integration-style HTTP tests are supported through the **`mangooio-test`** artifact. It starts the real application stack in **test** mode, with the same routing, config merge, and handlers as production, and exposes helpers to send requests against the configured HTTP connector.

That approach catches issues unit tests miss entirely: wrong routes, filter ordering, cookie handling, and template rendering. The trade-off is speed, since the app is booted once via `TestRunner` and shared by all test classes of the JVM, so keep individual tests focused and do not rely on state another test class left behind.

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>io.mangoo</groupId>
    <artifactId>mangooio-test</artifactId>
    <version>11.0.0</version>
    <scope>test</scope>
</dependency>
```

Use the latest published version rather than the placeholder shown above.

## Starting the application

Extend your tests with `io.mangoo.test.TestRunner`, and it starts mangoo I/O in **test** mode once per JVM; all test classes share that instance:

```java
import io.mangoo.test.TestRunner;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestRunner.class)
class ApplicationControllerTest {
}
```

Override `beforeStartup()` / `afterStartup()` if you need to set system properties before `Application.start(Mode.TEST)` runs. The application starts once per JVM. Only the extension instance that wins that start runs the hooks, so a subclass used as `@ExtendWith` is what makes an override reliable. `-Dapplication.mode` still wins over `Mode.TEST`. `close()` does nothing. The runner does not shut the JVM down.

## HTTP requests

```java
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Test
void testIndex() {
    TestResponse response = TestRequest.get("/").execute();

    assertThat(response, not(nullValue()));
    assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    assertThat(response.getContent(), containsString("Hello"));
}
```

Factory methods cover every verb you would need: `TestRequest.get/post/put/patch/delete/head/options(uri)`.

Chain on fluent extras before calling `execute()`:

```java
TestRequest.post("/save")
    .withHeader("X-Request-Id", "1")
    .withContentType("application/json")
    .withStringBody("{\"name\":\"Ada\"}")
    .withCookie(cookie)
    .withBasicAuthentication("user", "pass")
    .withDisabledRedirects()
    .withTimeout(5, ChronoUnit.SECONDS)
    .execute();
```

`withForm(Multimap)` forces POST and sends `application/x-www-form-urlencoded`.

`execute()` always calls `http://{connector.http.host}:{connector.http.port}`. An HTTPS-only application is not what this client talks to. Redirects are followed unless `withDisabledRedirects()` is set. The request timeout defaults to 5 seconds, and the connect timeout is 5 seconds. `withBasicAuthentication` installs a JDK `Authenticator`. It does not set an `Authorization` header itself.

There is no helper for WebSocket or SSE connections. See [Routing](routing.md#websockets).

## Browser sessions

`TestBrowser` keeps cookies across calls, so it can carry a login through to later requests:

```java
import io.mangoo.test.http.TestBrowser;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

TestBrowser browser = TestBrowser.open();

TestResponse login = browser.to("/dologin")
    .withHTTPMethod(Methods.POST.toString())
    .withDisabledRedirects()
    .execute();

assertThat(login.getStatusCode(), equalTo(StatusCodes.FOUND));

TestResponse account = browser.to("/authenticationrequired")
    .withHTTPMethod(Methods.GET.toString())
    .execute();
```

## Email and concurrency

`io.mangoo.test.email.SmtpMock` binds GreenMail to `smtp.host` and `smtp.port`. `start()` does nothing outside dev and test, and it does not change `smtp.protocol`. GreenMail speaks plain SMTP, so set `smtp.protocol: smtp` and an unprivileged port such as `3025` in the test environment. Obtain the mock with `Application.getInstance(SmtpMock.class)`. Call `start()` / `stop()` around the test and read captured messages from `getGreenMail()`.

`io.mangoo.test.concurrent.ConcurrentRunner` runs a Cactoos `Func` on many virtual threads. The default count is `availableProcessors() << 4`. It is a Hamcrest matcher, not a `java.util.function` type.

`io.mangoo.test.hamcrest.RegexMatcher.matches(regex)` lets you assert on response bodies with a regular expression instead of an exact string match.
