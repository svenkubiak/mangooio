package io.mangoo.templating;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import freemarker.cache.MruCacheStorage;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;
import io.mangoo.core.Application;
import io.mangoo.enums.Default;
import io.mangoo.enums.Required;
import io.mangoo.exceptions.MangooTemplateEngineException;
import io.mangoo.models.Source;
import io.undertow.server.HttpServerExchange;
import no.api.freemarker.java8.Java8ObjectWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 
 * @author svenkubiak
 *
 */
public class TemplateEngine {
    private final Configuration configuration = new Configuration(VERSION);
    private static final String TEMPLATE_SUFFIX = ".ftl";
    private static final String REGEX = "\n";
    private static final int MIN_LINES = 6;
    private static final int MAX_LINES = 8;
    private static final int MAX_CHARS = 65_536;
    private static final int ONE_SECOND_MS = 1000;
    private static final int STRONG_SIZE_LIMIT = 20;
    private static final Version VERSION = new Version(2, 3, 30);
    
    public TemplateEngine() {
        configuration.setClassForTemplateLoading(getClass(), Default.TEMPLATES_FOLDER.toString());
        configuration.setDefaultEncoding(StandardCharsets.UTF_8.name());
        configuration.setOutputEncoding(StandardCharsets.UTF_8.name());
        configuration.setLocalizedLookup(false);
        configuration.setNumberFormat(Default.NUMBER_FORMAT.toString());
        configuration.setAPIBuiltinEnabled(true);
        configuration.setObjectWrapper(new Java8ObjectWrapper(VERSION));
        configuration.setOutputFormat(HTMLOutputFormat.INSTANCE);
        configuration.setRecognizeStandardFileExtensions(false);

        if (Application.inDevMode()) {
            configuration.setTemplateUpdateDelayMilliseconds(ONE_SECOND_MS);
        } else {
            configuration.setTemplateUpdateDelayMilliseconds(Integer.MAX_VALUE);
            configuration.setCacheStorage(new MruCacheStorage(STRONG_SIZE_LIMIT, Integer.MAX_VALUE));
        }
    }

    @SuppressFBWarnings(value = "TEMPLATE_INJECTION_FREEMARKER")
    public String renderTemplate(TemplateContext context) throws MangooTemplateEngineException {
        Template template;
        try {
            template = configuration.getTemplate(context.getTemplatePath());
        } catch (IOException e) {
            throw new MangooTemplateEngineException("Template not found on path: " + context.getTemplatePath(), e);
        }

        StringWriter buffer = new StringWriter(MAX_CHARS);
        try {
            template.process(context.getContent(), buffer);
        } catch (TemplateException | IOException e) {
            throw new MangooTemplateEngineException("Failed to process template", e);
        }

        return buffer.toString();
    }

    @SuppressFBWarnings({"PATH_TRAVERSAL_IN","TEMPLATE_INJECTION_FREEMARKER"})
    public String renderException(HttpServerExchange exchange, Throwable cause, boolean templateException) throws MangooTemplateEngineException {
        Map<String, Object> content = new HashMap<>();
        content.put("templateException", templateException);

        if (templateException) {
            content.put("exceptions", cause.getMessage().split(REGEX)); //NOSONAR Method is only used in development mode
        } else {
            StackTraceElement stackTraceElement = cause.getStackTrace()[0];
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
            content.put("sourceCodePath", StringUtils.substringAfter(Paths.get(getBaseDirectory()).resolve(sourceCodePath).toFile().getPath(), "src/main/java") + " around line " + stackTraceElement.getLineNumber());
        }

        Configuration config = new Configuration(VERSION);
        config.setClassForTemplateLoading(getClass(), Default.DEFAULT_TEMPLATES_DIR.toString());

        Writer writer = new StringWriter();
        Template template;
        try {
            template = config.getTemplate("exception.ftl");
            template.process(content, writer);
        } catch (IOException | TemplateException e) {
            throw new MangooTemplateEngineException("Failed to process template", e);
        }

        return writer.toString();
    }
    
    public String getTemplateName(String templateName) {
        Objects.requireNonNull(templateName, Required.TEMPLATE_NAME.toString());
        return templateName.endsWith(TEMPLATE_SUFFIX) ? templateName : (templateName + TEMPLATE_SUFFIX);
    }

    /**
     * Retrieves the lines of code where an exception occurred
     *
     * @param errorLine The line number of the exception
     * @param sourcePath The path to the source code file
     * @return A list of source code with the exception and surrounding lines
     *
     * @throws IOException If an IO exception occurs
     */
    @SuppressFBWarnings(value = "PATH_TRAVERSAL_IN")
    private List<Source> getSources(int errorLine, String sourcePath) throws IOException {
        Objects.requireNonNull(sourcePath, Required.SOURCE_PATH.toString());

        StringBuilder buffer = new StringBuilder();
        buffer.append(System.getProperty("user.dir"))
        .append(File.separator)
        .append("src")
        .append(File.separator)
        .append("main")
        .append(File.separator)
        .append("java");

        List<Source> sources = new ArrayList<>();
        Path templateFile = Paths.get(buffer.toString()).resolve(sourcePath);
        if (Files.exists(templateFile)) {
            List<String> lines = Files.readAllLines(templateFile);

            int index = 0;
            for (String line : lines) {
                if ( (index + MAX_LINES > errorLine) && (index - MIN_LINES < errorLine) ) {
                    sources.add(new Source((index + 1) == errorLine, index + 1, line));
                }
                index++;
            }
        }

        return sources;
    }

    /**
     * @return The OS specific path to src/main/java
     */
    private String getBaseDirectory() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(System.getProperty("user.dir"))
        .append(File.separator)
        .append("src")
        .append(File.separator)
        .append("main")
        .append(File.separator)
        .append("java");
        
        return buffer.toString();
    }

    /**
     * Retrieves the source code file name from an StackTraceElement
     *
     * @param stackTraceElement The StackTraceElement to check
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