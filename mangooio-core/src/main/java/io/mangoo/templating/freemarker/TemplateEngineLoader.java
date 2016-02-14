package io.mangoo.templating.freemarker;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.apache.commons.io.IOUtils;

import freemarker.cache.TemplateLoader;

/**
 *
 * @author svenkubiak
 *
 */
public class TemplateEngineLoader implements TemplateLoader {
    public static final String ESCAPE_PREFIX = "<#ftl strip_whitespace=true><#escape x as x?html>";
    public static final String ESCAPE_SUFFIX = "</#escape>";
    private final TemplateLoader delegate;

    public TemplateEngineLoader(TemplateLoader delegate) {
        this.delegate = delegate;
    }

    @Override
    public Object findTemplateSource(String name) throws IOException {
        return delegate.findTemplateSource(name);
    }

    @Override
    public long getLastModified(Object templateSource) {
        return delegate.getLastModified(templateSource);
    }

    @Override
    public Reader getReader(Object templateSource, String encoding) throws IOException {
        try (Reader reader = delegate.getReader(templateSource, encoding)) {
            String templateText = IOUtils.toString(reader);
            return new StringReader(ESCAPE_PREFIX + templateText + ESCAPE_SUFFIX);
        } 
    }

    @Override
    public void closeTemplateSource(Object templateSource) throws IOException {
        delegate.closeTemplateSource(templateSource);
    }
}