package io.mangoo.controllers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.test.utils.Request;
import io.mangoo.test.utils.Response;
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
    private static final String ALIVE = "alive";
    private static final String ADMIN = "admin";
    private static final String PROPERTIES = "properties";
    private static final String MEMORY = "memory";    

    @Test
    public void testHealthAuthorized() {
        //given
        Response response = Request.get("/@health")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), containsString(ALIVE));
    }
    
    @Test
    public void testHealthUnaothorized() {
        //given
        Response response = Request.get("/@health").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(ALIVE)));
    }

    @Test
    public void testConfigAuthorized() {
        //given
        Response response = Request.get("/@config")
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
        Response response = Request.get("/@config").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(CONFIG)));
    }

    @Test
    public void testRoutedAuthorized() {
        //given
        Response response = Request.get("/@routes")
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
        Response response = Request.get("/@routes").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(ROUTES)));
    }

    @Test
    public void testCacheAuthorized() {
        //given
        Response response = Request.get("/@cache")
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
        Response response = Request.get("/@cache").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(CACHE)));
    }

    @Test
    public void testMetricsAuthorized() {
        //given
        Response response = Request.get("/@metrics")
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
        Response response = Request.get("/@metrics").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(METRICS)));
    }
    
    @Test
    public void testSchedulerAuthorized() {
        //given
        Response response = Request.get("/@scheduler")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(SCHEDULER));
    }
    
    @Test
    public void testSchedulerUnAuthorized() {
        //given
        Response response = Request.get("/@scheduler").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(SCHEDULER)));
    }
    
    @Test
    public void testSystemAuthorized() {
        //given
        Response response = Request.get("/@system")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(PROPERTIES));
    }
    
    @Test
    public void testSystemUnAuthorized() {
        //given
        Response response = Request.get("/@system").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(PROPERTIES)));
    }
    
    @Test
    public void testMemoryAuthorized() {
        //given
        Response response = Request.get("/@memory")
                .withBasicauthentication(ADMIN, ADMIN)
                .execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.OK));
        assertThat(response.getContentType(), equalTo(TEXT_HTML));
        assertThat(response.getContent(), containsString(MEMORY));
    }
    
    @Test
    public void testMemoryUnAuthorized() {
        //given
        Response response = Request.get("/@memory").execute();
        
        //then
        assertThat(response, not(nullValue()));
        assertThat(response.getStatusCode(), equalTo(StatusCodes.UNAUTHORIZED));
        assertThat(response.getContentType(), equalTo(TEXT_PLAIN));
        assertThat(response.getContent(), not(containsString(MEMORY)));
    }
}