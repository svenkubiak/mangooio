package io.mangoo.utils;

import com.google.common.net.MediaType;
import io.mangoo.TestExtension;
import io.mangoo.constants.Header;
import io.mangoo.core.Config;
import io.mangoo.exceptions.MangooJwtException;
import io.mangoo.routing.Attachment;
import io.mangoo.routing.bindings.Request;
import io.mangoo.test.concurrent.ConcurrentRunner;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.AttachmentKey;
import io.undertow.util.HeaderMap;
import io.undertow.util.Methods;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static io.mangoo.core.Application.getInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith({TestExtension.class})
class RequestUtilsTest {
    private Config config;

    @BeforeEach
    void setUp() {
        config = getInstance(Config.class);
    }

    @Test
    void testGetRequestParameters() {
        // given
        Deque<String> paramsOne = new LinkedList<>();
        paramsOne.push("foo");
        Deque<String> paramsTwo = new LinkedList<>();
        paramsTwo.push("bar");

        Map<String, Deque<String>> queryParameters = new HashMap<>();
        Map<String, Deque<String>> pathParameters = new HashMap<>();
        queryParameters.put("first", paramsOne);
        pathParameters.put("second", paramsTwo);

        HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);

        // when
        when(mockedExchange.getQueryParameters()).thenReturn(queryParameters);
        when(mockedExchange.getPathParameters()).thenReturn(pathParameters);
        Map<String, String> requestParameters = RequestUtils.getRequestParameters(mockedExchange);

        // then
        assertThat(requestParameters, not(nullValue()));
        assertThat(requestParameters.get("first"), equalTo("foo"));
        assertThat(requestParameters.get("second"), equalTo("bar"));
    }

    @Test
    void testGetRequestParametersConcurrent() {
        MatcherAssert.assertThat(t -> {
            // given
            String one = UUID.randomUUID().toString();
            String two = UUID.randomUUID().toString();
            Deque<String> paramsOne = new LinkedList<>();
            paramsOne.push(one);
            Deque<String> paramsTwo = new LinkedList<>();
            paramsTwo.push(two);

            Map<String, Deque<String>> queryParameters = new HashMap<>();
            Map<String, Deque<String>> pathParameters = new HashMap<>();
            queryParameters.put("first", paramsOne);
            pathParameters.put("second", paramsTwo);

            HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);

            // when
            when(mockedExchange.getQueryParameters()).thenReturn(queryParameters);
            when(mockedExchange.getPathParameters()).thenReturn(pathParameters);
            Map<String, String> requestParameters = RequestUtils.getRequestParameters(mockedExchange);

            return requestParameters != null && requestParameters.get("first").equals(one) && requestParameters.get("second").equals(two);
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }


    @Test
    void testIsPost() {
        // given
        HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);

        // when
        when(mockedExchange.getRequestMethod()).thenReturn(Methods.POST);
        boolean postPutPatch = RequestUtils.isPostPutPatch(mockedExchange);

        // then
        assertThat(postPutPatch, equalTo(true));
    }

    @Test
    void testIsPut() {
        // given
        HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);

        // when
        when(mockedExchange.getRequestMethod()).thenReturn(Methods.PUT);
        boolean postPutPatch = RequestUtils.isPostPutPatch(mockedExchange);

        // then
        assertThat(postPutPatch, equalTo(true));
    }

    @Test
    void testIsPatch() {
        // given
        HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);

        // when
        when(mockedExchange.getRequestMethod()).thenReturn(Methods.PUT);
        boolean postPutPatch = RequestUtils.isPostPutPatch(mockedExchange);

        // then
        assertThat(postPutPatch, equalTo(true));
    }

    @Test
    void testIsNonPostPutPatch() {
        // given
        HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);

        // when
        when(mockedExchange.getRequestMethod()).thenReturn(Methods.GET);
        boolean postPutPatch = RequestUtils.isPostPutPatch(mockedExchange);

        // then
        assertThat(postPutPatch, equalTo(false));
    }

    @Test
    void testIsJsonRequest() {
        // given
        HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);
        HeaderMap headerMap = new HeaderMap();
        headerMap.put(Header.CONTENT_TYPE, MediaType.JSON_UTF_8.withoutParameters().toString());

        // when
        when(mockedExchange.getRequestHeaders()).thenReturn(headerMap);
        boolean isJson = RequestUtils.isJsonRequest(mockedExchange);

        // then
        assertThat(isJson, equalTo(true));
    }

    @Test
    void testIsNonJsonRequest() {
        // given
        HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);
        HeaderMap headerMap = new HeaderMap();
        headerMap.put(Header.CONTENT_TYPE, MediaType.CSS_UTF_8.withoutParameters().toString());

        // when
        when(mockedExchange.getRequestHeaders()).thenReturn(headerMap);
        boolean isJson = RequestUtils.isJsonRequest(mockedExchange);

        // then
        assertThat(isJson, equalTo(false));
    }

    @Test
    void testGetAuthorizationHeader() {
        // given
        Request request = Mockito.mock(Request.class);
        String authorization = UUID.randomUUID().toString();
        String header = "Bearer " + authorization + "       ";

        // when
        when(request.getHeader(Header.AUTHORIZATION)).thenReturn(header);

        // then
        assertThat(RequestUtils.getAuthorizationHeader(request), not(nullValue()));
        assertThat(RequestUtils.getAuthorizationHeader(request).isPresent(), equalTo(true));
        assertThat(RequestUtils.getAuthorizationHeader(request).get(), equalTo(authorization));
    }

    @Test
    void testGetAttachmentKey() {
        //given
        // No setup needed for static method

        //when
        AttachmentKey<Attachment> attachmentKey = RequestUtils.getAttachmentKey();

        //then
        assertThat(attachmentKey, not(nullValue()));
        assertThat(attachmentKey, instanceOf(AttachmentKey.class));
    }

    @Test
    void testGetAttachmentKeyReturnsSameInstance() {
        //given
        // No setup needed for static method

        //when
        AttachmentKey<Attachment> attachmentKey1 = RequestUtils.getAttachmentKey();
        AttachmentKey<Attachment> attachmentKey2 = RequestUtils.getAttachmentKey();

        //then
        assertThat(attachmentKey1, sameInstance(attachmentKey2));
    }

    @Test
    void testGetRequestParametersWithQueryParameters() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.addQueryParam("param1", "value1");
        exchange.addQueryParam("param2", "value2");

        //when
        Map<String, String> parameters = RequestUtils.getRequestParameters(exchange);

        //then
        assertThat(parameters, not(nullValue()));
        assertThat(parameters.get("param1"), equalTo("value1"));
        assertThat(parameters.get("param2"), equalTo("value2"));
        assertThat(parameters.size(), equalTo(2));
    }

    @Test
    void testGetRequestParametersWithPathParameters() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.addPathParam("pathParam1", "pathValue1");
        exchange.addPathParam("pathParam2", "pathValue2");

        //when
        Map<String, String> parameters = RequestUtils.getRequestParameters(exchange);

        //then
        assertThat(parameters, not(nullValue()));
        assertThat(parameters.get("pathParam1"), equalTo("pathValue1"));
        assertThat(parameters.get("pathParam2"), equalTo("pathValue2"));
        assertThat(parameters.size(), equalTo(2));
    }

    @Test
    void testGetRequestParametersWithBothQueryAndPathParameters() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.addQueryParam("queryParam", "queryValue");
        exchange.addPathParam("pathParam", "pathValue");

        //when
        Map<String, String> parameters = RequestUtils.getRequestParameters(exchange);

        //then
        assertThat(parameters, not(nullValue()));
        assertThat(parameters.get("queryParam"), equalTo("queryValue"));
        assertThat(parameters.get("pathParam"), equalTo("pathValue"));
        assertThat(parameters.size(), equalTo(2));
    }

    @Test
    void testGetRequestParametersWithEmptyParameters() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);

        //when
        Map<String, String> parameters = RequestUtils.getRequestParameters(exchange);

        //then
        assertThat(parameters, not(nullValue()));
        assertThat(parameters.isEmpty(), is(true));
    }

    @Test
    void testGetRequestParametersWithNullExchange() {
        //given
        // No setup needed for null input

        //when & then
        assertThrows(NullPointerException.class, () -> RequestUtils.getRequestParameters(null));
    }

    @Test
    void testIsPostPutPatchWithPostRequest() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.POST);

        //when
        boolean isPostPutPatch = RequestUtils.isPostPutPatch(exchange);

        //then
        assertThat(isPostPutPatch, is(true));
    }

    @Test
    void testIsPostPutPatchWithPutRequest() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.PUT);

        //when
        boolean isPostPutPatch = RequestUtils.isPostPutPatch(exchange);

        //then
        assertThat(isPostPutPatch, is(true));
    }

    @Test
    void testIsPostPutPatchWithPatchRequest() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.PATCH);

        //when
        boolean isPostPutPatch = RequestUtils.isPostPutPatch(exchange);

        //then
        assertThat(isPostPutPatch, is(true));
    }

    @Test
    void testIsPostPutPatchWithGetRequest() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.GET);

        //when
        boolean isPostPutPatch = RequestUtils.isPostPutPatch(exchange);

        //then
        assertThat(isPostPutPatch, is(false));
    }

    @Test
    void testIsPostPutPatchWithDeleteRequest() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.setRequestMethod(Methods.DELETE);

        //when
        boolean isPostPutPatch = RequestUtils.isPostPutPatch(exchange);

        //then
        assertThat(isPostPutPatch, is(false));
    }

    @Test
    void testIsPostPutPatchWithNullExchange() {
        //given
        // No setup needed for null input

        //when & then
        assertThrows(NullPointerException.class, () -> RequestUtils.isPostPutPatch(null));
    }

    @Test
    void testIsJsonRequestWithJsonContentType() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.getRequestHeaders().put(Header.CONTENT_TYPE, "application/json");

        //when
        boolean isJsonRequest = RequestUtils.isJsonRequest(exchange);

        //then
        assertThat(isJsonRequest, is(true));
    }

    @Test
    void testIsJsonRequestWithJsonContentTypeAndCharset() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.getRequestHeaders().put(Header.CONTENT_TYPE, "application/json; charset=utf-8");

        //when
        boolean isJsonRequest = RequestUtils.isJsonRequest(exchange);

        //then
        assertThat(isJsonRequest, is(true));
    }

    @Test
    void testIsJsonRequestWithXmlContentType() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.getRequestHeaders().put(Header.CONTENT_TYPE, "application/xml");

        //when
        boolean isJsonRequest = RequestUtils.isJsonRequest(exchange);

        //then
        assertThat(isJsonRequest, is(false));
    }

    @Test
    void testIsJsonRequestWithTextContentType() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);
        exchange.getRequestHeaders().put(Header.CONTENT_TYPE, "text/plain");

        //when
        boolean isJsonRequest = RequestUtils.isJsonRequest(exchange);

        //then
        assertThat(isJsonRequest, is(false));
    }

    @Test
    void testIsJsonRequestWithNoContentType() {
        //given
        HttpServerExchange exchange = new HttpServerExchange(null);

        //when
        boolean isJsonRequest = RequestUtils.isJsonRequest(exchange);

        //then
        assertThat(isJsonRequest, is(false));
    }

    @Test
    void testIsJsonRequestWithNullExchange() {
        //given
        // No setup needed for null input

        //when & then
        assertThrows(NullPointerException.class, () -> RequestUtils.isJsonRequest(null));
    }

    @Test
    void testHasValidAuthenticationWithValidCookie() {
        //given
        String validJwt = createValidJwt();
        String cookie = config.getAuthenticationCookieName() + "=" + validJwt;

        //when
        boolean hasValidAuth = RequestUtils.hasValidAuthentication(cookie);

        //then
        assertThat(hasValidAuth, is(true));
    }

    @Test
    void testHasValidAuthenticationWithInvalidCookie() {
        //given
        String cookie = config.getAuthenticationCookieName() + "=invalid-jwt-token";

        //when
        boolean hasValidAuth = RequestUtils.hasValidAuthentication(cookie);

        //then
        assertThat(hasValidAuth, is(false));
    }

    @Test
    void testHasValidAuthenticationWithEmptyCookie() {
        //given
        String cookie = "";

        //when
        boolean hasValidAuth = RequestUtils.hasValidAuthentication(cookie);

        //then
        assertThat(hasValidAuth, is(false));
    }

    @Test
    void testHasValidAuthenticationWithNullCookie() {
        //given
        // No setup needed for null input

        //when
        boolean hasValidAuth = RequestUtils.hasValidAuthentication(null);

        //then
        assertThat(hasValidAuth, is(false));
    }

    @Test
    void testHasValidAuthenticationWithBlankCookie() {
        //given
        String cookie = "   ";

        //when
        boolean hasValidAuth = RequestUtils.hasValidAuthentication(cookie);

        //then
        assertThat(hasValidAuth, is(false));
    }

    @Test
    void testHasValidAuthenticationWithCookieContainingQuotes() {
        //given
        String validJwt = createValidJwt();
        String cookie = config.getAuthenticationCookieName() + "=\"" + validJwt + "\"";

        //when
        boolean hasValidAuth = RequestUtils.hasValidAuthentication(cookie);

        //then
        assertThat(hasValidAuth, is(true));
    }

    @Test
    void testHasValidAuthenticationWithMultipleCookies() {
        //given
        String validJwt = createValidJwt();
        String cookie = "otherCookie=value; " + config.getAuthenticationCookieName() + "=" + validJwt + "; anotherCookie=value2";

        //when
        boolean hasValidAuth = RequestUtils.hasValidAuthentication(cookie);

        //then
        assertThat(hasValidAuth, is(true));
    }

    @Test
    void testHasValidAuthenticationWithWrongCookieName() {
        //given
        String validJwt = createValidJwt();
        String cookie = "wrongCookieName=" + validJwt;

        //when
        boolean hasValidAuth = RequestUtils.hasValidAuthentication(cookie);

        //then
        assertThat(hasValidAuth, is(false));
    }

    @Test
    void testGetAuthorizationHeaderWithNoAuthorizationHeader() {
        //given
        Request request = new Request();

        //when
        Optional<String> authorization = RequestUtils.getAuthorizationHeader(request);

        //then
        assertThat(authorization.isPresent(), is(false));
    }

    @Test
    void testGetAuthorizationHeaderWithNullRequest() {
        //given
        // No setup needed for null input

        //when & then
        assertThrows(NullPointerException.class, () -> RequestUtils.getAuthorizationHeader(null));
    }

    private String createValidJwt() {
        try {
            var jwtData = JwtUtils.JwtData.create()
                    .withKey(config.getAuthenticationCookieKey())
                    .withSubject("fooobar")
                    .withSecret(config.getAuthenticationCookieSecret())
                    .withIssuer(config.getApplicationName())
                    .withAudience(config.getAuthenticationCookieName())
                    .withTtlSeconds(config.getAuthenticationCookieTokenExpires());

            return JwtUtils.createJwt(jwtData);
        } catch (MangooJwtException e) {
            throw new RuntimeException("Failed to create valid JWT for testing", e);
        }
    }
}
