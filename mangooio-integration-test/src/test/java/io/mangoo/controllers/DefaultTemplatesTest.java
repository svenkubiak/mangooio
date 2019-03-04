package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;

@ExtendWith({TestExtension.class})
public class DefaultTemplatesTest {
    private static final String TEXT_HTML = "text/html; charset=UTF-8";

    @Test
    public void testNotFound() {
        //given
        final TestResponse response = TestRequest.get("/foo").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_FOUND));
        assertThat(response.getContent(), containsString("The server has not found anything matching the Request-URI."));
    }
    
    @Test
    public void testInternalServerError() {
        //given
        final TestResponse response = TestRequest.get("/error").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo("text/html"));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.INTERNAL_SERVER_ERROR));
        assertThat(response.getContent(), containsString("The server encountered an unexpected condition which prevented it from fulfilling the request."));
    }
}