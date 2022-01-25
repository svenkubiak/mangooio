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
import io.mangoo.enums.Default;
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
    private static final String EVICTIONS = "Evictions";
    private static final String LOGGER = "logger";
    private static final String SCHEDULER = "scheduler";
    private static final String ROUTES = "routes";
    private static final String TOOLS = "tools";
    private static final String CACHE = "cache";
    private static final String ADMIN = "admin";
    private static final String CONTROL_PANEL = "Dashboard";
    
    @Test
    void testDashboardUnauthorized() {
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
    void testCacheAuthorized() {
        //given
        TestResponse response = login().to("/@admin/cache")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(CACHE));
        assertThat(response.getContent(), containsString(EVICTIONS));
    }
    
    @Test
    void testHealthWithHeader() {
        //given
        TestResponse response = login().to("/@admin/health")
                .withHeader(Default.APPLICATION_ADMIN_HEALTH_HEADER.toString(), Application.getInstance(Config.class).getApplicationAdminHealthToken())
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo("application/json; charset=UTF-8"));
        assertThat(response.getContent(), containsString("cpu"));
    }
    
    @Test
    void testHealthWithoutHeader() {
        //given
        TestResponse response = login().to("/@admin/health")
                .withHTTPMethod(Methods.GET.toString())
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.NOT_FOUND));
    }
    
    @Test
    void testCacheUnauthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/cache")
                .withDisabledRedirects()
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(CONTROL_PANEL)));
    }
    
    @Test
    void testDashboardAuthorized() {
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
    void testLoggerUnauthorized() {
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
    void testLoggerAuthorized() {
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
    void testRoutesAuthorized() {
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
    void testRoutesNotConaintAdmin() {
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
    void testRoutesUnauthorized() {
        //given
        TestResponse response = TestRequest.get("/@admin/routes").withDisabledRedirects().execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.FOUND));
        assertThat(response.getHeader("Location"), equalTo("/@admin/login"));
        assertThat(response.getContent(), not(containsString(ROUTES)));
    }
    
    @Test
    void testToolsAuthorized() {
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
    void testToolsTwoFactorAuthorized() {
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
    void testToolsUnauthorized() {
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
    void testToolsAjaxAuthorized() {
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
    void testToolsAjaxUnauthorized() {
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
    void testLoggerAjaxAuthorized() {
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
    void testLoggerAjaxUnauthorized() {
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
    void testLogin() {
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