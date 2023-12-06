package io.mangoo.templating;

import io.mangoo.i18n.Messages;
import io.mangoo.routing.bindings.Flash;
import io.mangoo.routing.bindings.Form;
import io.mangoo.routing.bindings.Session;
import io.mangoo.templating.methods.I18nMethod;
import io.mangoo.templating.methods.LocationMethod;
import io.mangoo.templating.methods.PrettyTimeMethod;
import io.mangoo.templating.methods.RouteMethod;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class TemplateContext {
    private String templatePath;
    private Map<String, Object> content = new HashMap<>();
    
    public TemplateContext() {
        content.put("route", new RouteMethod());
    }
    
    public TemplateContext(Map<String, Object> content) {
        this.content = content;
        content.put("route", new RouteMethod());
    }
    
    public TemplateContext withForm(Form form) {
        content.put("form", form);
        return this;
    }
    
    public TemplateContext withFlash(Flash flash) {
        content.put("flash", flash);
        return this;
    }
    
    public TemplateContext withSession(Session session) {
        content.put("session", session);
        return this;
    }
    
    public TemplateContext withMessages(Messages messages) {
        content.put("i18n", new I18nMethod(messages));
        return this;
    }
    
    public TemplateContext withController(String controller) {
        content.put("location", new LocationMethod(controller));
        return this;
    }
    
    public TemplateContext withPrettyTime(Locale locale) {
        content.put("prettytime", new PrettyTimeMethod(locale));
        return this;
    }
    
    public TemplateContext withTemplatePath(String path) {
        templatePath = path;
        return this;
    }
    
    public TemplateContext addContent(String name, Object object) {
        content.put(name, object);
        return this;
    }
    
    public String getTemplatePath() {
        return templatePath;
    }

    public Map<String, Object> getContent() {
        return content;
    }
}