package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.utils.http.WebRequest;
import io.mangoo.utils.http.WebResponse;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class FlashControllerTest {
    @Test
    public void testFlash() {
        //given
        WebResponse response = WebRequest.get("/flash").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("simpleerrorwarningsuccess"));
    }
}