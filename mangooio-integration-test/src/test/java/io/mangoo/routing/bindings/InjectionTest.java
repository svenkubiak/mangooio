package io.mangoo.routing.bindings;

import io.mangoo.TestExtension;
import io.mangoo.cache.Cache;
import io.mangoo.cache.CacheProvider;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.core.Shutdown;
import io.mangoo.crypto.Crypto;
import io.mangoo.i18n.Messages;
import io.mangoo.interfaces.MangooBootstrap;
import io.mangoo.interfaces.filters.OncePerRequestFilter;
import io.mangoo.models.Metrics;
import io.mangoo.routing.Response;
import io.mangoo.routing.handlers.*;
import io.mangoo.routing.listeners.MetricsListener;
import io.mangoo.routing.listeners.ServerSentEventCloseListener;
import io.mangoo.templating.TemplateEngine;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

/**
 * 
 * @author svenkubiak
 *
 */
@ExtendWith({TestExtension.class})
class InjectionTest {

    @Test
    void testForm() {
        //given
        Form form = Application.getInstance(Form.class);
        
        //then
        assertThat(form, not(nullValue()));
    }
    
    @Test
    void testAuthentication() {
        //given
        Authentication authentication = Application.getInstance(Authentication.class);
        
        //then
        assertThat(authentication, not(nullValue()));
    }
    
    @Test
    void testSession() {
        //given
        Session session = Application.getInstance(Session.class);
        
        //then
        assertThat(session, not(nullValue()));
    }
    
    @Test
    void testFlash() {
        //given
        Flash flash = Application.getInstance(Flash.class);
        
        //then
        assertThat(flash, not(nullValue()));
    }
    
    @Test
    void testCache() {
        //given
        Cache cache = Application.getInstance(Cache.class);
        
        //then
        assertThat(cache, not(nullValue()));
    }
    
    @Test
    void testCrypto() {
        //given
        Crypto crypto = Application.getInstance(Crypto.class);
        
        //then
        assertThat(crypto, not(nullValue()));
    }
    
    @Test
    void testConfig() {
        //given
        Config config = Application.getInstance(Config.class);
        
        //then
        assertThat(config, not(nullValue()));
    }
    
    @Test
    void testMessages() {
        //given
        Messages messages = Application.getInstance(Messages.class);
        
        //then
        assertThat(messages, not(nullValue()));
    }
    
    @Test
    void testResponse() {
        //given
        Response response  = Application.getInstance(Response.class);
        
        //then
        assertThat(response, not(nullValue()));
    }
    
    @Test
    void testRequest() {
        //given
        Request request = Application.getInstance(Request.class);
        
        //then
        assertThat(request, not(nullValue()));
    }
    
    @Test
    void testMetricsListener() {
        //given
        MetricsListener metricsListener = Application.getInstance(MetricsListener.class);
        
        //then
        assertThat(metricsListener, not(nullValue()));
    }
    
    @Test
    void testMetrics() {
        //given
        Metrics metrics = Application.getInstance(Metrics.class);
        
        //then
        assertThat(metrics, not(nullValue()));
    }
    
    @Test
    void testFallbackHandler() {
        //given
        FallbackHandler fallbackHandler = Application.getInstance(FallbackHandler.class);
        
        //then
        assertThat(fallbackHandler, not(nullValue()));
    }
    
    @Test
    void testExceptionHandler() {
        //given
        ExceptionHandler exceptionHandler = Application.getInstance(ExceptionHandler.class);
        
        //then
        assertThat(exceptionHandler, not(nullValue()));
    }
    
    @Test
    void testMangooBootstrap() {
        //given
        MangooBootstrap mangooLifecycle = Application.getInstance(MangooBootstrap.class);
        
        //then
        assertThat(mangooLifecycle, not(nullValue()));
    }
    
    @Test
    void testCacheProvider() {
        //given
        CacheProvider cacheProvider = Application.getInstance(CacheProvider.class);
        
        //then
        assertThat(cacheProvider, not(nullValue()));
    }
    
    @Test
    void testTemplateEngine() {
        //given
        TemplateEngine templateEngine = Application.getInstance(TemplateEngine.class);
        
        //then
        assertThat(templateEngine, not(nullValue()));
    }
    
    @Test
    void testValidator() {
        //given
        Validator validator = Application.getInstance(Validator.class);
        
        //then
        assertThat(validator, not(nullValue()));
    }

    @Test
    void testLocaleHandler() {
        //given
        LocaleHandler localeHandler = Application.getInstance(LocaleHandler.class);
        
        //then
        assertThat(localeHandler, not(nullValue()));
    }
    
    @Test
    void testInboundCookiesHandler() {
        //given
        InboundCookiesHandler inboundCookieHandler = Application.getInstance(InboundCookiesHandler.class);
        
        //then
        assertThat(inboundCookieHandler, not(nullValue()));
    }
    
    @Test
    void testOutboundCookiesHandler() {
        //given
        OutboundCookiesHandler outboundCookiesHandler = Application.getInstance(OutboundCookiesHandler.class);
        
        //then
        assertThat(outboundCookiesHandler, not(nullValue()));
    }
    
    @Test
    void testResponseHandler() {
        //given
        ResponseHandler responseHandler = Application.getInstance(ResponseHandler.class);
        
        //then
        assertThat(responseHandler, not(nullValue()));
    }
    
    @Test
    void testMangooRequestFilter() {
        //given
        OncePerRequestFilter mangooRequestFilter = Application.getInstance(OncePerRequestFilter.class);
        
        //then
        assertThat(mangooRequestFilter, not(nullValue()));
    }
    
    @Test
    void testServerSentEventCloseListener() {
        //given
        ServerSentEventCloseListener serverSentEventCloseListener = Application.getInstance(ServerSentEventCloseListener.class);
        
        //then
        assertThat(serverSentEventCloseListener, not(nullValue()));
    } 
    
    @Test
    void testShutdown() {
        //given
        Shutdown shutdown = Application.getInstance(Shutdown.class);
        
        //then
        assertThat(shutdown, not(nullValue()));
    }     
}