package io.mangoo.bindings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.core.Application;
import io.mangoo.crypto.Crypto;
import io.mangoo.i18n.Messages;
import io.mangoo.managers.ExecutionManager;
import io.mangoo.managers.ServerEventManager;
import io.mangoo.managers.WebSocketManager;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Authentication;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.bindings.Session;
import io.mangoo.routing.listeners.MetricsListener;
import io.mangoo.scheduler.Scheduler;

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
        
        //then
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
    }
}