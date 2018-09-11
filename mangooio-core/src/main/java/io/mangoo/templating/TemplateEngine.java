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
import java.util.Map;
import java.util.Objects;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.common.base.Charsets;

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
import io.mangoo.utils.BootstrapUtils;
import io.undertow.server.HttpServerExchange;
import no.api.freemarker.java8.Java8ObjectWrapper;

/**
 * 
 * @author svenkubiak
 *
 */
public class TemplateEngine {
    private final Configuration configuration = new Configuration(VERSION);
    private static final String TEMPLATE_SUFFIX = ".ftl";
    private static final int MAX_CHARS = 65_536;
    private static final int ONE_SECOND_MS = 1000;
    private static final int STRONG_SIZE_LIMIT = 20;
    private static final Version VERSION = new Version(2, 3, 28);
    
    public TemplateEngine() {
        this.configuration.setClassForTemplateLoading(this.getClass(), Default.TEMPLATES_FOLDER.toString());
        this.configuration.setDefaultEncoding(Charsets.UTF_8.name());
        this.configuration.setOutputEncoding(Charsets.UTF_8.name());
        this.configuration.setLocalizedLookup(false);
        this.configuration.setNumberFormat(Default.NUMBER_FORMAT.toString());
        this.configuration.setAPIBuiltinEnabled(true);
        this.configuration.setObjectWrapper(new Java8ObjectWrapper(VERSION));
        this.configuration.setOutputFormat(HTMLOutputFormat.INSTANCE);
        this.configuration.setRecognizeStandardFileExtensions(false);

        if (Application.inDevMode()) {
            this.configuration.setTemplateUpdateDelayMilliseconds(ONE_SECOND_MS);
        } else {
            this.configuration.setTemplateUpdateDelayMilliseconds(Integer.MAX_VALUE);
            this.configuration.setCacheStorage(new MruCacheStorage(STRONG_SIZE_LIMIT, Integer.MAX_VALUE));
        }
    }

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