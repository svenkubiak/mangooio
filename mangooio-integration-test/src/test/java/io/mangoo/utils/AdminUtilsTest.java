package io.mangoo.utils;

import io.mangoo.TestExtension;
import io.mangoo.routing.bindings.Form;
import io.undertow.server.handlers.Cookie;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@ExtendWith({TestExtension.class})
public class AdminUtilsTest {
    @Test
    void testGetAdminCookie() {
        //given
        boolean twofactor = false;

        //when
        Cookie cookie = AdminUtils.getAdminCookie(twofactor);

        //then
        assertThat(cookie, not(nullValue()));
        assertThat(cookie.getValue(), not(nullValue()));
        assertThat(cookie.getSameSiteMode(), equalTo("Strict"));
        assertThat(cookie.getPath(), equalTo("/"));
        assertThat(cookie.getPath(), equalTo("/"));
        assertThat(cookie.isHttpOnly(), equalTo(true));
        assertThat(cookie.isSameSite(), equalTo(true));
        assertThat(cookie.isDiscard(), equalTo(false));
    }

    @Test
    void testIsValidAuthentication() {
        //given
        Form form = new Form();
        form.addValue("username", "admin");
        form.addValue("password", "admin");

        //when
        boolean auth = AdminUtils.isValidAuthentication(form);

        //then
        assertThat(auth, equalTo(true));
    }
}
