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
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
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
        TestResponse response = TestRequest.get("/@admin").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(CONTROL_PANEL)));
    }
    
    @Test
    public void testDashboardAuthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin")
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
        TestResponse response = TestRequest.get("/@admin/logger").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(LOGGER)));
    }
    
    @Test
    public void testLoggerAuthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/logger")
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
        TestResponse response = TestRequest.get("/@admin/routes")
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
        TestResponse response = TestRequest.get("/@admin/routes")
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
        TestResponse response = TestRequest.get("/@admin/routes").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(ROUTES)));
    }

    @Test
    public void testMetricsAuthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/metrics")
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
        TestResponse response = TestRequest.get("/@admin/metrics/reset")
                .withBasicAuthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(METRICS));
        
        //given
        TestRequest.get("/").execute();
        TestRequest.get("/").execute();
        TestRequest.get("/").execute();
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
        TestResponse response = TestRequest.get("/@admin/metrics/reset")
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(ROUTES)));
    }
    
    @Test
    public void testMetricsUnauthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/metrics").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(METRICS)));
    }
    
    @Test
    public void testSchedulerAuthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/scheduler")
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
        TestResponse response = TestRequest.get("/@admin/scheduler").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    public void testToolsAuthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/tools")
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
        TestResponse response = TestRequest.get("/@admin/tools").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(TOOLS)));
    }
    
    @Test
    public void testToolsAjaxAuthorized() {
        //given
        TestResponse response = TestRequest.post("/@admin/tools/ajax")
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
        TestResponse response = TestRequest.post("/@admin/tools/ajax").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    public void testLoggerAjaxAuthorized() {
        //given
        TestResponse response = TestRequest.post("/@admin/logger/ajax")
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
        TestResponse response = TestRequest.post("/@admin/logger/ajax").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    public void testHealthAuthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/health")
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
        TestResponse response = TestRequest.get("/@admin/health").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContent(), not(containsString("uptime")));
    }
}