package io.mangoo.templating.freemarker.directives;

import java.io.IOException;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import io.mangoo.routing.bindings.Session;

/**
 *
 * @author svenkubiak
 *
 */
@SuppressWarnings("rawtypes")
public class AuthenticityTokenDirective implements TemplateDirectiveModel {
    private final Session session;

    public AuthenticityTokenDirective(Session session) {
        this.session = session;
    }

    @Override
    public void execute(Environment environment, Map params, TemplateModel[] loopVars, TemplateDirectiveBody templateDirectiveBody) throws TemplateException, IOException {
        if (this.session != null) {
            environment.getOut().append(this.session.getAuthenticity());
        }
    }
}