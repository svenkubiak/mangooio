package io.mangoo.controllers;

import com.google.common.net.MediaType;
import io.mangoo.TestExtension;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class ExceptionHandlerTest {

    @Test
    void testPostInternalServerError() {
        //given
        TestResponse response = TestRequest.post("/error").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.INTERNAL_SERVER_ERROR));
        assertThat(response.getContentType(), equalTo("text/html"));
        assertThat(response.getContent(), containsString("The server encountered something it didn't expect and was unable to complete the request."));
        assertThat(response.getHeader("X-Frame-Options"), equalTo("DENY"));
        assertThat(response.getHeader("X-Content-Type-Options"), equalTo("nosniff"));
    }

    @Test
    void testPostIllegalArgumentIsBadRequest() {
        //given
        TestResponse response = TestRequest.post("/illegal-argument").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.BAD_REQUEST));
        assertThat(response.getHeader("X-Frame-Options"), equalTo("DENY"));
    }

    @Test
    void testPostJsonInternalServerError() {
        //given
        TestResponse response = TestRequest.post("/error")
                .withContentType(MediaType.JSON_UTF_8.withoutParameters().toString())
                .withStringBody("{}")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.INTERNAL_SERVER_ERROR));
        assertThat(response.getContentType(), equalTo("application/json"));
        assertThat(response.getContent(), equalTo("{\"error\":\"Internal Server Error\"}"));
    }
}
