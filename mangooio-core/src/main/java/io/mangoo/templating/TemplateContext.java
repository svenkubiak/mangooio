package io.mangoo.templating;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.mangoo.i18n.Messages;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.templating.directives.FormDirective;
import io.mangoo.templating.directives.TokenDirective;
import io.mangoo.templating.methods.I18nMethod;
import io.mangoo.templating.methods.LocationMethod;
import io.mangoo.templating.methods.PrettyTimeMethod;
import io.mangoo.templating.methods.RouteMethod;

/**
 * 
 * @author svenkubiak
 *
 */
public class TemplateContext {
    private String templatePath;
    private Map<String, Object> content = new HashMap<>();
    
    public TemplateContext(Map<String, Object> content) {
        this.content = content;
        this.content.put("route", new RouteMethod());
    }
    
    public TemplateContext withForm(Form form) {
        this.content.put("form", form);
        return this;
    }
    
    public TemplateContext withFlash(Flash flash) {
        this.content.put("flash", flash);
        return this;
    }
    
    public TemplateContext withSession(Session session) {
        this.content.put("session", session);
        return this;
    }
    
    public TemplateContext withMessages(Messages messages) {
        this.content.put("i18n", new I18nMethod(messages));
        return this;
    }
    
    public TemplateContext withController(String controller) {
        this.content.put("location", new LocationMethod(controller));
        return this;
    }
    
    public TemplateContext withPrettyTime(Locale locale) {
        this.content.put("prettytime", new PrettyTimeMethod(locale));
        return this;
    }
    
    public TemplateContext withAuthenticity(Session session) {
        this.content.put("authenticity", new TokenDirective(session));
        return this;
    }
    
    public TemplateContext withAuthenticityForm(Session session) {
        this.content.put("authenticityForm", new FormDirective(session));
        return this;
    }
    
    public TemplateContext withTemplatePath(String path) {
        this.templatePath = path;
        return this;
    }
    
    public TemplateContext addContent(String name, Object object) {
        this.content.put(name, object);
        return this;
    }
    
    public String getTemplatePath() {
        return this.templatePath;
    }

    public Map<String, Object> getContent() {
        return this.content;
    }
}