mangoo I/O ships with convenient tools for testing your application. Please note, that these utilities are not part of the core and come with a additional dependency. This is mainly because you want to set the scope of this dependency set to “test” in your maven configuration.

```properties
<dependency>
    <groupId>io.mangoo</groupId>
    <artifactId>mangooio-test</artifactId>
    <version>5.0.0</version>
    <scope>test</scope>
</dependency>
```

## Backend testing

mangoo I/O provides convinent classes to support unit testing your application.

Here is an example of how a unit test with the test utilities might look like.

```java
...
import io.mangoo.test.utils.TestRequest;
import io.mangoo.test.utils.TestResponse;
...

@Test
public void testIndex() {
    //given
    TestResponse response = TestRequest.get("/").execute();

    //then
    assertThat(response, not(nullValue()));
    assertThat(response.getContentType(), equalTo(TEXT_HTML));
    assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
}
```

The most common use case is probably a request-response test with your application. Therefore, mangoo I/O provides your with a test utility for Request and Response. You can add authentication, headers, etc. to the request. Check the fluent API of the Request object for this.

There may be situation where you need to pass the request information along to the request. For this scenarios mangoo I/O provides you with the Browser class.

```java
WebBrowser browser = WebBrowser.open();
```

The browser class enables you to pass to keep the request information on the following requests. Here is an example on how this might look like.

```java
...
import io.mangoo.test.utils.TestBrowser;
import io.mangoo.test.utils.TestRequest;
import io.mangoo.test.utils.TestResponse;
...

//given
TestBrowser browser = TestBrowser.open();

//when
TestResponse response = browser.withUri("/dologin")
    .withMethod(Methods.POST)
    .execute();

//then
assertThat(response, not(nullValue()));
assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));

//when
response = browser.withUri("/authenticationrequired")
    .withDisableRedirects(true)
    .withMethod(Methods.GET)
    .execute();
```

The information from the first request, like cookies, etc. will be passed to the following request, enabling you a browser-like testing of your application.

