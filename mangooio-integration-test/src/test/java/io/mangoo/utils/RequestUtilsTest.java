package io.mangoo.utils;

import com.google.common.net.MediaType;
import io.mangoo.TestExtension;
import io.mangoo.constants.Header;
import io.mangoo.test.concurrent.ConcurrentRunner;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Methods;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.when;

@ExtendWith({TestExtension.class})
class RequestUtilsTest {

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
    void testWrapSecurity() {
        // given
        HttpHandler handler = Mockito.mock(HttpHandler.class);

        // when
        HttpHandler security = RequestUtils.wrapBasicAuthentication(handler, "foo", "bar");

        // then
        assertThat(security, not(equalTo(null)));
        assertThat(security.getClass().getSimpleName(), equalTo("SecurityInitialHandler"));
    }
    
    @Test
    void testWrapSecurityConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            // given
            HttpHandler handler = Mockito.mock(HttpHandler.class);

            // when
            HttpHandler security = RequestUtils.wrapBasicAuthentication(handler, "foo", "bar");
            
            // then
            return security.getClass().getSimpleName().equals("SecurityInitialHandler");
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
}