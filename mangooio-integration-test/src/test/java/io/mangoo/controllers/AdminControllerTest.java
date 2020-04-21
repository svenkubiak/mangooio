package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import io.mangoo.TestExtension;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.test.http.TestRequest;
import io.mangoo.test.http.TestResponse;
import io.undertow.util.Methods;
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
    private static final String ROUTES = "routes";
    private static final String TOOLS = "tools";
    private static final String ADMIN = "admin";
    private static final String CONTROL_PANEL = "Dashboard";
    
    @Test
    public void testDashboardUnauthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(CONTROL_PANEL)));
    }
    
    @Test
    public void testDashboardAuthorized() {
        //given
        TestResponse response = login().to("/@admin")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(CONTROL_PANEL));
    }
    
    @Test
    public void testLoggerUnauthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/logger")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(LOGGER)));
    }
    
    @Test
    public void testLoggerAuthorized() {
        //given
        TestResponse response = login().to("/@admin/logger")
                .withHTTPMethod(Methods.GET.toString())
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
        TestResponse response = login().to("/@admin/routes")
                .withHTTPMethod(Methods.GET.toString())
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
        TestResponse response = TestRequest.get("/@admin/routes").withDisabledRedirects().execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(ROUTES)));
    }
    
    @Test
    public void testSchedulerAuthorized() {
        //given
        TestResponse response = login().to("/@admin/scheduler")
                .withHTTPMethod(Methods.GET.toString())
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
        TestResponse response = TestRequest.get("/@admin/scheduler")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    public void testToolsAuthorized() {
        //given
        TestResponse response = login().to("/@admin/tools")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(TOOLS));
    }
    
    @Test
    public void testToolsTwoFactorAuthorized() {
        //given
        TestResponse response = login().to("/@admin/tools")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString("Two Factor Authentication"));
    }
    
    @Test
    public void testToolsUnauthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/tools")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(TOOLS)));
    }
    
    @Test
    public void testToolsAjaxAuthorized() {
        //given
        TestResponse response = login().to("/@admin/tools/ajax")
                .withHTTPMethod(Methods.POST.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo("application/json; charset=UTF-8"));
    }
    
    @Test
    public void testToolsAjaxUnauthorized() {
        //given
        TestResponse response = TestRequest.post("/@admin/tools/ajax")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    public void testLoggerAjaxAuthorized() {
        //given
        TestResponse response = login().to("/@admin/logger/ajax")
                .withHTTPMethod(Methods.POST.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo("text/plain; charset=UTF-8"));
    }
    
    @Test
    public void testLoggerAjaxUnauthorized() {
        //given
        TestResponse response = TestRequest.post("/@admin/logger/ajax")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    public void testLogin() {
        //given
        TestResponse response = TestRequest.get("/@admin/login").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContent(), containsString("login"));
    }
    
    private TestResponse login() {
        Multimap<String, String> parameters = ArrayListMultimap.create();
        parameters.put("username", Application.getInstance(Config.class).getApplicationAdminUsername());
        parameters.put("password", Application.getInstance(Config.class).getApplicationAdminPassword());
        TestResponse response = TestRequest.post("/@admin/authenticate")
                .withForm(parameters)
                .execute();
        
        return response;
    }
}