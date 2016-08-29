package test.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;

import org.junit.Test;

public class ApplicationControllerTest {

    @Test
    public void testIndexPage() {
        //given
        WebResponse response = WebRequest.get("/").execute();

        //then
        assertThat(response.getContent(), containsString("Hello World!"));
    }
}
