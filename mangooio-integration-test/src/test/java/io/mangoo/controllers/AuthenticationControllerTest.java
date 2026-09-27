package io.mangoo.controllers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import controllers.AuthenticationController;
import io.mangoo.TestExtension;
import io.mangoo.test.http.TestBrowser;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.mangoo.utils.TotpUtils;
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
class AuthenticationControllerTest {
    private static final String USERNAME = "foo";
    private static final String AUTHENTICATION_COOKIE = "test-authentication";
    private static final String SUBJECT = "mysubject";
    private static final String LOCATION = "Location";

    @Test
    void testNotAuthenticated() {
        //given
        TestResponse response = TestRequest.get("/authenticationrequired")
                .withDisabledRedirects()
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getContent(), not(equalTo(USERNAME)));
    }
    
    @Test
    void testSubject() {
        //given
        TestResponse response = TestRequest.get("/subject")
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("not authenticated"));
        
        //given
        TestBrowser instance = TestBrowser.open();
        response = instance.to("/dologin")
                .withHTTPMethod(Methods.POST.toString())
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        
        //given
        instance.to("/subject")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("authenticated"));
    }

    @Test
    void testAuthenticated() {
        //given
        TestBrowser instance = TestBrowser.open();

        //when
        TestResponse response = instance
                .to("/dologin")
                .withDisabledRedirects()
                .withHTTPMethod(Methods.POST.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));

        //when
        response = instance.to("/authenticationrequired")
                .withDisabledRedirects()
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent().length(), equalTo(9));

        //when
        response = instance.to("/logout")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));

        //when
        response = instance.to("/authenticationrequired")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
    }

    @Test
    void testMissingAuthenticationRedirectsToLogin() {
        //when
        TestResponse response = TestRequest.get("/authenticationrequired")
                .withDisabledRedirects()
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader(LOCATION), containsString("/login"));
    }

    /**
     * Regression test for OutboundCookiesHandler: the authentication cookie is written based on
     * hasSubject(), not on isValid(). Switching that call site back to isValid() leaves the second
     * factor step without a cookie and this test fails on the missing cookie.
     */
    @Test
    void testAuthenticationCookieIsWrittenWhileSecondFactorIsPending() {
        //given
        TestBrowser instance = TestBrowser.open();

        //when
        TestResponse response = instance.to("/dologintwofactor")
                .withHTTPMethod(Methods.POST.toString())
                .withDisabledRedirects()
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(instance.getCookie(AUTHENTICATION_COOKIE), not(nullValue()));
        assertThat(instance.getCookie(AUTHENTICATION_COOKIE).getValue(), not(emptyOrNullString()));
    }

    @Test
    void testPendingSecondFactorIsNotValidOnUnboundRoute() {
        //given
        TestBrowser instance = TestBrowser.open();
        instance.to("/dologintwofactor")
                .withHTTPMethod(Methods.POST.toString())
                .withDisabledRedirects()
                .execute();

        //when
        TestResponse response = instance.to("/authenticationstate")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then hasSubject is true, isValid is false, two factor is pending
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("true:false:true"));
    }

    @Test
    void testPendingSecondFactorRedirectsToMfa() {
        //given
        TestBrowser instance = TestBrowser.open();
        instance.to("/dologintwofactor")
                .withHTTPMethod(Methods.POST.toString())
                .withDisabledRedirects()
                .execute();

        //when
        TestResponse response = instance.to("/authenticationrequired")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader(LOCATION), containsString("/twofactor"));
    }

    @Test
    void testFullTwoFactorLoginFlow() {
        //given
        TestBrowser instance = TestBrowser.open();

        //when password step
        TestResponse response = instance.to("/dologintwofactor")
                .withHTTPMethod(Methods.POST.toString())
                .withDisabledRedirects()
                .execute();

        //then a session exists, but it does not grant access yet
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(instance.getCookie(AUTHENTICATION_COOKIE), not(nullValue()));

        //when the second factor is verified
        Multimap<String, String> parameter = ArrayListMultimap.create();
        parameter.put("twofactor", TotpUtils.getTotp(AuthenticationController.SECRET));

        response = instance.to("/factorize")
                .withForm(parameter)
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader(LOCATION), containsString("/authenticationrequired"));

        //when using the refreshed cookie
        HttpCookie cookie = instance.getCookie(AUTHENTICATION_COOKIE);

        //then the authentication is complete
        response = TestRequest.get("/authenticationstate")
                .withCookie(cookie)
                .withDisabledRedirects()
                .execute();

        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("true:true:false"));

        //and the protected route is reachable
        response = TestRequest.get("/authenticationrequired")
                .withCookie(cookie)
                .withDisabledRedirects()
                .execute();

        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo(SUBJECT));
    }
}