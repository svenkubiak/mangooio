package io.mangoo.routing.routes;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.enums.Http;

@ExtendWith({TestExtension.class})
public class RequestRouteTest {

    @Test
    public void testTo() {
        //given
        RequestRoute requestRoute = new RequestRoute(Http.GET);
        
        //when
        requestRoute.to("foo");
        
        //then
        assertThat(requestRoute.getUrl(), equalTo("/foo"));
        assertThat(requestRoute.getMethod(), equalTo(Http.GET));
        
        //when
        requestRoute.to("/bar");
        
        //then
        assertThat(requestRoute.getUrl(), equalTo("/bar"));
    }
}