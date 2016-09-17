package controllers;

import io.mangoo.enums.Default;
import io.mangoo.routing.Response;
import io.mangoo.utils.cookie.CookieBuilder;
import io.undertow.server.handlers.Cookie;

public class I18nController {
    public Response translation() {
        return Response.withOk();
    }
    
    public Response localize() {
        Cookie cookie = CookieBuilder.create()
                .name(Default.COOKIE_I18N_NAME.toString())
                .value("en")
                .build();
        
        return Response.withOk().andCookie(cookie);
    }
}