package io.mangoo.controllers;

import io.mangoo.TestExtension;
import io.mangoo.test.http.TestBrowser;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.HttpCookie;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class CsrfControllerTest {
	private static final int AUTHENTICITY_LENGTH = 32;

    @Test
    public void testValidCsrf() {
        //given
    	TestBrowser instance = TestBrowser.open();

    	//when
        TestResponse response = instance.to("/csrf")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));

        HttpCookie cookie = response.getCookie("test-session");
System.out.println(cookie);

        //when
        response = instance.to("/valid")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        System.out.println(cookie);
        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }

    @Test
    public void testInvalidCsrf() {
        //when
        TestResponse response = TestRequest.get("/valid").execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
        assertThat(response.getContent(), not(containsString("bar")));
    }
}
