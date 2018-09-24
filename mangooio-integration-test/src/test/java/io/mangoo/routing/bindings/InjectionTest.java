package io.mangoo.routing.bindings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.mangoo.TestExtension;
import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.core.Shutdown;
import io.mangoo.crypto.Crypto;
import io.mangoo.i18n.Messages;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.interfaces.MangooRequestFilter;
import io.mangoo.models.Metrics;
import io.mangoo.providers.CacheProvider;
import io.mangoo.routing.Response;
import io.mangoo.routing.handlers.ExceptionHandler;
import io.mangoo.routing.handlers.FallbackHandler;
import io.mangoo.routing.handlers.InboundCookiesHandler;
import io.mangoo.routing.handlers.LimitHandler;
import io.mangoo.routing.handlers.LocaleHandler;
import io.mangoo.routing.handlers.OutboundCookiesHandler;
import io.mangoo.routing.handlers.ResponseHandler;
import io.mangoo.routing.listeners.MetricsListener;
import io.mangoo.routing.listeners.ServerSentEventCloseListener;
import io.mangoo.routing.listeners.WebSocketCloseListener;
import io.mangoo.scheduler.Scheduler;
import io.mangoo.scheduler.SchedulerFactory;
import io.mangoo.services.ConcurrentService;
import io.mangoo.services.ServerSentEventService;
import io.mangoo.services.WebSocketService;
import io.mangoo.templating.TemplateEngine;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
public class InjectionTest {

    @Test
    public void testForm() {
        //given
        Form form = Application.getInstance(Form.class);
        
        //then
        assertThat(form, not(nullValue()));
    }
    
    @Test
    public void testAuthentication() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        
        //then
        assertThat(authentication, not(nullValue()));
    }
    
    @Test
    public void testSession() {
        //given
        Session session = Application.getInstance(Session.class);
        
        //then
        assertThat(session, not(nullValue()));
    }
    
    @Test
    public void testFlash() {
        //given
        Flash flash = Application.getInstance(Flash.class);
        
        //then
        assertThat(flash, not(nullValue()));
    }
    
    @Test
    public void testCache() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        
        //then
        assertThat(cache, not(nullValue()));
    }
    
    @Test
    public void testCrypto() {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);
        
        //then
        assertThat(crypto, not(nullValue()));
    }
    
    @Test
    public void testConfig() {
        //given
        Config config = Application.getInstance(Config.class);
        
        //then
        assertThat(config, not(nullValue()));
    }
    
    @Test
    public void testMessages() {
        //given
        Messages messages = Application.getInstance(Messages.class);
        
        //then
        assertThat(messages, not(nullValue()));
    }
    
    @Test
    public void testResponse() {
        //given
        Response response  = Application.getInstance(Response.class);
        
        //then
        assertThat(response, not(nullValue()));
    }
    
    @Test
    public void testRequest() {
        //given
        Request request = Application.getInstance(Request.class);
        
        //then
        assertThat(request, not(nullValue()));
    }
    
    @Test
    public void testScheduler() {
        //given
        Scheduler scheduler = Application.getInstance(Scheduler.class);
        
        //then
        assertThat(scheduler, not(nullValue()));
    }
    
    @Test
    public void testExecutionManager() {
        //given
        ConcurrentService concurrentService = Application.getInstance(ConcurrentService.class);
        
        //then
        assertThat(concurrentService, not(nullValue()));
    }
    
    @Test
    public void testWebSocketManager() {
        //given
        WebSocketService webSocketService = Application.getInstance(WebSocketService.class);
        
        //then
        assertThat(webSocketService, not(nullValue()));
    }
    
    @Test
    public void testServerEventManager() {
        //given
        ServerSentEventService serverSentEventService = Application.getInstance(ServerSentEventService.class);
        
        //then
        assertThat(serverSentEventService, not(nullValue()));
    }
    
    @Test
    public void testMetricsListener() {
        //given
        MetricsListener metricsListener = Application.getInstance(MetricsListener.class);
        
        //then
        assertThat(metricsListener, not(nullValue()));
    }
    
    @Test
    public void testMetrics() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //then
        assertThat(metrics, not(nullValue()));
    }
    
    @Test
    public void testFallbackHandler() {
        //given
        FallbackHandler fallbackHandler = Application.getInstance(FallbackHandler.class);
        
        //then
        assertThat(fallbackHandler, not(nullValue()));
    }
    
    @Test
    public void testExceptionHandler() {
        //given
        ExceptionHandler exceptionHandler = Application.getInstance(ExceptionHandler.class);
        
        //then
        assertThat(exceptionHandler, not(nullValue()));
    }
    
    @Test
    public void testMangooBootstrap() {
        //given
        MangooBootstrap mangooLifecycle = Application.getInstance(MangooBootstrap.class);
        
        //then
        assertThat(mangooLifecycle, not(nullValue()));
    }
    
    @Test
    public void testCacheProvider() {
        //given
        CacheProvider cacheProvider = Application.getInstance(CacheProvider.class);
        
        //then
        assertThat(cacheProvider, not(nullValue()));
    }
    
    @Test
    public void testTemplateEngine() {
        //given
        TemplateEngine templateEngine = Application.getInstance(TemplateEngine.class);
        
        //then
        assertThat(templateEngine, not(nullValue()));
    }
    
    @Test
    public void testValidator() {
        //given
        Validator validator = Application.getInstance(Validator.class);
        
        //then
        assertThat(validator, not(nullValue()));
    }
    
    @Test
    public void testLimitHandler() {
        //given
        LimitHandler limitHandler = Application.getInstance(LimitHandler.class);
        
        //then
        assertThat(limitHandler, not(nullValue()));
    }
    
    @Test
    public void testLocaleHandler() {
        //given
        LocaleHandler localeHandler = Application.getInstance(LocaleHandler.class);
        
        //then
        assertThat(localeHandler, not(nullValue()));
    }
    
    @Test
    public void testInboundCookiesHandler() {
        //given
        InboundCookiesHandler inboundCookieHandler = Application.getInstance(InboundCookiesHandler.class);
        
        //then
        assertThat(inboundCookieHandler, not(nullValue()));
    }
    
    @Test
    public void testOutboundCookiesHandler() {
        //given
        OutboundCookiesHandler outboundCookiesHandler = Application.getInstance(OutboundCookiesHandler.class);
        
        //then
        assertThat(outboundCookiesHandler, not(nullValue()));
    }
    
    @Test
    public void testResponseHandler() {
        //given
        ResponseHandler responseHandler = Application.getInstance(ResponseHandler.class);
        
        //then
        assertThat(responseHandler, not(nullValue()));
    }
    
    @Test
    public void testMangooRequestFilter() {
        //given
        MangooRequestFilter mangooRequestFilter = Application.getInstance(MangooRequestFilter.class);
        
        //then
        assertThat(mangooRequestFilter, not(nullValue()));
    }
    
    @Test
    public void testServerSentEventCloseListener() {
        //given
        ServerSentEventCloseListener serverSentEventCloseListener = Application.getInstance(ServerSentEventCloseListener.class);
        
        //then
        assertThat(serverSentEventCloseListener, not(nullValue()));
    } 
    
    @Test
    public void testWebSocketCloseListener() {
        //given
        WebSocketCloseListener webSocketCloseListener = Application.getInstance(WebSocketCloseListener.class);
        
        //then
        assertThat(webSocketCloseListener, not(nullValue()));
    }  
    
    @Test
    public void testSchedulerFactory() {
        //given
        SchedulerFactory schedulerFactory = Application.getInstance(SchedulerFactory.class);
        
        //then
        assertThat(schedulerFactory, not(nullValue()));
    } 
    
    @Test
    public void testShutdown() {
        //given
        Shutdown shutdown = Application.getInstance(Shutdown.class);
        
        //then
        assertThat(shutdown, not(nullValue()));
    }     
}