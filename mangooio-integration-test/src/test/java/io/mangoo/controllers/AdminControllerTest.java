package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.core.Application;
import io.mangoo.models.Metrics;
import io.mangoo.test.utils.WebRequest;
import io.mangoo.test.utils.WebResponse;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
public class AdminControllerTest {
    private static final String TEXT_HTML = "text/html; charset=UTF-8";
    private static final String TEXT_PLAIN = "text/plain; charset=UTF-8";
    private static final String LOGGER = "logger";
    private static final String SCHEDULER = "scheduler";
    private static final String METRICS = "metrics";
    private static final String ROUTES = "routes";
    private static final String TOOLS = "tools";
    private static final String ADMIN = "admin";
    private static final String CONTROL_PANEL = "Dashboard";
    
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
        assertThat(response.getContent(), containsString(CONTROL_PANEL));
    }
    
    @Test
    public void testLoggerUnAuthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/logger").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(LOGGER)));
    }
    
    @Test
    public void testLoggerAuthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/logger")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(LOGGER));
    }
    
    @Test
    public void testDashboardUnAuthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(CONTROL_PANEL)));
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
    public void testResetMetricsAuthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/metrics/reset")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(METRICS));
        
        //given
        WebRequest.get("/").execute();
        WebRequest.get("/").execute();
        WebRequest.get("/").execute();
        Metrics metrics = Application.getInstance(Metrics.class);
        metrics.reset();
        
        //then
        assertThat(metrics.getAvgRequestTime(), equalTo(0L));
        assertThat(metrics.getMaxRequestTime(), equalTo(0));
        assertThat(metrics.getMinRequestTime(), equalTo(0));
        assertThat(metrics.getResponseMetrics().size(), equalTo(0));
        assertThat(metrics.getDataSend(), equalTo(0L));
    }
    
    @Test
    public void testResetMetricsUnauthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/metrics/reset")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(ROUTES)));
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
    
    @Test
    public void testToolsAuthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/tools")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(TOOLS));
    }
    
    @Test
    public void testToolsUnauthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/tools").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(TOOLS)));
    }
    
    @Test
    public void testToolsAjaxAuthorized() {
        //given
        WebResponse response = WebRequest.post("/@admin/tools/ajax")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo("application/json; charset=UTF-8"));
    }
    
    @Test
    public void testToolsAjaxUnauthorized() {
        //given
        WebResponse response = WebRequest.post("/@admin/tools/ajax").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo("text/plain; charset=UTF-8"));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    public void testLoggerAjaxAuthorized() {
        //given
        WebResponse response = WebRequest.post("/@admin/logger/ajax")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo("text/plain; charset=UTF-8"));
    }
    
    @Test
    public void testLoggerAjaxUnauthorized() {
        //given
        WebResponse response = WebRequest.post("/@admin/logger/ajax").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo("text/plain; charset=UTF-8"));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    public void testHealthAuthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/health")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo("application/json; charset=UTF-8"));
        assertThat(response.getContent(), containsString("uptime"));
    }
    
    @Test
    public void testHealthUnauthorized() {
        //given
        WebResponse response = WebRequest.get("/@admin/health").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo("text/plain; charset=UTF-8"));
        assertThat(response.getContent(), not(containsString("uptime")));
    }
}