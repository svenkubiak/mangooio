package io.mangoo.controllers.api;

import io.mangoo.enums.ContentType;
import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;
import io.undertow.util.StatusCodes;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * Created by gowthaman on 12/3/17.
 */
public class ItemControllerTest {


    @Test
    public void testSubPackageGet() {
        //given
        WebResponse response = WebRequest.get("/api/item").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
    }

    @Test
    public void testSubPackagePost() {
        //given
        WebResponse response = WebRequest.post("/api/item")
                .withRequestBody("{\"name\":\"One\"}")
                .withContentType(ContentType.APPLICATION_JSON)
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo(""));
    }


    @Test
    public void testSubPackagePut() {
        //given
        WebResponse response = WebRequest
                .put("/api/item?name=One&status=false")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo(""));
    }

    @Test
    public void testSubPackageDelete() {
        //given
        WebResponse response = WebRequest.delete("/api/item?name=One").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo(""));
    }

}
