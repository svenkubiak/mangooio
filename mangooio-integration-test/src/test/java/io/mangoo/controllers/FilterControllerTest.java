package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.test.http.Request;
import io.mangoo.test.http.Response;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class FilterControllerTest {

    @Test
    public void testFilterWithAdditionalContent() {
        //given
        Response response = Request.get("/filter").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }

    @Test
    public void testFilterWithAdditionalHeader() {
        //given
        Response response = Request.get("/headerfilter").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getHeader(Headers.CONTENT_MD5_STRING), equalTo("12"));
    }
    
    @Test
    public void testMultipleFilters() {
        //given
        Response response = Request.get("/filters").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("filteronefiltertwofilterthree"));
    }
}