package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;
import io.undertow.util.StatusCodes;

public class BasicAuthenticationControllerTest {

    @Test
    public void testBasicAuthenticationFail() {
        //given
        final WebResponse response = WebRequest.get("/basicauth").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
    }
    
    @Test
    public void testBasicAuthenticationSuccess() {
        //given
        final WebResponse response = WebRequest.get("/basicauth").withBasicauthentication("foo", "bar").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContent(), equalTo("authenticated"));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
}