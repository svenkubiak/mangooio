package io.mangoo.templating.pebble;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.error.PebbleException;
import com.mitchellbosecke.pebble.template.PebbleTemplate;

import io.mangoo.core.Application;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.i18n.Messages;
import io.mangoo.models.Source;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.templating.TemplateEngine;
import io.mangoo.templating.pebble.tags.AuthenticityTag;
import io.mangoo.utils.ThrowableUtils;
import io.undertow.server.HttpServerExchange;

/**
 *
 * @author svenkubiak
 *
 */
public class TemplateEnginePebble implements TemplateEngine {
    private static final String TEMPLATE_SUFFIX = ".peb";
    private final PebbleEngine pebbleEngine;

    public TemplateEnginePebble() {
        this.pebbleEngine = new PebbleEngine.Builder().build();
    }

    @Override
    public String render(Flash flash, Session session, Form form, Messages messages, String templatePath, Map<String, Object> content) throws MangooTemplateEngineException {
        PebbleTemplate pebbleTemplate;

        try {
            pebbleTemplate = pebbleEngine.getTemplate("templates/" + templatePath);
        } catch (final PebbleException e) {
            throw new MangooTemplateEngineException("Failed to render template", e);
        }

        content.put("form", form);
        content.put("flash", flash);
        content.put("session", session);
        content.put("authenticity", new AuthenticityTag(session));

        return processTemplate(content, pebbleTemplate);
    }

    @Override
    public String render(String pathPrefix, String templateName, Map<String, Object> content) throws MangooTemplateEngineException {
        PebbleTemplate pebbleTemplate;
        try {
            pebbleTemplate = pebbleEngine.getTemplate(pathPrefix + "/" + getTemplateName(templateName));
        } catch (final PebbleException e) {
            throw new MangooTemplateEngineException("Failed to render Template", e);
        }

        return processTemplate(content, pebbleTemplate);
    }

    @Override
    public String renderException(HttpServerExchange exchange, Throwable cause, boolean templateException) throws MangooTemplateEngineException {
        final Map<String, Object> content = new HashMap<>();
        content.put("templateException", templateException);

        if (templateException) {
            content.put("exceptions", cause.getMessage().split("\n"));
        } else {
            final StackTraceElement stackTraceElement = Arrays.asList(cause.getStackTrace()).get(0);
            final String sourceCodePath = ThrowableUtils.getSourceCodePath(stackTraceElement);

            List<Source> sources;
            try {
                sources = ThrowableUtils.getSources(stackTraceElement.getLineNumber(), sourceCodePath);
            } catch (final IOException e) {
                throw new MangooTemplateEngineException("Failed to get source lines of exception", e);
            }
            content.put("sources", sources);
            content.put("cause", ExceptionUtils.getMessage(cause));
            content.put("url", exchange.getRequestURI());
            content.put("method", exchange.getRequestMethod());
            content.put("line", stackTraceElement.getLineNumber());
            content.put("causeSource", cause.toString());
            content.put("stackTraces", cause.getStackTrace());
            content.put("sourceCodePath", StringUtils.substringAfter(new File(Application.getBaseDirectory()).toPath().resolve(sourceCodePath).toFile().getPath(), "src/main/java") + " around line " + stackTraceElement.getLineNumber()); //NOSONAR
        }

        final Writer writer = new StringWriter();
        try {
            final PebbleTemplate pebbleTemplate = pebbleEngine.getTemplate("exception.peb");
            pebbleTemplate.evaluate(writer, content);
        } catch (PebbleException | IOException e) {
            throw new MangooTemplateEngineException("Failed to process template", e);
        }

        return writer.toString();
    }

    /**
     * Process a template by rendering the content into the template
     *
     * @param content The content to render in the template
     * @param template The template
     * @return A completely rendered template
     *
     * @throws TemplateExceptions TemplateExceptions
     * @throws IOException IOException
     */
    private String processTemplate(Map<String, Object> content, PebbleTemplate pebbleTemplate) throws MangooTemplateEngineException {
        Objects.requireNonNull(content, "content can not be null");
        Objects.requireNonNull(pebbleTemplate, "pebbleTemplate can not be null");

        final Writer writer = new StringWriter();
        try {
            pebbleTemplate.evaluate(writer, content);
        } catch (PebbleException | IOException e) {
            throw new MangooTemplateEngineException("Failed to process template", e);
        }

        return writer.toString();
    }

    @Override
    public String getTemplateName(String templateName) {
        Objects.requireNonNull(templateName, "templateName can not be null");

        return templateName.endsWith(TEMPLATE_SUFFIX) ? templateName : (templateName + TEMPLATE_SUFFIX);
    }
}