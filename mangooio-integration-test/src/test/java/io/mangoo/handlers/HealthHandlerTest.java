package io.mangoo.handlers;

import io.mangoo.TestExtension;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
class HealthHandlerTest {

    @Test
    void testLiveness() {
        //given
        TestResponse response = TestRequest.get("/@health/live").execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getHeader("Cache-Control"), equalTo("no-store"));
        assertThat(response.getContent(), emptyString());
    }

    @Test
    void testReadiness() {
        //given
        TestResponse response = TestRequest.get("/@health/ready").execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getHeader("Cache-Control"), equalTo("no-store"));
    }

    @Test
    void testProbeSetsNoCookies() {
        //given
        TestResponse response = TestRequest.get("/@health/ready").execute();

        //then
        assertThat(response.getCookies(), empty());
    }

    @Test
    void testOtherMethodsAreNotAnswered() {
        //given
        TestResponse response = TestRequest.post("/@health/live").execute();

        //then
        assertThat(response.getStatusCode(), not(equalTo(StatusCodes.OK)));
    }

    @Test
    void testOtherPathsAreNotAnswered() {
        //given
        TestResponse response = TestRequest.get("/@health/unknown").execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_FOUND));
    }
}
