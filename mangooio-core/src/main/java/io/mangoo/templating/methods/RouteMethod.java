package io.mangoo.templating.methods;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.re2j.Pattern;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.mangoo.routing.Router;

public class RouteMethod implements TemplateMethodModelEx {
    private static final Pattern PARAMETER_PATTERN = Pattern.compile("\\{(.*?)\\}");
    private static final int MIN_ARGUMENTS = 1;

    @Override
    @SuppressWarnings("rawtypes")
    public TemplateModel exec(List arguments) throws TemplateModelException {
        String url;
        if (arguments.size() >= MIN_ARGUMENTS) {
            var controller = ((SimpleScalar) arguments.get(0)).getAsString();
            var requestRoute = Router.getReverseRoute(controller);
            
            if (requestRoute != null) {
                url = requestRoute.getUrl();
                var matcher = PARAMETER_PATTERN.matcher(url);
                var i = 1;
                while (matcher.find()) {
                    var argument = ((SimpleScalar) arguments.get(i)).getAsString();
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