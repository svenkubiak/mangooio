package io.mangoo.bindings;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;

import org.junit.Test;

import io.mangoo.authentication.Authentication;
import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.crypto.Crypto;
import io.mangoo.i18n.Messages;
import io.mangoo.managers.ExecutionManager;
import io.mangoo.managers.ServerEventManager;
import io.mangoo.managers.WebSocketManager;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.bindings.Session;
import io.mangoo.routing.listeners.MetricsListener;
import io.mangoo.scheduler.Scheduler;
import io.mangoo.test.Mangoo;

/**
 * 
 * @author svenkubiak
 *
 */
public class InjectionTest {

    @Test
    public void testInjection() {
        //given
        Form form = Mangoo.TEST.getInstance(Form.class);
        Authentication authentication = Mangoo.TEST.getInstance(Authentication.class);
        Session session = Mangoo.TEST.getInstance(Session.class);
        Flash flash = Mangoo.TEST.getInstance(Flash.class);
        Cache cache = Mangoo.TEST.getInstance(Cache.class);
        Crypto crypto = Mangoo.TEST.getInstance(Crypto.class);
        Config config = Mangoo.TEST.getInstance(Config.class);
        Messages messages = Mangoo.TEST.getInstance(Messages.class);
        Response response  = Mangoo.TEST.getInstance(Response.class);
        Request request = Mangoo.TEST.getInstance(Request.class);
        Scheduler mangooSchedulder = Mangoo.TEST.getInstance(Scheduler.class);
        ExecutionManager executionManager = Mangoo.TEST.getInstance(ExecutionManager.class);
        WebSocketManager webSocketManager = Mangoo.TEST.getInstance(WebSocketManager.class);
        ServerEventManager serverEventManager = Mangoo.TEST.getInstance(ServerEventManager.class);
        MetricsListener metricsListener = Mangoo.TEST.getInstance(MetricsListener.class);
        
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