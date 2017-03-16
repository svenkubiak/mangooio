package io.mangoo.templating.freemarker.methods;

import java.util.List;
import java.util.regex.Matcher;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.mangoo.routing.Route;
import io.mangoo.routing.Router;
import io.mangoo.utils.TemplateUtils;

/**
 *
 * @author svenkubiak
 *
 */
@SuppressWarnings("rawtypes")
public class RouteMethod implements TemplateMethodModelEx {
    private static final int MIN_ARGUMENTS = 1;

    @Override
    public TemplateModel exec(List arguments) throws TemplateModelException {
        String url = "";
        if (arguments.size() >= MIN_ARGUMENTS) {
            String controller = ((SimpleScalar) arguments.get(0)).getAsString();
            Route route = Router.getReverseRoute(controller);
            if (route != null) {
                url = route.getUrl();
                Matcher matcher = TemplateUtils.PARAMETER_PATTERN.matcher(url);
                int i = 1;
                while (matcher.find()) {
                    String argument = ((SimpleScalar) arguments.get(i)).getAsString();
                    url = url.replace("{" + matcher.group(1) + "}", argument); 
                    i++;
                }
            } else {
                throw new TemplateModelException("Reverse route for " + controller + " could not be found!");
            }
        } else {
            throw new TemplateModelException("Missing at least one argument (ControllerClass:ControllerMethod) for reverse routing!");
        }

        return new SimpleScalar(url);
    }
}