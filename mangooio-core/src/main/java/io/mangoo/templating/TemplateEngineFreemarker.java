package io.mangoo.templating;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.common.base.Charsets;

import freemarker.cache.MruCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.i18n.Messages;
import io.mangoo.interfaces.MangooTemplateEngine;
import io.mangoo.models.Source;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.templating.directives.FormDirective;
import io.mangoo.templating.directives.TokenDirective;
import io.mangoo.templating.methods.I18nMethod;
import io.mangoo.templating.methods.LocationMethod;
import io.mangoo.templating.methods.PrettyTimeMethod;
import io.mangoo.templating.methods.RouteMethod;
import io.mangoo.utils.BootstrapUtils;
import io.undertow.server.HttpServerExchange;
import no.api.freemarker.java8.Java8ObjectWrapper;

/**
 * 
 * @author svenkubiak
 *
 */
public class TemplateEngineFreemarker implements MangooTemplateEngine {
    private final Configuration configuration = new Configuration(VERSION);
    private static final String TEMPLATE_SUFFIX = ".ftl";
    private static final int MAX_CHARS = 65_536;
    private static final int ONE_SECOND_MS = 1000;
    private static final int STRONG_SIZE_LIMIT = 20;
    private static final Version VERSION = new Version(2, 3, 27);
    private static final List<String> blacklist = Arrays.asList(
            "form", "flash", "session", "subject", "i18n", "route", "location", "prettytime", "authenticity", "authenticityForm"
            );
    
    public TemplateEngineFreemarker() {
        this.configuration.setClassForTemplateLoading(this.getClass(), Default.TEMPLATES_FOLDER.toString());
        this.configuration.setDefaultEncoding(Charsets.UTF_8.name());
        this.configuration.setOutputEncoding(Charsets.UTF_8.name());
        this.configuration.setLocalizedLookup(false);
        this.configuration.setNumberFormat(Default.NUMBER_FORMAT.toString());
        this.configuration.setTemplateLoader(new TemplateEngineLoader(configuration.getTemplateLoader()));
        this.configuration.setAPIBuiltinEnabled(true);
        this.configuration.setObjectWrapper(new Java8ObjectWrapper(VERSION));

        if (Application.inDevMode()) {
            this.configuration.setTemplateUpdateDelayMilliseconds(ONE_SECOND_MS);
        } else {
            this.configuration.setTemplateUpdateDelayMilliseconds(Integer.MAX_VALUE);
            this.configuration.setCacheStorage(new MruCacheStorage(STRONG_SIZE_LIMIT, Integer.MAX_VALUE));
        }
    }

    @Override
    @SuppressWarnings("all")
    public String render(Flash flash, Session session, Form form, Messages messages, String templatePath, Map<String, Object> content, String controller, Locale locale) throws MangooTemplateEngineException {
        Template template;
        try {
            template = configuration.getTemplate(templatePath);
        } catch (IOException e) {
            throw new MangooTemplateEngineException("Template not found on path: " + templatePath, e);
        }
        
        if (!Application.inProdMode()) { 
            Optional<String> key = containsInvalidKey(content);
            if (key.isPresent()) {
                throw new MangooTemplateEngineException(templatePath + " contains the following restricted key: " + key.get());
            }
        }
        
        content.put("form", form);
        content.put("flash", flash);
        content.put("session", session);
        content.put("i18n", new I18nMethod(messages));
        content.put("route", new RouteMethod());
        content.put("location", new LocationMethod(controller));
        content.put("prettytime", new PrettyTimeMethod(locale));
        content.put("authenticity", new TokenDirective(session));
        content.put("authenticityForm", new FormDirective(session));

        return processTemplate(content, template);
    }

    @Override
    @SuppressWarnings("all")
    public String render(String pathPrefix, String templateName, Map<String, Object> content) throws MangooTemplateEngineException {
        Template template;
        try {
            template = configuration.getTemplate(pathPrefix + "/" + getTemplateName(templateName));
        } catch (IOException e) {
            throw new MangooTemplateEngineException("Failed to render template", e);
        }

        return processTemplate(content, template);
    }

    @Override
    @SuppressWarnings("all")
    public String renderException(HttpServerExchange exchange, Throwable cause, boolean templateException) throws MangooTemplateEngineException {
        Writer writer = new StringWriter();
        Map<String, Object> content = new HashMap<>();
        content.put("templateException", templateException);

        if (templateException) {
            content.put("exceptions", cause.getMessage().split("\n"));
        } else {
            StackTraceElement stackTraceElement = Arrays.asList(cause.getStackTrace()).get(0);
            String sourceCodePath = getSourceCodePath(stackTraceElement);

            List<Source> sources;
            try {
                sources = getSources(stackTraceElement.getLineNumber(), sourceCodePath);
            } catch (IOException e) {
                throw new MangooTemplateEngineException("Failed to get source lines of exception", e);
            }
            content.put("sources", sources);
            content.put("cause", ExceptionUtils.getMessage(cause));
            content.put("url", exchange.getRequestURI());
            content.put("method", exchange.getRequestMethod());
            content.put("line", stackTraceElement.getLineNumber());
            content.put("causeSource", cause.toString());
            content.put("stackTraces", cause.getStackTrace());
            content.put("sourceCodePath", StringUtils.substringAfter(new File(BootstrapUtils.getBaseDirectory()).toPath().resolve(sourceCodePath).toFile().getPath(), "src/main/java") + " around line " + stackTraceElement.getLineNumber());
        }

        Configuration config = new Configuration(VERSION);
        config.setClassForTemplateLoading(this.getClass(), Default.DEFAULT_TEMPLATES_DIR.toString());

        Template template;
        try {
            template = config.getTemplate("exception.ftl");
            template.process(content, writer);
        } catch (IOException | TemplateException e) {
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
    private String processTemplate(Map<String, Object> content, Template template) throws MangooTemplateEngineException {
        Objects.requireNonNull(content, Required.CONTENT.toString());
        Objects.requireNonNull(template, Required.TEMPLATE.toString());
        
        StringWriter buffer = new StringWriter(MAX_CHARS);
        try {
            template.process(content, buffer);
        } catch (TemplateException | IOException e) {
            throw new MangooTemplateEngineException("Failed to process template", e);
        }

        return buffer.toString();
    }

    @Override
    public String getTemplateName(String templateName) {
        Objects.requireNonNull(templateName, Required.TEMPLATE_NAME.toString());
        return templateName.endsWith(TEMPLATE_SUFFIX) ? templateName : (templateName + TEMPLATE_SUFFIX);
    }
    
    private Optional<String> containsInvalidKey(Map<String, Object> content) {
        String found = null;
        for (String key : blacklist) {
            if (content.containsKey(key)) {
                found = key;
                break;
            }
        }
        
        return Optional.ofNullable(found);
    }
    

    /**
     * Retrieves the lines of code where an exception occurred
     *
     * @param errorLine The line number of the exception
     * @param sourcePath The path to the source code file
     * @return A list of source code with the exception and surrounding lines
     *
     * @throws FileNotFoundException If the file is not found
     * @throws IOException If an IO exception occurs
     */
    @SuppressWarnings("all")
    private List<Source> getSources(int errorLine, String sourcePath) throws FileNotFoundException, IOException {
        Objects.requireNonNull(sourcePath, Required.SOURCE_PATH.toString());

        StringBuffer buffer = new StringBuffer();
        buffer.append(System.getProperty("user.dir"))
        .append(File.separator)
        .append("src")
        .append(File.separator)
        .append("main")
        .append(File.separator)
        .append("java");

        List<Source> sources = new ArrayList<Source>();
        File templateFile = new File(buffer.toString()).toPath().resolve(sourcePath).toFile();
        if (templateFile.exists()) {
            List<String> lines = IOUtils.readLines(new FileInputStream(templateFile), Charsets.UTF_8);

            int index = 0;
            for (String line : lines) {
                if ( (index + 8 > errorLine) && (index - 6 < errorLine) ) {
                    sources.add(new Source((index + 1) == errorLine, index + 1, line));
                }
                index++;
            }
        }

        return sources;
    }

    /**
     * Retrieves the source code file name from an StrackTraceElement
     *
     * @param stackTraceElement The StrackTraceElement to check
     * @return Source code filename
     */
    private String getSourceCodePath(StackTraceElement stackTraceElement) {
        Objects.requireNonNull(stackTraceElement, Required.STACK_TRACE_ELEMENT.toString());

        String packageName = stackTraceElement.getClassName();
        int position = packageName.lastIndexOf('.');
        if (position > 0) {
            packageName = packageName.substring(0, position);
            return StringUtils.replace(packageName, ".", File.separator) + File.separator + stackTraceElement.getFileName();
        }

        return stackTraceElement.getFileName();
    }
}