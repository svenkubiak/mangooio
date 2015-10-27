package io.mangoo.bindings;

import org.junit.Test;

import io.mangoo.authentication.Authentication;
import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.crypto.Crypto;
import io.mangoo.i18n.Messages;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.bindings.Session;
import io.mangoo.scheduler.MangooScheduler;
import io.mangoo.test.MangooInstance;

/**
 * 
 * @author svenkubiak
 *
 */
public class InjectionTest {

    @Test
    public void testInjection() {
        MangooInstance.TEST.getInjector().getInstance(Form.class);
        MangooInstance.TEST.getInjector().getInstance(Authentication.class);
        MangooInstance.TEST.getInjector().getInstance(Session.class);
        MangooInstance.TEST.getInjector().getInstance(Flash.class);
        MangooInstance.TEST.getInjector().getInstance(Cache.class);
        MangooInstance.TEST.getInjector().getInstance(Crypto.class);
        MangooInstance.TEST.getInjector().getInstance(Config.class);
        MangooInstance.TEST.getInjector().getInstance(Messages.class);
        MangooInstance.TEST.getInjector().getInstance(Response.class);
        MangooInstance.TEST.getInjector().getInstance(Request.class);
        MangooInstance.TEST.getInjector().getInstance(MangooScheduler.class);
    }
}