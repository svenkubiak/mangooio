package controllers;

import io.mangoo.enums.Default;
import io.mangoo.i18n.Messages;
import io.mangoo.routing.Response;
import io.undertow.server.handlers.Cookie;
import io.undertow.server.handlers.CookieImpl;

public class I18nController {
    
    public Response translation() {
        return Response.withOk();
    }
    
    public Response localize() {
        Cookie cookie = new CookieImpl(Default.I18N_COOKIE_NAME.toString(), "en");
        return Response.withOk().andCookie(cookie);
    }
    
    public Response special() {
        return Response.withOk();
    }
    
    public Response umlaute() {
        return Response.withOk();
    }
    
    public Response messages(Messages messages) {
        return Response.withOk().andTextBody(messages.get("welcome"));
    }
}