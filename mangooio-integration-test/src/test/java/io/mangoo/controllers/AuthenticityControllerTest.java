package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.startsWith;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.test.http.TestBrowser;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.Methods;
import io.undertow.util.StatusCodes;

/**
 *
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class AuthenticityControllerTest {
	private static final int AUTHENTICITY_LENGTH = 32;

    @Test
    public void testAuthenticityForm() {
        //given
        TestResponse response = TestRequest.get("/authenticityform").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), startsWith("<input type=\"hidden\" value=\""));
        assertThat(response.getContent(), endsWith(" name=\"authenticity\" />"));
    }

    @Test
    public void testAuthenticityToken() {
        //given
        TestResponse response = TestRequest.get("/authenticitytoken").execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent().length(), equalTo(AUTHENTICITY_LENGTH));
    }

    @Test
    public void testValidAuthenticity() {
        //given
    	TestBrowser instance = TestBrowser.open();

    	//when
        TestResponse response = instance.to("/authenticitytoken")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        String token = response.getContent();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent().length(), equalTo(AUTHENTICITY_LENGTH));

        //when
        response = instance.to("/valid?authenticity=" + token)
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }

    @Test
    public void testInvalidAuthenticity() {
        //when
        TestResponse response = TestRequest.get("/invalid?authenticity=fdjsklfjsd82jkfldsjkl").execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
        assertThat(response.getContent(), not(containsString("bar")));
        assertThat(response.getContent(), containsString("Access forbidden"));
    }
}
