package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;

import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.Test;
import org.llorllale.cactoos.matchers.RunsInThreads;
import org.mockito.Mockito;

import com.google.common.net.MediaType;

import io.mangoo.TestSuite;
import io.mangoo.enums.Header;
import io.mangoo.enums.oauth.OAuthProvider;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import io.undertow.util.Methods;

/**
 * 
 * @author svenkubiak
 *
 */
public class RequestUtilsTest {

    @Test
    public void testGetRequestParameters() {
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
    public void testGetRequestParametersConcurrent() {
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
        }, new RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
    }


    @Test
    public void testIsPost() {
        // given
        HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);

        // when
        when(mockedExchange.getRequestMethod()).thenReturn(Methods.POST);
        boolean postPutPatch = RequestUtils.isPostPutPatch(mockedExchange);

        // then
        assertThat(postPutPatch, equalTo(true));
    }

    @Test
    public void testIsPut() {
        // given
        HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);

        // when
        when(mockedExchange.getRequestMethod()).thenReturn(Methods.PUT);
        boolean postPutPatch = RequestUtils.isPostPutPatch(mockedExchange);

        // then
        assertThat(postPutPatch, equalTo(true));
    }

    @Test
    public void testIsPatch() {
        // given
        HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);

        // when
        when(mockedExchange.getRequestMethod()).thenReturn(Methods.PUT);
        boolean postPutPatch = RequestUtils.isPostPutPatch(mockedExchange);

        // then
        assertThat(postPutPatch, equalTo(true));
    }

    @Test
    public void testIsNonPostPutPatch() {
        // given
        HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);

        // when
        when(mockedExchange.getRequestMethod()).thenReturn(Methods.GET);
        boolean postPutPatch = RequestUtils.isPostPutPatch(mockedExchange);

        // then
        assertThat(postPutPatch, equalTo(false));
    }

    @Test
    public void testIsJsonRequest() {
        // given
        HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);
        HeaderMap headerMap = new HeaderMap();
        headerMap.put(Header.CONTENT_TYPE.toHttpString(), MediaType.JSON_UTF_8.withoutParameters().toString());

        // when
        when(mockedExchange.getRequestHeaders()).thenReturn(headerMap);
        boolean isJson = RequestUtils.isJsonRequest(mockedExchange);

        // then
        assertThat(isJson, equalTo(true));
    }

    @Test
    public void testIsNonJsonRequest() {
        // given
        HttpServerExchange mockedExchange = Mockito.mock(HttpServerExchange.class);
        HeaderMap headerMap = new HeaderMap();
        headerMap.put(Header.CONTENT_TYPE.toHttpString(), MediaType.CSS_UTF_8.withoutParameters().toString());

        // when
        when(mockedExchange.getRequestHeaders()).thenReturn(headerMap);
        boolean isJson = RequestUtils.isJsonRequest(mockedExchange);

        // then
        assertThat(isJson, equalTo(false));
    }

    @Test
    public void testWrapSecurity() {
        // given
        HttpHandler handler = Mockito.mock(HttpHandler.class);

        // when
        HttpHandler security = RequestUtils.wrapSecurity(handler, "foo", "bar");

        // then
        assertThat(security, not(equalTo(null)));
        assertThat(security.getClass().getSimpleName(), equalTo("SecurityInitialHandler"));
    }
    
    @Test
    public void testWrapSecurityConcurrent() throws InterruptedException {
        MatcherAssert.assertThat(t -> {
            // given
            HttpHandler handler = Mockito.mock(HttpHandler.class);

            // when
            HttpHandler security = RequestUtils.wrapSecurity(handler, "foo", "bar");
            
            // then
            return security != null && security.getClass().getSimpleName().equals("SecurityInitialHandler");
        }, new RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
    }

    @Test
    public void testGetOAuthProvider() {
        // when
        Optional<OAuthProvider> twitter = RequestUtils.getOAuthProvider("twitter");
        Optional<OAuthProvider> facebook = RequestUtils.getOAuthProvider("facebook");
        Optional<OAuthProvider> google = RequestUtils.getOAuthProvider("google");

        // then
        assertThat(twitter, not(nullValue()));
        assertThat(twitter.isPresent(), equalTo(true));
        assertThat(twitter.get().name(), equalTo("TWITTER"));
        assertThat(facebook, not(nullValue()));
        assertThat(facebook.isPresent(), equalTo(true));
        assertThat(facebook.get().name(), equalTo("FACEBOOK"));
        assertThat(google, not(nullValue()));
        assertThat(google.isPresent(), equalTo(true));
        assertThat(google.get().name(), equalTo("GOOGLE"));
    }
    
    @Test
    public void testGetOAuthProviderConcurrent() {
        MatcherAssert.assertThat(t -> {
            // when
            Optional<OAuthProvider> twitter = RequestUtils.getOAuthProvider("twitter");
            Optional<OAuthProvider> facebook = RequestUtils.getOAuthProvider("facebook");
            Optional<OAuthProvider> google = RequestUtils.getOAuthProvider("google");

            return twitter.get().name().equals("TWITTER") && facebook.get().name().equals("FACEBOOK") && google.get().name().equals("GOOGLE");
        }, new RunsInThreads<>(new AtomicInteger(), TestSuite.THREADS));
    }
}