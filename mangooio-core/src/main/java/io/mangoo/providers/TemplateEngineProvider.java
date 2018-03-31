package io.mangoo.providers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;

import io.mangoo.configuration.Config;
import io.mangoo.interfaces.MangooTemplateEngine;

/**
 * 
 * @author svenkubiak
 *
 */
@Singleton
public class TemplateEngineProvider implements Provider<MangooTemplateEngine> {
    private static final Logger LOG = LogManager.getLogger(TemplateEngineProvider.class);
    private MangooTemplateEngine templateEngine;

    @Inject
    public TemplateEngineProvider(Injector injector, Config config) {
        Class<? extends MangooTemplateEngine> templateEngineClass = null; 
        try {
            templateEngineClass = Class.forName(config.getApplicationTemplateEngine()).asSubclass(MangooTemplateEngine.class);
        } catch (ClassNotFoundException e) {
            LOG.error("Could not find Template Engine class: " + config.getApplicationTemplateEngine(), e);
        }
        
        if (templateEngineClass != null) {
            this.templateEngine = injector.getInstance(templateEngineClass);
            LOG.info("Using {} as implementation for Template Engine",  templateEngineClass);
        }
    }
    
    @Override
    public MangooTemplateEngine get() {
        return this.templateEngine;
    }
}