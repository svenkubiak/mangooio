package io.mangoo.providers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.templating.TemplateEngine;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class TemplateEngineProvider implements Provider<TemplateEngine> {
    private static final Logger LOG = LogManager.getLogger(TemplateEngineProvider.class);
    private TemplateEngine templateEngine;

    @Inject
    public TemplateEngineProvider(Injector injector, Config config) {
        Class<? extends TemplateEngine> templateEngineClass = null; 
        try {
            templateEngineClass = Class.forName(config.getTemplateEngineClass()).asSubclass(TemplateEngine.class);
        } catch (ClassNotFoundException e) {
            LOG.error("Can not find Template Engine class: " + config.getTemplateEngineClass(), e);
        }
        
        if (templateEngineClass != null) {
            this.templateEngine = injector.getInstance(templateEngineClass);
            LOG.info("Using {} as implementation for Template Engine",  templateEngineClass);
        }
    }
    
    @Override
    public TemplateEngine get() {
        return this.templateEngine;
    }
}