package io.mangoo.routing.routes;

import io.mangoo.TestExtension;
import io.mangoo.enums.Http;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith({TestExtension.class})
class RequestRouteTest {

    @Test
    void testTo() {
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
    
    @Test
    void testAuthentication() {
        //given
        RequestRoute requestRoute = new RequestRoute(Http.GET);
        
        //when
        requestRoute.withAuthentication();
        
        //then
        assertThat(requestRoute.hasAuthentication(), equalTo(true));
    }

    @Test
    void testNonBlocking() {
        //given
        RequestRoute requestRoute = new RequestRoute(Http.GET);
        
        //when
        requestRoute.withNonBlocking();
        
        //then
        assertThat(requestRoute.isBlocking(), equalTo(true));
    }
}