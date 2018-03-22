package io.mangoo.helpers.cookies;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import java.time.LocalDateTime;

import org.junit.Test;

import io.mangoo.helpers.cookie.CookieBuilder;
import io.mangoo.utils.DateUtils;
import io.undertow.server.handlers.Cookie;

/**
 * 
 * @author svenkubiak
 *
 */
public class CookieBuilderTest {
	@Test
	public void testBuildCookie() {
        //given
		CookieBuilder builder = CookieBuilder.create();
		LocalDateTime now = LocalDateTime.now();
		
    		//when
		Cookie cookie = builder.discard(false)
			.domain("http://localhost")
			.sameSiteMode("lax")
			.expires(now)
			.httpOnly(true)
			.maxAge(24)
			.name("foo")
			.secure(true)
			.value("bar")
			.build();
		
        //then
        assertThat(cookie, not(nullValue()));
        assertThat(cookie.getDomain(), equalTo("http://localhost"));
        assertThat(cookie.getExpires(), equalTo(DateUtils.localDateTimeToDate(now)));
        assertThat(cookie.isHttpOnly(), equalTo(true));
        assertThat(cookie.getMaxAge(), equalTo(24));
        assertThat(cookie.getName(), equalTo("foo"));
        assertThat(cookie.isSecure(), equalTo(true));
        assertThat(cookie.getValue(), equalTo("bar"));
        assertThat(cookie.getSameSiteMode(), equalTo("Lax"));
	}
}