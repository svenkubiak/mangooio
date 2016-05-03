package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.utils.http.WebRequest;
import io.mangoo.utils.http.WebResponse;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class AdminControllerTest {
    private static final String TEXT_HTML = "text/html; charset=UTF-8";
    private static final String TEXT_PLAIN = "text/plain; charset=UTF-8";
    private static final String SCHEDULER = "scheduler";
    private static final String METRICS = "metrics";
    private static final String CACHE = "cache";
    private static final String ROUTES = "routes";
    private static final String CONFIG = "config";
    private static final String ADMIN = "admin";
    private static final String PROPERTIES = "properties";
    
    @Test
    public void testDashboardAuthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(PROPERTIES));
    }
    
    @Test
    public void testDashboardUnAuthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(PROPERTIES)));
    }
    
    @Test
    public void testConfigAuthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/configuration")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(CONFIG));
    }
    
    @Test
    public void testConfigUnauthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/configuration").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(CONFIG)));
    }

    @Test
    public void testRoutedAuthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/routes")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(ROUTES));
    }
    
    @Test
    public void testRoutedUnauthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/routes").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(ROUTES)));
    }

    @Test
    public void testCacheAuthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/cache")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(CACHE));
    }
    
    @Test
    public void testCacheUnauthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/cache").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(CACHE)));
    }

    @Test
    public void testMetricsAuthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/metrics")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(METRICS));
    }
    
    @Test
    public void testMetricsUnauthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/metrics").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(METRICS)));
    }
    
    @Test
    public void testSchedulerAuthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/scheduler")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(SCHEDULER));
    }
    
    @Test
    public void testSchedulerUnauthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/scheduler").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
}