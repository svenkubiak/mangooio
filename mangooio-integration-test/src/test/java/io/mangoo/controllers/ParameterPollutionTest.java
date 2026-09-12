package io.mangoo.controllers;

import io.mangoo.TestExtension;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

@ExtendWith({TestExtension.class})
class ParameterPollutionTest {

    @Test
    void testSingleQueryParameterIsAccepted() {
        //given
        final TestResponse response = TestRequest.get("/?lang=de").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }

    @Test
    void testDuplicateQueryParameterIsRejectedWithBadRequest() {
        //given
        final TestResponse response = TestRequest.get("/?lang=de&lang=en").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.BAD_REQUEST));
    }

    @Test
    void testQueryParameterCanNotOverrideRouteParameter() {
        //given
        final TestResponse response = TestRequest.get("/string/routevalue?foo=spoofed").execute();

        //then the route value must win, a client must never be able to forge it
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("routevalue"));
    }

    @Test
    void testQueryParameterCanNotOverrideMultipleRouteParameters() {
        //given
        final TestResponse response = TestRequest.get("/multiple/bar/1?foo=spoofed&bar=99").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar:1"));
    }

    @Test
    void testUnrelatedQueryParameterIsKept() {
        //given
        final TestResponse response = TestRequest.get("/string/routevalue?other=x").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("routevalue"));
    }
}
