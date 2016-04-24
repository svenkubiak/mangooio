package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.utils.http.HTTPRequest;
import io.mangoo.utils.http.HTTPResponse;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class FilterControllerTest {

    @Test
    public void testFilterWithAdditionalContent() {
        //given
        HTTPResponse response = HTTPRequest.get("/filter").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }

    @Test
    public void testFilterWithAdditionalHeader() {
        //given
        HTTPResponse response = HTTPRequest.get("/headerfilter").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getHeader(Headers.CONTENT_MD5_STRING), equalTo("12"));
    }
}