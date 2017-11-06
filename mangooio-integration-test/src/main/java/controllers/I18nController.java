package controllers;

import io.mangoo.enums.Default;
import io.mangoo.helpers.cookie.CookieBuilder;
import io.mangoo.routing.Response;
import io.undertow.server.handlers.Cookie;

public class I18nController {
    public Response translation() {
        return Response.withOk();
    }
    
    public Response localize() {
        Cookie cookie = CookieBuilder.create()
                .name(Default.I18N_COOKIE_NAME.toString())
                .value("en")
                .build();
        
        return Response.withOk().andCookie(cookie);
    }
    
    public Response special() {
        return Response.withOk();
    }
    
    public Response umlaute() {
        return Response.withOk();
    }
}