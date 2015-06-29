package mangoo.io.templating;

import io.undertow.server.HttpServerExchange;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mangoo.io.core.Application;
import mangoo.io.enums.Default;
import mangoo.io.i18n.Messages;
import mangoo.io.routing.bindings.Flash;
import mangoo.io.routing.bindings.Form;
import mangoo.io.routing.bindings.Session;
import mangoo.io.templating.directives.AuthenticityFormDirective;
import mangoo.io.templating.directives.AuthenticityTokenDirective;
import mangoo.io.templating.methods.I18nMethod;
import mangoo.io.utils.Source;
import mangoo.io.utils.ThrowableUtils;

import org.apache.commons.lang.exception.ExceptionUtils;

import com.google.common.base.Charsets;
import com.google.inject.Singleton;

import freemarker.cache.MruCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.Version;

@Singleton
public class TemplateEngine {
    private static final int ONE_SECOND = 1;
    private static final int STRONG_SIZE_LIMIT = 20;
    private static final Version VERSION = new Version(2, 3, 22);
    private Configuration configuration = new Configuration(VERSION);
    private String baseDirectory;

    public TemplateEngine() {
        this.configuration.setClassForTemplateLoading(this.getClass(), Default.TEMPLATES_FOLDER.toString());
        this.configuration.setDefaultEncoding(Charsets.UTF_8.name());
        this.configuration.setOutputEncoding(Charsets.UTF_8.name());
        this.configuration.setLocalizedLookup(false);
        this.configuration.setNumberFormat("0.######");
        this.configuration.setTemplateLoader(new TemplateEngineLoader(configuration.getTemplateLoader()));

        if (Application.inDevMode()) {
            this.configuration.setTemplateUpdateDelay(ONE_SECOND);
        } else {
            this.configuration.setTemplateUpdateDelay(Integer.MAX_VALUE);
            this.configuration.setCacheStorage(new MruCacheStorage(STRONG_SIZE_LIMIT, Integer.MAX_VALUE));
        }

        StringBuilder buffer = new StringBuilder();
        buffer.append(System.getProperty("user.dir")).append(File.separator)
              .append("src").append(File.separator).append("main")
              .append(File.separator).append("java");

        this.baseDirectory = buffer.toString();
    }

    @SuppressWarnings("all")
    public String render(Flash flash, Session session, Form form, Messages messages, String pathPrefix, String templateName, Map<String, Object> content) throws Exception {
        Template template = configuration.getTemplate(pathPrefix + "/" + getTemplateName(templateName));
        content.put("form", form);
        content.put("flash", flash);
        content.put("session", session);
        content.put("i18n", new I18nMethod(messages));
        content.put("authenticityToken", new AuthenticityTokenDirective(session));
        content.put("authenticityForm", new AuthenticityFormDirective(session));

        return processTemplate(content, template);
    }

    @SuppressWarnings("all")
    public String render(String pathPrefix, String templateName, Map<String, Object> content) throws Exception {
        Template template = configuration.getTemplate(pathPrefix + "/" + getTemplateName(templateName));

        return processTemplate(content, template);
    }

    @SuppressWarnings("all")
    public String renderException(HttpServerExchange exchange, Throwable cause) throws Exception {
        Writer writer = new StringWriter();
        Map<String, Object> content = new HashMap<String, Object>();

        StackTraceElement stackTraceElement = Arrays.asList(cause.getStackTrace()).get(0);
        String sourceCodePath = ThrowableUtils.getSourceCodePath(stackTraceElement);

        List<Source> sources = ThrowableUtils.getSources(stackTraceElement.getLineNumber(), sourceCodePath);
        content.put("sources", sources);
        content.put("cause", ExceptionUtils.getMessage(cause));
        content.put("url", exchange.getRequestURI());
        content.put("method", exchange.getRequestMethod());
        content.put("line", stackTraceElement.getLineNumber());
        content.put("sourceCodePath", new File(this.baseDirectory).toPath().resolve(sourceCodePath).toFile().getAbsolutePath());

        Configuration config = new Configuration(VERSION);
        config.setClassForTemplateLoading(this.getClass(), "/defaults/");

        Template template = config.getTemplate("exception.ftl");
        template.process(content, writer);

        return writer.toString();
    }

    private String getTemplateName(String templateName) {
        String name = null;
        if (templateName.endsWith(Default.TEMPLATE_SUFFIX.toString())) {
            name = templateName;
        } else {
            name = templateName + Default.TEMPLATE_SUFFIX.toString();
        }

        return name;
    }

    private String processTemplate(Map<String, Object> content, Template template) throws TemplateException, IOException {
        StringWriter buffer = new StringWriter(65536);
        template.process(content, buffer);

        Writer writer = new StringWriter();
        writer.write(buffer.toString());
        writer.close();

        return buffer.toString();
    }
}