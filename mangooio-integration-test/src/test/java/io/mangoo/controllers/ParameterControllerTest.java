package io.mangoo.controllers;

import io.mangoo.TestExtension;
import io.mangoo.test.concurrent.ConcurrentRunner;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class ParameterControllerTest {
    private static final String TEXT_PLAIN = "text/plain; charset=UTF-8";

    @Test
    void testStringParameter() {
        //given
        TestResponse response = TestRequest.get("/string/bar").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }
    
    @Test
    void testStringParameterConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid = UUID.randomUUID().toString();
            
            //when
            TestResponse response = TestRequest.get("/string/" + uuid).execute();
            
            //then
            return response != null && response.getStatusCode() == StatusCodes.OK && response.getContent().equals(uuid);
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testOptionalRequestParameter() {
        //given
        TestResponse response = TestRequest.get("/optional/bar").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("Optional[bar]"));
    }
    
    @Test
    void testOptionalRequestParameterConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid = UUID.randomUUID().toString();
            
            //when
            TestResponse response = TestRequest.get("/optional/" + uuid).execute();

            //then
            return response != null && response.getStatusCode() == StatusCodes.OK && response.getContent().equals("Optional[" + uuid + "]");
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testOptionalQueryParameter() {
        //given
        TestResponse response = TestRequest.get("/optional/?foo=bar").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("Optional[bar]"));
    }
    
    @Test
    void testOptionalQueryParameterConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid = UUID.randomUUID().toString();
            
            //when
            TestResponse response = TestRequest.get("/optional/?foo=" + uuid).execute();

            //then
            return response != null && response.getStatusCode() == StatusCodes.OK && response.getContent().equals("Optional[" + uuid + "]");
        }, new ConcurrentRunner<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    void testWithoutParameter() {
        //given
        TestResponse response = TestRequest.get("/string").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("isNull"));
    }
    
    @ParameterizedTest(name = "#{index} - Run test with args={0}")
    @ValueSource(strings = {"äöü", "tüsätö-$ß_"})
    void testUmlautParameter(String arg) {
        //given
        TestResponse response = TestRequest.get("/string/" + arg).execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo(arg));
    }

    @Test
    void testDoublePrimitiveParamter() {
        //given
        TestResponse response = TestRequest.get("/doublePrimitive/1.42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.42"));
    }

    @Test
    void testDoubleParameter() {
        //given
        TestResponse response = TestRequest.get("/double/1.42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.42"));
    }

    @Test
    void testIntParameter() {
        //given
        TestResponse response = TestRequest.get("/int/42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("42"));
    }

    @Test
    void testIntegerParameter() {
        //given
        TestResponse response = TestRequest.get("/integer/42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("42"));
    }
    
    @Test
    void testFloatPrimitiveParameter() {
        //given
        TestResponse response = TestRequest.get("/floatPrimitive/1.24").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.24"));
    }

    @Test
    void testFloatParameter() {
        //given
        TestResponse response = TestRequest.get("/float/1.24").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.24"));
    }

    @Test
    void testPrimitiveLongParameter() {
        //given
        TestResponse response = TestRequest.get("/longPrimitive/6000").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("6000"));
    }
    
    @Test
    void testLongParameter() {
        //given
        TestResponse response = TestRequest.get("/long/60000").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("60000"));
    }

    @Test
    void testMultipleParameter() {
        //given
        TestResponse response = TestRequest.get("/multiple/bar/1").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar:1"));
    }

    @Test
    void testPathParameter() {
        //given
        TestResponse response = TestRequest.get("/path?foo=bar").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }
    
    @Test
    void testLocalDateParameter() {
        //given
        TestResponse response = TestRequest.get("/localdate/2007-12-03").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("2007-12-03"));
    }

    @Test
    void testLocalDateTimeParameter() {
        //given
        TestResponse response = TestRequest.get("/localdatetime/2007-12-03T10:15:30").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("2007-12-03T10:15:30"));
    }
}