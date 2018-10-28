package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author sven.kubiak
 *
 */
@ExtendWith({TestExtension.class})
public class HttpMethodsTest {
    
    @Test
    public void testGet() {
        //given
        final TestResponse response = TestRequest.get("/").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
    
    @Test
    public void testPost() {
        //given
        final TestResponse response = TestRequest.post("/").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
    
    @Test
    public void testPut() {
        //given
        final TestResponse response = TestRequest.put("/put").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }

    @Test
    public void testHead() {
        //given
        final TestResponse response = TestRequest.head("/").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
    
    @Test
    public void testDelete() {
        //given
        final TestResponse response = TestRequest.delete("/").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
    
    @Test
    public void testOptions() {
        //given
        final TestResponse response = TestRequest.options("/").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
    
    @Test
    public void testPatch() {
        //given
        final TestResponse response = TestRequest.patch("/").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }
}