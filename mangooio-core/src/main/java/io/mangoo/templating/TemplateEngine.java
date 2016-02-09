package io.mangoo.templating;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import freemarker.template.TemplateException;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.i18n.Messages;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.undertow.server.HttpServerExchange;

/**
 *
 * @author svenkubiak
 *
 */
public interface TemplateEngine {
    /**
     * Renders a template for a specific controller class and method
     *
     * @param flash The current flash
     * @param session The current session
     * @param form The current Form
     * @param messages The current messages
     * @param templatePath The path to the template to render
     * @param content The content map which is passed to the template
     * @return A rendered template
     *
     * @throws MangooTemplateEngineException MangooTemplateEngineException
     */
    public String render(Flash flash, Session session, Form form, Messages messages, String templatePath,
            Map<String, Object> content) throws MangooTemplateEngineException;

    /**
     * 
     * @param pathPrefix
     * @param templateName
     * @param content
     * @return
     * @throws MangooTemplateEngineException
     */
    public String render(String pathPrefix, String templateName, Map<String, Object> content) throws MangooTemplateEngineException;

    /**
     * When in dev mode, this method is used to pass the framework exception to the frontend
     *
     * @param exchange A HttpServerExchange instance
     * @param cause The throwable cause
     * @param templateException True if the exceptions occurs during exception rendering, false otherwise
     * @return A rendered template
     *
     * @throws FileNotFoundException FileNotFoundException
     * @throws IOException IOException
     * @throws TemplateException TemplateException
     */
    public  String renderException(HttpServerExchange exchange, Throwable cause, boolean templateException)
            throws MangooTemplateEngineException;
}