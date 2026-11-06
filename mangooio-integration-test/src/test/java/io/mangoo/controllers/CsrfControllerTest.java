package io.mangoo.controllers;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import io.mangoo.TestExtension;
import io.mangoo.constants.Const;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
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

@ExtendWith({TestExtension.class})
public class CsrfControllerTest {

    @Test
    public void testValidCsrfForm() {
        //given
    	TestBrowser instance = TestBrowser.open();

    	//when
        TestResponse response = instance.to("/csrf/form")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));

        String token = getCsrf(response.getContent());
        Multimap<String, String> form = ArrayListMultimap.create();
        form.put(Const.CSRF_TOKEN, token);

        //when
        response = instance.to("/csrf/validate")
                .withForm(form)
                .withHTTPMethod(Methods.POST.toString())
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }

    @Test
    public void testValidCsrfToken() {
        //given
        TestBrowser instance = TestBrowser.open();

        //when
        TestResponse response = instance.to("/csrf/token")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        String token = response.getContent();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));

        //when
        response = instance.to("/csrf/validate")
                .withHeader(Const.CSRF_TOKEN, token)
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }

    @Test
    public void testInvalidCsrf() {
        //when
        TestResponse response = TestRequest.get("/csrf/validate").execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
        assertThat(response.getContent(), not(containsString("bar")));
    }

    @Test
    public void testWrongCsrfTokenIsRejected() {
        //given
        TestBrowser instance = TestBrowser.open();
        instance.to("/csrf/token")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //when
        TestResponse response = instance.to("/csrf/validate")
                .withHeader(Const.CSRF_TOKEN, "wrong-token")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
        assertThat(response.getContent(), not(containsString("bar")));
    }

    @Test
    public void testCsrfTokenOfAnotherSessionIsRejected() {
        //given
        String token = TestBrowser.open().to("/csrf/token")
                .withHTTPMethod(Methods.GET.toString())
                .execute()
                .getContent();
        TestBrowser instance = TestBrowser.open();
        instance.to("/csrf/token")
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //when
        TestResponse response = instance.to("/csrf/validate")
                .withHeader(Const.CSRF_TOKEN, token)
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FORBIDDEN));
        assertThat(response.getContent(), not(containsString("bar")));
    }

    @Test
    public void testUnreadableSessionCookieIsReplaced() {
        //given
        String sessionCookieName = Application.getInstance(Config.class).getSessionCookieName();
        HttpCookie stale = new HttpCookie(sessionCookieName, "stale");
        stale.setPath("/");
        TestBrowser instance = TestBrowser.open();

        //when
        TestResponse response = instance.to("/csrf/form")
                .withCookie(stale)
                .withHTTPMethod(Methods.GET.toString())
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));

        // The client keeps the stale cookie next to the issued one, a browser would replace it
        HttpCookie issued = instance.getCookies().stream()
                .filter(cookie -> cookie.getName().equals(sessionCookieName) && !"stale".equals(cookie.getValue()))
                .findFirst()
                .orElse(null);
        assertThat(issued, not(nullValue()));

        Multimap<String, String> form = ArrayListMultimap.create();
        form.put(Const.CSRF_TOKEN, getCsrf(response.getContent()));

        //when
        response = TestBrowser.open().to("/csrf/validate")
                .withCookie(issued)
                .withForm(form)
                .withHTTPMethod(Methods.POST.toString())
                .execute();

        //then
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), equalTo("bar"));
    }

    private String getCsrf(String form) {
        String search = "value=\"";
        int start = form.indexOf(search);
        if (start != -1) {
            start += search.length();
            int end = form.indexOf("\"", start);
            if (end != -1) {
                return form.substring(start, end);
            }
        }

        return null;
    }
}
