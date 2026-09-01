# Testing

HTTP tests live in the `mangooio-test` artifact. Put it on the test classpath:

```xml
<dependency>
    <groupId>io.mangoo</groupId>
    <artifactId>mangooio-test</artifactId>
    <version>10.11.0</version>
    <scope>test</scope>
</dependency>
```

Use the latest published version instead of a placeholder.

## Starting the application

Extend tests with `io.mangoo.test.TestRunner`. It starts mangoo I/O in **test** mode once per test class:

```java
import io.mangoo.test.TestRunner;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(TestRunner.class)
class ApplicationControllerTest {
}
```

Override `beforeStartup()` / `afterStartup()` if you need system properties before `Application.start(Mode.TEST)`.

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

Factories: `TestRequest.get/post/put/patch/delete/head/options(uri)`.

Fluent extras on `TestResponse`:

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

`withForm(Multimap)` sends `application/x-www-form-urlencoded` as POST.

## Browser sessions

`TestBrowser` keeps cookies across calls:

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

`io.mangoo.test.email.SmtpMock` starts GreenMail in dev/test using `smtp.host` and `smtp.port`. Call `start()` / `stop()` and inspect `getGreenMail()`.

`io.mangoo.test.concurrent.ConcurrentRunner` is a Hamcrest matcher that runs a function on many virtual threads.

`io.mangoo.test.hamcrest.RegexMatcher.matches(regex)` matches response bodies.
