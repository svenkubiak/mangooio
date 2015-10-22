package io.mangoo.templating;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.common.base.Charsets;
import com.google.inject.Singleton;

import freemarker.cache.MruCacheStorage;
import freemarker.core.ParseException;
import freemarker.template.Configuration;
import freemarker.template.MalformedTemplateNameException;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateNotFoundException;
import freemarker.template.Version;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.i18n.Messages;
import io.mangoo.models.Source;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.templating.directives.AuthenticityFormDirective;
import io.mangoo.templating.directives.AuthenticityTokenDirective;
import io.mangoo.templating.methods.I18nMethod;
import io.mangoo.utils.RequestUtils;
import io.mangoo.utils.ThrowableUtils;
import io.undertow.server.HttpServerExchange;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class TemplateEngine {
    private Configuration configuration = new Configuration(VERSION);
    private String baseDirectory;
    private static final int MAX_CHARS = 65536;
    private static final int ONE_SECOND_MS = 1000;
    private static final int STRONG_SIZE_LIMIT = 20;
    private static final Version VERSION = new Version(2, 3, 23);

    public TemplateEngine() {
        this.configuration.setClassForTemplateLoading(this.getClass(), Default.TEMPLATES_FOLDER.toString());
        this.configuration.setDefaultEncoding(Charsets.UTF_8.name());
        this.configuration.setOutputEncoding(Charsets.UTF_8.name());
        this.configuration.setLocalizedLookup(false);
        this.configuration.setNumberFormat(Default.NUMBER_FORMAT.toString());
        this.configuration.setTemplateLoader(new TemplateEngineLoader(configuration.getTemplateLoader()));
        this.configuration.setAPIBuiltinEnabled(true);
        
        if (Application.inDevMode()) {
            this.configuration.setTemplateUpdateDelayMilliseconds(ONE_SECOND_MS);
        } else {
            this.configuration.setTemplateUpdateDelayMilliseconds(Integer.MAX_VALUE);
            this.configuration.setCacheStorage(new MruCacheStorage(STRONG_SIZE_LIMIT, Integer.MAX_VALUE));
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append(System.getProperty("user.dir")).append(File.separator)
        .append("src").append(File.separator).append("main")
        .append(File.separator).append("java");

        this.baseDirectory = buffer.toString();
    }

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
     * @throws TemplateNotFoundException TemplateNotFoundException
     * @throws MalformedTemplateNameException MalformedTemplateNameException
     * @throws ParseException ParseException
     * @throws IOException IOException
     * @throws TemplateException TemplateException
     */
    @SuppressWarnings("all")
    public String render(Flash flash, Session session, Form form, Messages messages, String templatePath, Map<String, Object> content) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
        Template template = configuration.getTemplate(templatePath);
        content.put("form", form);
        content.put("flash", flash);
        content.put("session", session);
        content.put("i18n", new I18nMethod(messages));
        content.put("authenticityToken", new AuthenticityTokenDirective(session));
        content.put("authenticityForm", new AuthenticityFormDirective(session));

        return processTemplate(content, template);
    }

    @SuppressWarnings("all")
    public String render(String pathPrefix, String templateName, Map<String, Object> content) throws TemplateNotFoundException, MalformedTemplateNameException, ParseException, IOException, TemplateException {
        Template template = configuration.getTemplate(pathPrefix + "/" + RequestUtils.getTemplateName(templateName));

        return processTemplate(content, template);
    }

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
    @SuppressWarnings("all")
    public String renderException(HttpServerExchange exchange, Throwable cause, boolean templateException) throws FileNotFoundException, IOException, TemplateException {
        Writer writer = new StringWriter();
        Map<String, Object> content = new HashMap<String, Object>();
        content.put("templateException", templateException);

        if (templateException) {
            content.put("exceptions", cause.getMessage().split("\n"));
        } else {
            StackTraceElement stackTraceElement = Arrays.asList(cause.getStackTrace()).get(0);
            String sourceCodePath = ThrowableUtils.getSourceCodePath(stackTraceElement);

            List<Source> sources = ThrowableUtils.getSources(stackTraceElement.getLineNumber(), sourceCodePath);
            content.put("sources", sources);
            content.put("cause", ExceptionUtils.getMessage(cause));
            content.put("url", exchange.getRequestURI());
            content.put("method", exchange.getRequestMethod());
            content.put("line", stackTraceElement.getLineNumber());
            content.put("causeSource", cause.toString());
            content.put("stackTraces", cause.getStackTrace());
            content.put("sourceCodePath", StringUtils.substringAfter(new File(this.baseDirectory).toPath().resolve(sourceCodePath).toFile().getPath(), "src/main/java") + " around line " + stackTraceElement.getLineNumber());
        }

        Configuration config = new Configuration(VERSION);
        config.setClassForTemplateLoading(this.getClass(), Default.DEFAULT_TEMPLATES_DIR.toString());

        Template template = config.getTemplate(Default.EXCEPTION_TEMPLATE_NAME.toString());
        template.process(content, writer);

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
    private String processTemplate(Map<String, Object> content, Template template) throws TemplateException, IOException {
        StringWriter buffer = new StringWriter(MAX_CHARS);
        template.process(content, buffer);

        Writer writer = new StringWriter();
        writer.write(buffer.toString());
        writer.close();

        return buffer.toString();
    }
}