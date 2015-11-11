package io.mangoo.routing;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.startsWith;

import org.junit.Test;

import io.mangoo.enums.RouteType;
import io.undertow.util.Methods;

/**
 * 
 * @author svenkubiak
 *
 */
public class RouteTest {
    
    @Test
    public void testAddResourcePath() {
        //given
        int oldSize = Router.getRoutes().size();
        Route route = new Route(RouteType.RESOURCE_PATH).toUrl("woohoo");
        
        //then
        assertThat(route.getUrl(), startsWith("/"));
        assertThat(route.getUrl(), endsWith("/"));
        assertThat(Router.getRoutes().size(), equalTo(oldSize + 1));
    }
    
    @Test
    public void testAddNonResourcePath() {
        //given
        int oldSize = Router.getRoutes().size();
        Route route = new Route(RouteType.REQUEST).toUrl("wooop");
        
        //then
        assertThat(route.getUrl(), startsWith("/"));
        assertThat(Router.getRoutes().size(), equalTo(oldSize));
    }
    
    @Test
    public void testAllowBlocking() {
        //given
        Route route = new Route(RouteType.REQUEST);
        
        //when
        route.allowBlocking();
        
        //then
        assertThat(route.isBlocking(), equalTo(true));
    }
    
    @Test
    public void testNotAllowBlocking() {
        //given
        Route route = new Route(RouteType.REQUEST);
        
        //then
        assertThat(route.isBlocking(), equalTo(false));
    }
    
    @Test
    public void testMapRequest() {
        //given
        Route route = Router.mapRequest(Methods.GET).toUrl("/woobooop");
        
        //then
        assertThat(route.getRequestMethod(), equalTo(Methods.GET));
        assertThat(route.getUrl(), equalTo("/woobooop"));
        assertThat(route.getRouteType(), equalTo(RouteType.REQUEST));
    }
    
    @Test
    public void testMapWebSocket() {
        //given
        Route route = Router.mapWebSocket();
        
        //then
        assertThat(route.getRouteType(), equalTo(RouteType.WEBSOCKET));
    }
    
    @Test
    public void testMapServerSentEvent() {
        //given
        Route route = Router.mapServerSentEvent();
        
        //then
        assertThat(route.getRouteType(), equalTo(RouteType.SERVER_SENT_EVENT));
    }
    
    @Test
    public void testResourceFile() {
        //given
        Route route = Router.mapResourceFile();
        
        //then
        assertThat(route.getRouteType(), equalTo(RouteType.RESOURCE_FILE));
    }
    
    @Test
    public void testResourcePath() {
        //given
        Route route = Router.mapResourcePath();
        
        //then
        assertThat(route.getRouteType(), equalTo(RouteType.RESOURCE_PATH));
    }
}