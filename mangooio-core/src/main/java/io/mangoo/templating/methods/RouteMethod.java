package io.mangoo.templating.methods;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.mangoo.routing.Route;
import io.mangoo.routing.Router;

/**
 *
 * @author svenkubiak
 *
 */
@SuppressWarnings("rawtypes")
public class RouteMethod implements TemplateMethodModelEx {
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{(.*?)\\}");
    private static final int MIN_ARGUMENTS = 1;

    @Override
    public TemplateModel exec(List arguments) throws TemplateModelException {
        String url;
        if (arguments.size() >= MIN_ARGUMENTS) {
            String controller = ((SimpleScalar) arguments.get(0)).getAsString();
            Route route = Router.getReverseRoute(controller);
            if (route != null) {
                url = route.getUrl();
                Matcher matcher = PARAMETER_PATTERN.matcher(url);
                int i = 1;
                while (matcher.find()) {
                    String argument = ((SimpleScalar) arguments.get(i)).getAsString();
                    url = StringUtils.replace(url, "{" + matcher.group(1) + "}", argument);
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