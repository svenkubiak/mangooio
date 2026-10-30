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

Override `beforeStartup()` / `afterStartup()` if you need to set system properties before `Application.start(Mode.TEST)` runs. Overrides take effect only if that runner starts the application first.

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

`withForm(Multimap)` sends the body as `application/x-www-form-urlencoded`, as a POST.

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

`io.mangoo.test.email.SmtpMock` starts GreenMail in dev/test using `smtp.host` and `smtp.port`. GreenMail speaks plain SMTP, so set `smtp.protocol: smtp` and an unprivileged `smtp.port` such as `3025` in the test environment, and obtain the mock with `Application.getInstance(SmtpMock.class)`. Call `start()` / `stop()` around your test and inspect captured messages through `getGreenMail()`.

`io.mangoo.test.concurrent.ConcurrentRunner` is a Hamcrest matcher that runs a function across many virtual threads at once, useful for shaking out concurrency bugs a single-threaded test would never hit.

`io.mangoo.test.hamcrest.RegexMatcher.matches(regex)` lets you assert on response bodies with a regular expression instead of an exact string match.
