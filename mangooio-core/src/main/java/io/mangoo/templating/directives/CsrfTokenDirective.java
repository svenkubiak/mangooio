package io.mangoo.templating.directives;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import io.mangoo.routing.bindings.Session;

import java.io.IOException;
import java.util.Map;

public class CsrfTokenDirective implements TemplateDirectiveModel {
    private final Session session;

    public CsrfTokenDirective(Session session) {
        this.session = session;
    }

    @Override
    public void execute(Environment environment, Map params, TemplateModel[] loopVars, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
        if (session != null) {
            environment.getOut().append(session.getCsrf());
        }
    }
}