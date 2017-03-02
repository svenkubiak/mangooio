package io.mangoo.templating;

import java.util.Locale;
import java.util.Map;

import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.i18n.Messages;
import io.mangoo.models.Subject;
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
     * @param subject The current subject
     * @param templatePath The path to the template to render
     * @param content The content map which is passed to the template
     * @param path The relative path of the request 
     * @param locale The locale to be used 
     * @return A rendered template
     *
     * @throws MangooTemplateEngineException MangooTemplateEngineException
     */
    public String render(Flash flash, Session session, Form form, Messages messages, Subject subject, String templatePath, Map<String, Object> content, String path, Locale locale) throws MangooTemplateEngineException;

    /**
     * Renders a template for a specific controller class and method
     * 
     * @param pathPrefix A path prefix for the template
     * @param templateName The path to the template to render
     * @param content The content map which is passed to the template
     * @return A rendered template
     * 
     * @throws MangooTemplateEngineException MangooTemplateEngineException
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
     * @throws MangooTemplateEngineException MangooTemplateEngineException
     */
    public String renderException(HttpServerExchange exchange, Throwable cause, boolean templateException) throws MangooTemplateEngineException;
    
    
    /**
     * Checks if a given template name has the current suffix and sets is
     * if it does not exist
     *
     * @param templateName The name of the template file
     * @return The template name with correct suffix
     */
    public String getTemplateName(String templateName);
}