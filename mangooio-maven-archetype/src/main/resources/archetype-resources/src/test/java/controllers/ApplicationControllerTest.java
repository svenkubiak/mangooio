package controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.test.TestRunner;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;

@ExtendWith({TestRunner.class})
public class ApplicationControllerTest {

    @Test
    public void testIndexPage() {
        //given
        TestResponse response = TestRequest.get("/").execute();

        //then
        assertThat(response.getContent(), containsString("Hello World!"));
    }
}