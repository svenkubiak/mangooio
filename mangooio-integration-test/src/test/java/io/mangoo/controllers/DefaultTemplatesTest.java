package io.mangoo.controllers;

import io.mangoo.TestExtension;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class DefaultTemplatesTest {
    private static final String TEXT_HTML = "text/html; charset=UTF-8";

    @Test
    void testNotFound() {
        //given
        final TestResponse response = TestRequest.get("/foo").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_FOUND));
        assertThat(response.getContent(), containsString("The server has not found anything matching the Request-URI."));
    }

    @Test
    void testInternalServerError() {
        //given
        final TestResponse response = TestRequest.get("/error").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getContentType(), equalTo("text/html"));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.INTERNAL_SERVER_ERROR));
        assertThat(response.getContent(), containsString("The server encountered an unexpected condition which prevented it from fulfilling the request."));
    }
}