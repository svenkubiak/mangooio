package io.mangoo.bindings;

import org.junit.Test;

import io.mangoo.authentication.Authentication;
import io.mangoo.cache.Cache;
import io.mangoo.configuration.Config;
import io.mangoo.crypto.Crypto;
import io.mangoo.i18n.Messages;
import io.mangoo.mail.Mailer;
import io.mangoo.routing.Response;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Request;
import io.mangoo.routing.bindings.Session;
import io.mangoo.scheduler.MangooScheduler;
import io.mangoo.test.MangooTestInstance;

/**
 * 
 * @author svenkubiak
 *
 */
public class InjectionTest {

    @Test
    public void testInjection() {
        MangooTestInstance.IO.getInjector().getInstance(Form.class);
        MangooTestInstance.IO.getInjector().getInstance(Authentication.class);
        MangooTestInstance.IO.getInjector().getInstance(Session.class);
        MangooTestInstance.IO.getInjector().getInstance(Flash.class);
        MangooTestInstance.IO.getInjector().getInstance(Cache.class);
        MangooTestInstance.IO.getInjector().getInstance(Crypto.class);
        MangooTestInstance.IO.getInjector().getInstance(Config.class);
        MangooTestInstance.IO.getInjector().getInstance(Messages.class);
        MangooTestInstance.IO.getInjector().getInstance(Mailer.class);
        MangooTestInstance.IO.getInjector().getInstance(Response.class);
        MangooTestInstance.IO.getInjector().getInstance(Request.class);
        MangooTestInstance.IO.getInjector().getInstance(MangooScheduler.class);
    }
}