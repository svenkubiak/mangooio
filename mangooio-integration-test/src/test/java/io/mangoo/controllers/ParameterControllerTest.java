package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
public class ParameterControllerTest {
    private static final String TEXT_PLAIN = "text/plain; charset=UTF-8";

    @Test
    public void testStringParameter() {
        //given
        WebResponse response = WebRequest.get("/string/bar").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }
    
    @Test
    public void testStringParameterWithSpecialCharacters() {
        //given
        WebResponse response = WebRequest.get("/string/tüsätö-$ß_").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("tüsätö-$ß_"));
    }

    @Test
    public void testDoublePrimitiveParamter() {
        //given
        WebResponse response = WebRequest.get("/doublePrimitive/1.42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.42"));
    }

    @Test
    public void testDoubleParameter() {
        //given
        WebResponse response = WebRequest.get("/double/1.42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.42"));
    }

    @Test
    public void testIntParameter() {
        //given
        WebResponse response = WebRequest.get("/int/42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("42"));
    }

    @Test
    public void testIntegerParameter() {
        //given
        WebResponse response = WebRequest.get("/integer/42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("42"));
    }
    
    @Test
    public void testFloatPrimitiveParameter() {
        //given
        WebResponse response = WebRequest.get("/floatPrimitive/1.24").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.24"));
    }

    @Test
    public void testFloatParameter() {
        //given
        WebResponse response = WebRequest.get("/float/1.24").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.24"));
    }

    @Test
    public void testPrimitiveLongParameter() {
        //given
        WebResponse response = WebRequest.get("/longPrimitive/6000").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("6000"));
    }
    
    @Test
    public void testLongParameter() {
        //given
        WebResponse response = WebRequest.get("/long/60000").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("60000"));
    }

    @Test
    public void testMultipleParameter() {
        //given
        WebResponse response = WebRequest.get("/multiple/bar/1").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar:1"));
    }

    @Test
    public void testPathParameter() {
        //given
        WebResponse response = WebRequest.get("/path?foo=bar").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }
    
    @Test
    public void testLocalDateParameter() {
        //given
        WebResponse response = WebRequest.get("/localdate/2007-12-03").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("2007-12-03"));
    }

    @Test
    public void testLocalDateTimeParameter() {
        //given
        WebResponse response = WebRequest.get("/localdatetime/2007-12-03T10:15:30").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("2007-12-03T10:15:30"));
    }
}