package controllers;

import io.mangoo.constants.Default;
import io.mangoo.i18n.Messages;
import io.mangoo.routing.Response;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;

public class I18nController {
    
    public Response translation() {
        return Response.ok().render();
    }
    
    public Response localize() {
        Cookie cookie = new CookieImpl(Default.I18N_COOKIE_NAME, "en");
        return Response.ok().cookie(cookie).render();
    }
    
    public Response special() {
        return Response.ok().render();
    }
    
    public Response umlaute() {
        return Response.ok().render();
    }
    
    public Response messages(Messages messages) {
        return Response.ok().bodyText(messages.get("welcome"));
    }
}