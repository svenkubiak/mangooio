package io.mangoo.controllers;

import io.mangoo.TestExtension;
import io.mangoo.cache.Cache;
import io.mangoo.constants.Header;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooJwtException;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;
import org.apache.logging.log4j.util.Strings;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class ApplicationControllerTest {
    private static final String JSON = "{\"foo\":\"bar\"}";
    private static final String JSON_PLAIN = "{foo=bar}";
    private static final String TEXT_PLAIN = "text/plain; charset=UTF-8";
    private static final String TEXT_HTML = "text/html; charset=UTF-8";

    @Test
    void testIndex() {
        //given
        final TestResponse response = TestRequest.get("/").execute();
        
        Application.getInstance(Cache.class).put("foo", "bar");

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }

    @Test
    void testOriginSuccess() {
        //given
        final TestResponse response = TestRequest.get("/origin").withHeader("Origin", "http://foo.de").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }

    @Test
    void testOriginSuccessFailture() {
        //given
        final TestResponse response = TestRequest.get("/origin").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
    }

    @Test
    void testAnyOf() {
        //given
        final TestResponse deleteResponse = TestRequest.delete("/").execute();
        final TestResponse patchResponse = TestRequest.patch("/").execute();
        final TestResponse putResponse = TestRequest.put("/").execute();

        //then
        assertThat(deleteResponse, not(nullValue()));
        assertThat(patchResponse, not(nullValue()));
        assertThat(putResponse, not(nullValue()));
        assertThat(deleteResponse.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(patchResponse.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(putResponse.getStatusCode(), equalTo(StatusCodes.METHOD_NOT_ALLOWED));
    }

    @Test
    void testRoute() {
        //given
        final TestResponse response = TestRequest.get("/route").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("/route"));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
    }
    
    @Test
    void testNamed() {
        //given
        final TestResponse response = TestRequest.get("/named").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("injected"));
    }

    @Test
    void testApiFilterUnauthorized() {
        //given
        final TestResponse response = TestRequest.get("/api-filter").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
    }

    @Test
    void testApiFilterAuthorized() throws MangooJwtException {
        //given
        final TestResponse response = TestRequest
                .get("/api-filter")
                .withHeader("Authorization", Application.getInstance(Config.class).getString("application.api.key"))
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }

    @Test
    void testReverse() {
        //given
        final TestResponse response = TestRequest.get("/reverse").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), Matchers.anyOf(equalTo("/string\n/int/23\n/multiple/11/42"), equalTo("/string\r\n/int/23\r\n/multiple/11/42")));
        
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
    }
    
    @ParameterizedTest
    @CsvSource({
        ", we are at locationwe are at application controller",
        "/controller, we are at application controller",
        "/8282838477, we are at locationwe are at application controller",
    })
    void testLocation(String parameter, String result) {
        //given
    	if (parameter == null) {parameter = Strings.EMPTY;};
        final TestResponse response = TestRequest.get("/location" + parameter).execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo(result));
    }
    
    @Test
    void testPrettyTime() {
        //given
        final TestResponse response = TestRequest.get("/prettytime")
                .withHeader("Accept-Language", "de-DE")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString("vor"));
        assertThat(response.getContent(), containsString("Stunden"));
    }

    @Test
    void testRequest() {
        //given
        final TestResponse response = TestRequest.get("/request").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("/request"));
    }

    @Test
    void testRedirectWithDisableRedirects() {
        //given
        final TestResponse response = TestRequest.get("/redirect").withDisabledRedirects().execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getResponseUrl(), equalTo("http://localhost:10808"));
    }

    @Test
    void testRedirectWithoutDisableRedirects() {
        //given
        final TestResponse response = TestRequest.get("/redirect").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getResponseUrl(), equalTo("http://localhost:10808"));
    }

    @Test
    void testPlainText() {
        //given
        final TestResponse response = TestRequest.get("/text").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }

    @Test
    void testNotFound() {
        //given
        final TestResponse response = TestRequest.get("/foo").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_FOUND));
    }

    @Test
    void testForbidden() {
        //given
        final TestResponse response = TestRequest.get("/forbidden").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
    }

    @Test
    void testNotModified() {
        //given
        final TestResponse response = TestRequest.get("/not-modified").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_MODIFIED));
    }

    @Test
    void testBadRequest() {
        //given
        final TestResponse response = TestRequest.get("/badrequest").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.BAD_REQUEST));
    }

    @Test
    void testUnauthorized() {
        //given
        final TestResponse response = TestRequest.get("/unauthorized").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
    }

    @Test
    void testAdditionalHeaders() {
        //given
        final TestResponse response = TestRequest.get("/header").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getHeader("Access-Control-Allow-Origin"), equalTo("https://mangoo.io"));
    }

    @Test
    void testPost() {
        //given
        final TestResponse response = TestRequest.post("/post")
                .withStringBody("Winter is coming!")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo("Winter is coming!"));
    }

    @Test
    void testPut() {
        //given
        final TestResponse response = TestRequest.put("/put")
                .withStringBody("The king of the north!")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo("The king of the north!"));
    }

    @Test
    void testJsonBoonWithPost() {
        //given
        final TestResponse response = TestRequest.post("/jsonboonpost")
                .withStringBody(JSON)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo(JSON_PLAIN));
    }

    @Test
    void testDefault200() {
        //given
        final TestResponse response = TestRequest.get("/default-200")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString("200"));
        assertThat(response.getContent(), containsString("The server successfully processed the request."));
    }

    @Test
    void testDefault400() {
        //given
        final TestResponse response = TestRequest.get("/default-400")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.BAD_REQUEST));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString("400"));
        assertThat(response.getContent(), containsString("The server didn't understand the syntax of the request."));
    }

    @Test
    void testDefault401() {
        //given
        final TestResponse response = TestRequest.get("/default-401")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString("401"));
        assertThat(response.getContent(), containsString("The request requires user authentication."));
    }

    @Test
    void testDefault403() {
        //given
        final TestResponse response = TestRequest.get("/default-403")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString("403"));
        assertThat(response.getContent(), containsString("The server is refusing the request."));
    }

    @Test
    void testDefault404() {
        //given
        final TestResponse response = TestRequest.get("/default-404")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_FOUND));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString("404"));
        assertThat(response.getContent(), containsString("The server has not found anything matching the Request-URI."));
    }

    @Test
    void testDefault500() {
        //given
        final TestResponse response = TestRequest.get("/default-500")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.INTERNAL_SERVER_ERROR));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString("500"));
        assertThat(response.getContent(), containsString("The server encountered something it didn't expect and was unable to complete the request."));
    }

    @Test
    void testDefaultXXX() {
        //given
        final TestResponse response = TestRequest.get("/default-xxx")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.TOO_MANY_REQUESTS));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString("429"));
        assertThat(response.getContent(), containsString("The server returned HTTP status code 429."));
    }

    @Test
    void testJsonBoonWithPut() {
        //given
        final TestResponse response = TestRequest.put("/jsonboonput")
                .withStringBody(JSON)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), equalTo(JSON_PLAIN));
    }

    @Test
    void testUnhinderedText() {
        //given
        final TestResponse response = TestRequest.get("/unrendered/text")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("This is unrendered"));
    }
    
    @Test
    void testFreemarkerConfiguration() {
        //given
        final TestResponse response = TestRequest.get("/freemarker").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString("Output format: HTML"));
        assertThat(response.getContent(), containsString("Auto-escaping: true"));
    }
    
    @Test
    void testHeaders() {
        //given
        final TestResponse response = TestRequest.get("/").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getHeader(Headers.SERVER.toString()), equalTo("Undertow"));
        assertThat(response.getHeader(Header.X_XSS_PROTECTION.toString()), equalTo("1"));
        assertThat(response.getHeader(Header.X_CONTENT_TYPE_OPTIONS.toString()), equalTo("nosniff"));
        assertThat(response.getHeader(Header.X_FRAME_OPTIONS.toString()), equalTo("DENY"));
        assertThat(response.getHeader(Header.CONTENT_SECURITY_POLICY.toString()), equalTo(""));
        assertThat(response.getHeader(Header.FEATURE_POLICY.toString()), equalTo("myFeaturePolicy"));
        assertThat(response.getHeader(Header.REFERER_POLICY.toString()), equalTo("no-referrer"));
        assertThat(response.getHeader(Header.PERMISSIONS_POLICY.toString()), equalTo("myPermissionsPolicy"));
    }
    
    @Test
    void testCorsHeaders() {
        //given
        final TestResponse response = TestRequest.options("/api")
                .withHeader("Origin", "localhost")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getHeader("Access-Control-Allow-Origin"), equalTo("localhost"));
        assertThat(response.getHeader("Access-Control-Allow-Credentials"), equalTo("true"));
        assertThat(response.getHeader("Access-Control-Allow-Headers"), equalTo("Content-Range,ETag"));
        assertThat(response.getHeader("Access-Control-Allow-Methods"), equalTo("GET,POST,PATCH"));
        assertThat(response.getHeader("Access-Control-Expose-Headers"), equalTo("Authorization,Content-Type"));
        assertThat(response.getHeader("Access-Control-Max-Age"), equalTo("86400"));
    }
}