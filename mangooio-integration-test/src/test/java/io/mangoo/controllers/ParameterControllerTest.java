package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.test.MangooRequest;
import io.mangoo.test.MangooResponse;
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
        MangooResponse response = MangooRequest.get("/string/bar").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }
    
    @Test
    public void testStringParameterWithSpecialCharacters() {
        //given
        MangooResponse response = MangooRequest.get("/string/tüsätö-$ß_").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("tüsätö-$ß_"));
    }

    @Test
    public void testDoublePrimitiveParamter() {
        //given
        MangooResponse response = MangooRequest.get("/doublePrimitive/1.42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.42"));
    }

    @Test
    public void testDoubleParameter() {
        //given
        MangooResponse response = MangooRequest.get("/double/1.42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.42"));
    }

    @Test
    public void testIntParameter() {
        //given
        MangooResponse response = MangooRequest.get("/int/42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("42"));
    }

    @Test
    public void testIntegerParameter() {
        //given
        MangooResponse response = MangooRequest.get("/integer/42").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("42"));
    }
    
    @Test
    public void testFloatPrimitiveParameter() {
        //given
        MangooResponse response = MangooRequest.get("/floatPrimitive/1.24").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.24"));
    }

    @Test
    public void testFloatParameter() {
        //given
        MangooResponse response = MangooRequest.get("/float/1.24").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("1.24"));
    }

    @Test
    public void testPrimitiveLongParameter() {
        //given
        MangooResponse response = MangooRequest.get("/longPrimitive/6000").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("6000"));
    }
    
    @Test
    public void testLongParameter() {
        //given
        MangooResponse response = MangooRequest.get("/long/60000").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("60000"));
    }

    @Test
    public void testMultipleParameter() {
        //given
        MangooResponse response = MangooRequest.get("/multiple/bar/1").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar:1"));
    }

    @Test
    public void testPathParameter() {
        //given
        MangooResponse response = MangooRequest.get("/path?foo=bar").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }
    
    @Test
    public void testLocalDateParameter() {
        //given
        MangooResponse response = MangooRequest.get("/localdate/2007-12-03").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("2007-12-03"));
    }

    @Test
    public void testLocalDateTimeParameter() {
        //given
        MangooResponse response = MangooRequest.get("/localdatetime/2007-12-03T10:15:30").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("2007-12-03T10:15:30"));
    }
}