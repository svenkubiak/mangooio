package io.mangoo.routing.bindings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.i18n.Messages;
import io.mangoo.interfaces.MangooLifecycle;
import io.mangoo.interfaces.MangooRequestFilter;
import io.mangoo.managers.ExecutionManager;
import io.mangoo.managers.ServerEventManager;
import io.mangoo.managers.WebSocketManager;
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
import io.mangoo.templating.TemplateEngine;

/**
 * 
 * @author svenkubiak
 *
 */
public class InjectionTest {

    @Test
    public void testInjection() {
        //given
        Form form = Application.getInstance(Form.class);
        Authentication authentication = Application.getInstance(Authentication.class);
        Session session = Application.getInstance(Session.class);
        Flash flash = Application.getInstance(Flash.class);
        Cache cache = Application.getInstance(Cache.class);
        Crypto crypto = Application.getInstance(Crypto.class);
        Config config = Application.getInstance(Config.class);
        Messages messages = Application.getInstance(Messages.class);
        Response response  = Application.getInstance(Response.class);
        Request request = Application.getInstance(Request.class);
        Scheduler mangooSchedulder = Application.getInstance(Scheduler.class);
        ExecutionManager executionManager = Application.getInstance(ExecutionManager.class);
        WebSocketManager webSocketManager = Application.getInstance(WebSocketManager.class);
        ServerEventManager serverEventManager = Application.getInstance(ServerEventManager.class);
        MetricsListener metricsListener = Application.getInstance(MetricsListener.class);
        Metrics metrics = Application.getInstance(Metrics.class);
        FallbackHandler fallbackHandler = Application.getInstance(FallbackHandler.class);
        ExceptionHandler exceptionHandler = Application.getInstance(ExceptionHandler.class);
        MangooLifecycle mangooLifecycle = Application.getInstance(MangooLifecycle.class);
        CacheProvider cacheProvider = Application.getInstance(CacheProvider.class);
        TemplateEngine templateEngine = Application.getInstance(TemplateEngine.class);
        Validator validator = Application.getInstance(Validator.class);
        LimitHandler limitHandler = Application.getInstance(LimitHandler.class);
        LocaleHandler localeHandler = Application.getInstance(LocaleHandler.class);
        InboundCookiesHandler inboundCookieHandler = Application.getInstance(InboundCookiesHandler.class);
        OutboundCookiesHandler outboundCookiesHandler = Application.getInstance(OutboundCookiesHandler.class);
        ResponseHandler responseHandler = Application.getInstance(ResponseHandler.class);
        MangooRequestFilter mangooRequestFilter = Application.getInstance(MangooRequestFilter.class);
        ServerSentEventCloseListener serverSentEventCloseListener = Application.getInstance(ServerSentEventCloseListener.class);
        WebSocketCloseListener webSocketCloseListener = Application.getInstance(WebSocketCloseListener.class);
        SchedulerFactory schedulerFactory = Application.getInstance(SchedulerFactory.class);
        
        //then
        assertThat(schedulerFactory, not(nullValue()));
        assertThat(webSocketCloseListener, not(nullValue()));
        assertThat(serverSentEventCloseListener, not(nullValue()));
        assertThat(outboundCookiesHandler, not(nullValue()));
        assertThat(mangooRequestFilter, not(nullValue()));
        assertThat(responseHandler, not(nullValue()));
        assertThat(inboundCookieHandler, not(nullValue()));
        assertThat(localeHandler, not(nullValue()));
        assertThat(limitHandler, not(nullValue()));
        assertThat(validator, not(nullValue()));
        assertThat(templateEngine, not(nullValue()));
        assertThat(cacheProvider, not(nullValue()));
        assertThat(mangooLifecycle, not(nullValue()));
        assertThat(exceptionHandler, not(nullValue()));
        assertThat(fallbackHandler, not(nullValue()));
        assertThat(form, not(nullValue()));
        assertThat(authentication, not(nullValue()));
        assertThat(session, not(nullValue()));
        assertThat(flash, not(nullValue()));
        assertThat(cache, not(nullValue()));
        assertThat(crypto, not(nullValue()));
        assertThat(config, not(nullValue()));
        assertThat(messages, not(nullValue()));
        assertThat(response, not(nullValue()));
        assertThat(request, not(nullValue()));
        assertThat(mangooSchedulder, not(nullValue()));
        assertThat(executionManager, not(nullValue()));
        assertThat(webSocketManager, not(nullValue()));
        assertThat(serverEventManager, not(nullValue()));
        assertThat(metricsListener, not(nullValue()));
        assertThat(metrics, not(nullValue()));
    }
}