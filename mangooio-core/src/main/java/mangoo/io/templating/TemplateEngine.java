package mangoo.io.templating;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mangoo.io.core.Application;
import mangoo.io.i18n.Messages;
import mangoo.io.routing.bindings.Flash;
import mangoo.io.routing.bindings.Session;
import mangoo.io.templating.directives.AuthenticityFormDirective;
import mangoo.io.templating.directives.AuthenticityTokenDirective;
import mangoo.io.templating.methods.I18nMethod;
import mangoo.io.utils.ExceptionUtils;
import mangoo.io.utils.Source;

import com.google.common.base.Charsets;
import com.google.inject.Singleton;

import freemarker.cache.MruCacheStorage;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.Version;

@Singleton
public class TemplateEngine {
    private static final int STRONG_SIZE_LIMIT = 20;
    private static final Version VERSION = new Version(2, 3, 22);
    private Configuration configuration = new Configuration(VERSION);
    private String baseDirectory;

    public TemplateEngine() {
        this.configuration.setClassForTemplateLoading(this.getClass(), "/templates/");
        this.configuration.setDefaultEncoding(Charsets.UTF_8.name());
        this.configuration.setOutputEncoding(Charsets.UTF_8.name());
        this.configuration.setLocalizedLookup(false);
        this.configuration.setNumberFormat("0.######");
        this.configuration.setTemplateLoader(new TemplateEngineLoader(configuration.getTemplateLoader()));

        if (Application.inDevMode()) {
            this.configuration.setTemplateUpdateDelay(1);
        } else {
            this.configuration.setTemplateUpdateDelay(Integer.MAX_VALUE);
            this.configuration.setCacheStorage(new MruCacheStorage(STRONG_SIZE_LIMIT, Integer.MAX_VALUE));
        }
        
        StringBuilder buffer = new StringBuilder();
        buffer.append(System.getProperty("user.dir"))
            .append(File.separator)
            .append("src")
            .append(File.separator)
            .append("main")
            .append(File.separator)
            .append("java");
        
        this.baseDirectory = buffer.toString();
    }

    @SuppressWarnings("all")
    public String render(Flash flash, Session session, Messages messages, String pathPrefix, String templateName, Map<String, Object> content) throws Exception {
        String name = null;
        if (templateName.endsWith(".ftl")) {
            name = templateName;            
        } else {
            name = templateName + ".ftl";
        }

        Template template = configuration.getTemplate(pathPrefix + "/" + name);
        content.put("flash", flash);
        content.put("session", session);
        content.put("i18n", new I18nMethod(messages));
        content.put("authenticityToken", new AuthenticityTokenDirective(session));
        content.put("authenticityForm", new AuthenticityFormDirective(session));

        StringWriter buffer = new StringWriter(65536);
        template.process(content, buffer);

        Writer writer = new StringWriter();
        writer.write(buffer.toString());
        writer.close();

        return buffer.toString();
    }

    @SuppressWarnings("all")
    public String renderException(List<StackTraceElement> exception) throws Exception {
        Writer writer = new StringWriter();
        Map<String, Object> content = new HashMap<String, Object>();

        StackTraceElement stackTraceElement = exception.get(0);
        String sourceCodePath = ExceptionUtils.getSourceCodePath(stackTraceElement);

        List<Source> sources = ExceptionUtils.getSources(stackTraceElement.getLineNumber(), sourceCodePath);
        content.put("sources", sources);
        content.put("line", stackTraceElement.getLineNumber());
        content.put("sourceCodePath", new File(this.baseDirectory).toPath().resolve(sourceCodePath).toFile().getAbsolutePath());

        Configuration config = new Configuration(VERSION);
        config.setClassForTemplateLoading(this.getClass(), "/defaults/");

        Template template = config.getTemplate("exception.ftl");
        template.process(content, writer);

        return writer.toString();
    }
}