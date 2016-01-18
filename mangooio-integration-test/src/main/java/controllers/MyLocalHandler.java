package controllers;

import io.mangoo.interfaces.handlers.MangooLocaleHandler;
import io.mangoo.routing.handlers.LocaleHandler;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;

public class MyLocalHandler extends LocaleHandler implements HttpHandler, MangooLocaleHandler {

    @Override
    public void handleRequest(HttpServerExchange arg0) throws Exception {
        System.out.println("my custon locale handler");
    }
}