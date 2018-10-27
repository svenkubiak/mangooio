package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.models.Metrics;
import io.mangoo.test.http.Request;
import io.mangoo.test.http.Response;
import io.undertow.util.StatusCodes;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class AdminControllerTest {
    private static final String TEXT_HTML = "text/html; charset=UTF-8";
    private static final String LOGGER = "logger";
    private static final String SCHEDULER = "scheduler";
    private static final String METRICS = "metrics";
    private static final String ROUTES = "routes";
    private static final String TOOLS = "tools";
    private static final String ADMIN = "admin";
    private static final String CONTROL_PANEL = "Dashboard";
    
    @Test
    public void testDashboardUnAuthorized() {
        //given
        Response response = Request.get("/@admin").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(CONTROL_PANEL)));
    }
    
    @Test
    public void testDashboardAuthorized() {
        //given
        Response response = Request.get("/@admin")
                .withBasicAuthentication(ADMIN, ADMIN)
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
        Response response = Request.get("/@admin/logger").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(LOGGER)));
    }
    
    @Test
    public void testLoggerAuthorized() {
        //given
        Response response = Request.get("/@admin/logger")
                .withBasicAuthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(LOGGER));
    }

    @Test
    public void testRoutesAuthorized() {
        //given
        Response response = Request.get("/@admin/routes")
                .withBasicAuthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(ROUTES));
    }
    
    @Test
    public void testRoutesNotConaintAdmin() {
        //given
        Response response = Request.get("/@admin/routes")
                .withBasicAuthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), not(containsString("io.mangoo.admin.AdminController")));
    }
    
    @Test
    public void testRoutesUnauthorized() {
        //given
        Response response = Request.get("/@admin/routes").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(ROUTES)));
    }

    @Test
    public void testMetricsAuthorized() {
        //given
        Response response = Request.get("/@admin/metrics")
                .withBasicAuthentication(ADMIN, ADMIN)
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
        Response response = Request.get("/@admin/metrics/reset")
                .withBasicAuthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(METRICS));
        
        //given
        Request.get("/").execute();
        Request.get("/").execute();
        Request.get("/").execute();
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
        Response response = Request.get("/@admin/metrics/reset")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(ROUTES)));
    }
    
    @Test
    public void testMetricsUnauthorized() {
        //given
        Response response = Request.get("/@admin/metrics").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(METRICS)));
    }
    
    @Test
    public void testSchedulerAuthorized() {
        //given
        Response response = Request.get("/@admin/scheduler")
                .withBasicAuthentication(ADMIN, ADMIN)
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
        Response response = Request.get("/@admin/scheduler").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    public void testToolsAuthorized() {
        //given
        Response response = Request.get("/@admin/tools")
                .withBasicAuthentication(ADMIN, ADMIN)
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
        Response response = Request.get("/@admin/tools").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(TOOLS)));
    }
    
    @Test
    public void testToolsAjaxAuthorized() {
        //given
        Response response = Request.post("/@admin/tools/ajax")
                .withBasicAuthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo("application/json; charset=UTF-8"));
    }
    
    @Test
    public void testToolsAjaxUnauthorized() {
        //given
        Response response = Request.post("/@admin/tools/ajax").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    public void testLoggerAjaxAuthorized() {
        //given
        Response response = Request.post("/@admin/logger/ajax")
                .withBasicAuthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo("text/plain; charset=UTF-8"));
    }
    
    @Test
    public void testLoggerAjaxUnauthorized() {
        //given
        Response response = Request.post("/@admin/logger/ajax").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    public void testHealthAuthorized() {
        //given
        Response response = Request.get("/@admin/health")
                .withBasicAuthentication(ADMIN, ADMIN)
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
        Response response = Request.get("/@admin/health").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString("uptime")));
    }
}