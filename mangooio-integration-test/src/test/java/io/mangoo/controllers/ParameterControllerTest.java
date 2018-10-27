package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.llorllale.cactoos.matchers.RunsInThreads;

import io.mangoo.TestExtension;
import io.mangoo.test.http.Request;
import io.mangoo.test.http.Response;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class ParameterControllerTest {
    private static final String TEXT_PLAIN = "text/plain; charset=UTF-8";

    @Test
    public void testStringParameter() {
        //given
        Response response = Request.get("/string/bar").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }
    
    @Test
    public void testStringParameterConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid = UUID.randomUUID().toString();
            
            //when
            Response response = Request.get("/string/" + uuid).execute();
            
            //then
            return response != null && response.getStatusCode() == StatusCodes.OK && response.getContent().equals(uuid);
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    public void testOptionalRequestParameter() {
        //given
        Response response = Request.get("/optional/bar").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("Optional[bar]"));
    }
    
    @Test
    public void testOptionalRequestParameterConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid = UUID.randomUUID().toString();
            
            //when
            Response response = Request.get("/optional/" + uuid).execute();

            //then
            return response != null && response.getStatusCode() == StatusCodes.OK && response.getContent().equals("Optional[" + uuid + "]");
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    public void testOptionalQueryParameter() {
        //given
        Response response = Request.get("/optional/?foo=bar").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("Optional[bar]"));
    }
    
    @Test
    public void testOptionalQueryParameterConcurrent() {
        MatcherAssert.assertThat(t -> {
            //given
            String uuid = UUID.randomUUID().toString();
            
            //when
            Response response = Request.get("/optional/?foo=" + uuid).execute();

            //then
            return response != null && response.getStatusCode() == StatusCodes.OK && response.getContent().equals("Optional[" + uuid + "]");
        }, new RunsInThreads<>(new AtomicInteger(), TestExtension.THREADS));
    }
    
    @Test
    public void testWithoutParameter() {
        //given
        Response response = Request.get("/string").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("isNull"));
    }
    
    @Test
    public void testUmlautParameter() {
        //given
        Response response = Request.get("/string/äöü").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("äöü"));
    }
    
    @Test
    public void testStringParameterWithSpecialCharacters() {
        //given
        Response response = Request.get("/string/tüsätö-$ß_").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("tüsätö-$ß_"));
    }

    @Test
    public void testDoublePrimitiveParamter() {
        //given
        Response response = Request.get("/doublePrimitive/1.42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.42"));
    }

    @Test
    public void testDoubleParameter() {
        //given
        Response response = Request.get("/double/1.42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.42"));
    }

    @Test
    public void testIntParameter() {
        //given
        Response response = Request.get("/int/42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("42"));
    }

    @Test
    public void testIntegerParameter() {
        //given
        Response response = Request.get("/integer/42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("42"));
    }
    
    @Test
    public void testFloatPrimitiveParameter() {
        //given
        Response response = Request.get("/floatPrimitive/1.24").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.24"));
    }

    @Test
    public void testFloatParameter() {
        //given
        Response response = Request.get("/float/1.24").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.24"));
    }

    @Test
    public void testPrimitiveLongParameter() {
        //given
        Response response = Request.get("/longPrimitive/6000").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("6000"));
    }
    
    @Test
    public void testLongParameter() {
        //given
        Response response = Request.get("/long/60000").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("60000"));
    }

    @Test
    public void testMultipleParameter() {
        //given
        Response response = Request.get("/multiple/bar/1").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar:1"));
    }

    @Test
    public void testPathParameter() {
        //given
        Response response = Request.get("/path?foo=bar").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }
    
    @Test
    public void testLocalDateParameter() {
        //given
        Response response = Request.get("/localdate/2007-12-03").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("2007-12-03"));
    }

    @Test
    public void testLocalDateTimeParameter() {
        //given
        Response response = Request.get("/localdatetime/2007-12-03T10:15:30").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("2007-12-03T10:15:30"));
    }
}