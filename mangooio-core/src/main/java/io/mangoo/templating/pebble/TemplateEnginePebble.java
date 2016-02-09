package io.mangoo.templating.pebble;

import java.util.Map;

import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.i18n.Messages;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.templating.TemplateEngine;
import io.undertow.server.HttpServerExchange;

public class TemplateEnginePebble implements TemplateEngine {

    @Override
    public String render(Flash flash, Session session, Form form, Messages messages, String templatePath,
            Map<String, Object> content) throws MangooTemplateEngineException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String render(String pathPrefix, String templateName, Map<String, Object> content)
            throws MangooTemplateEngineException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String renderException(HttpServerExchange exchange, Throwable cause, boolean templateException)
            throws MangooTemplateEngineException {
        // TODO Auto-generated method stub
        return null;
    }

}
