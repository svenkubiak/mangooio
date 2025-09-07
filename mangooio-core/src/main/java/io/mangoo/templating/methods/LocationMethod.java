package io.mangoo.templating.methods;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import io.mangoo.constants.Required;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@SuppressWarnings("rawtypes")
public class LocationMethod implements TemplateMethodModelEx {
   private static final int NUM_ARGUMENTS = 1;
   private final String controller;

    public LocationMethod(String path) {
        Objects.requireNonNull(path, Required.PATH);
        this.controller = path.toLowerCase(Locale.ENGLISH);
    }

    @Override
    public Boolean exec(List arguments) throws TemplateModelException {
        return validArguments(arguments) && matches(arguments);
    }
    
    private boolean validArguments(List arguments) {
        return arguments != null && arguments.size() == NUM_ARGUMENTS && StringUtils.isNotBlank(this.controller);
    }
    
    private boolean matches(List arguments) {
        String route = ((SimpleScalar) arguments.getFirst()).getAsString().toLowerCase(Locale.ENGLISH);
        boolean equals = controller.equalsIgnoreCase(route);
        
        if (equals) {
            return true;
        } else if (!route.contains(":")){
            return route.equalsIgnoreCase(StringUtils.substringBefore(controller, ":"));
        }
        
        return false;
    }
}