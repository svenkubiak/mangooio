package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import io.mangoo.enums.RouteType;

/**
 * 
 * @author svenkubiak
 *
 */
public class BootstrapUtilsTest {

    @Test
    public void testGetRouteType() {
        //given
        String get      = "get";
        String post     = "post";
        String put      = "put";
        String delete   = "delete";
        String head     = "head";
        String wss      = "wss";
        String sse      = "sse";
        String file     = "file";
        String path     = "path";
        
        //then
        assertThat(BootstrapUtils.getRouteType(get), equalTo(RouteType.REQUEST));
        assertThat(BootstrapUtils.getRouteType(post), equalTo(RouteType.REQUEST));
        assertThat(BootstrapUtils.getRouteType(put), equalTo(RouteType.REQUEST));
        assertThat(BootstrapUtils.getRouteType(delete), equalTo(RouteType.REQUEST));
        assertThat(BootstrapUtils.getRouteType(head), equalTo(RouteType.REQUEST));
        assertThat(BootstrapUtils.getRouteType(wss), equalTo(RouteType.WEBSOCKET));
        assertThat(BootstrapUtils.getRouteType(sse), equalTo(RouteType.SERVER_SENT_EVENT));
        assertThat(BootstrapUtils.getRouteType(file), equalTo(RouteType.RESOURCE_FILE));
        assertThat(BootstrapUtils.getRouteType(path), equalTo(RouteType.RESOURCE_PATH));
    }
    
    @Test
    public void testHasBlocking() {
        //given
        String urlWithBlocking = "- GET: / -> ApplicationController.index @blocking";
        String urlWithNonBlocking = "- GET: /text -> ApplicationController.text";
        
        //then
        assertThat(BootstrapUtils.hasBlocking(urlWithBlocking), equalTo(true));
        assertThat(BootstrapUtils.hasBlocking(urlWithNonBlocking), equalTo(false));
    }
    
    @Test
    public void testHasAuthentication() {
        //given
        String urlWithAuthentication = "- WSS: /websocket -> WebSocketController @authentication";
        String urlWithNonAuthentication = "- WSS: /websocketauth -> WebSocketController";
        
        //then
        assertThat(BootstrapUtils.hasAuthentication(urlWithAuthentication), equalTo(true));
        assertThat(BootstrapUtils.hasAuthentication(urlWithNonAuthentication), equalTo(false));
    }    
}