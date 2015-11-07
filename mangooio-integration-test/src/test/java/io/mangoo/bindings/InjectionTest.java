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
import io.mangoo.scheduler.Scheduler;
import io.mangoo.test.MangooInstance;

/**
 * 
 * @author svenkubiak
 *
 */
public class InjectionTest {

    @Test
    public void testInjection() {
        //given
        Form form = MangooInstance.TEST.getInstance(Form.class);
        Authentication authentication = MangooInstance.TEST.getInstance(Authentication.class);
        Session session = MangooInstance.TEST.getInstance(Session.class);
        Flash flash = MangooInstance.TEST.getInstance(Flash.class);
        Cache cache = MangooInstance.TEST.getInstance(Cache.class);
        Crypto crypto = MangooInstance.TEST.getInstance(Crypto.class);
        Config config = MangooInstance.TEST.getInstance(Config.class);
        Messages messages = MangooInstance.TEST.getInstance(Messages.class);
        Response response  = MangooInstance.TEST.getInstance(Response.class);
        Request request = MangooInstance.TEST.getInstance(Request.class);
        Scheduler mangooSchedulder = MangooInstance.TEST.getInstance(Scheduler.class);
        ExecutionManager executionManager = MangooInstance.TEST.getInstance(ExecutionManager.class);
        WebSocketManager webSocketManager = MangooInstance.TEST.getInstance(WebSocketManager.class);
        ServerEventManager serverEventManager = MangooInstance.TEST.getInstance(ServerEventManager.class);
        
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
    }
}