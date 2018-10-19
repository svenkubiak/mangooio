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
    
    @Test
    public void testAuthorization() {
        //given
        RequestRoute requestRoute = new RequestRoute(Http.GET);
        
        //when
        requestRoute.withAuthorization();
        
        //then
        assertThat(requestRoute.hasAuthentication(), equalTo(true));
        assertThat(requestRoute.hasAuthorization(), equalTo(true));
    }
    
    @Test
    public void testAuthentication() {
        //given
        RequestRoute requestRoute = new RequestRoute(Http.GET);
        
        //when
        requestRoute.withAuthentication();
        
        //then
        assertThat(requestRoute.hasAuthentication(), equalTo(true));
        assertThat(requestRoute.hasAuthorization(), equalTo(false));
    }
    
    @Test
    public void testBasicAuthentication() {
        //given
        RequestRoute requestRoute = new RequestRoute(Http.GET);
        
        //when
        requestRoute.withBasicAuthentication("foo", "bar");
        
        //then
        assertThat(requestRoute.hasBasicAuthentication(), equalTo(true));
    }
    
    @Test
    public void testNonBlocking() {
        //given
        RequestRoute requestRoute = new RequestRoute(Http.GET);
        
        //when
        requestRoute.withNonBlocking();
        
        //then
        assertThat(requestRoute.isBlocking(), equalTo(true));
    }
    
    @Test
    public void testWithLimit() {
        //given
        RequestRoute requestRoute = new RequestRoute(Http.GET);
        requestRoute.to("/foo");
        
        //when
        requestRoute.withRequestLimit(42);
        
        //then
        assertThat(requestRoute.getLimit(), equalTo(42));
    }
}