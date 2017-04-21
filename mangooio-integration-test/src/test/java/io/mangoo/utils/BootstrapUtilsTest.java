package io.mangoo.utils;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

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
        String patch    = "patch";
        String options   = "options";
        String post     = "post";
        String put      = "put";
        String delete   = "delete";
        String head     = "head";
        String wss      = "wss";
        String sse      = "sse";
        String file     = "file";
        String path     = "path";
        
        //then
        assertThat(BootstrapUtils.getRouteType(options), equalTo(RouteType.REQUEST));
        assertThat(BootstrapUtils.getRouteType(patch), equalTo(RouteType.REQUEST));
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
    public void testGetPackageName() {
        //given
        String package1 = "io.mangoo";
        String package2 = "io.mangoo.";
        
        //then
        assertThat(BootstrapUtils.getPackageName(package1), equalTo(package2));
        assertThat(BootstrapUtils.getPackageName(package2), equalTo(package2));
    } 
    
    @Test
    public void testGetBaseDirectory() {
        //then
        assertThat(BootstrapUtils.getBaseDirectory(), not(nullValue()));
    }
    
    @Test
    public void testGetVersion() throws InterruptedException {
        //then
        assertThat(BootstrapUtils.getVersion(), not(nullValue()));
    } 
    
    @Test
    public void testGetLogo() throws InterruptedException {
        //then
        assertThat(BootstrapUtils.getLogo(), not(nullValue()));
    }
}