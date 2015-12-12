package controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import io.mangoo.test.utils.Request;
import io.mangoo.test.utils.Response;
import org.junit.Test;

public class ApplicationControllerTest {

    @Test
    public void testIndexPage() {
        //given
        Response response = Request.get("/").execute();

        //then
        assertThat(response.getContent(), containsString("Hello World!"));
    }
}
