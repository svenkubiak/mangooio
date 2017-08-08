package io.mangoo.templating.methods;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import io.mangoo.enums.Required;

@SuppressWarnings("rawtypes")
public class LocationMethod implements TemplateMethodModelEx {
   private static final int NUM_ARGUMENTS = 1;
   private String controller;

    public LocationMethod(String path) {
        Objects.requireNonNull(path, Required.PATH.toString());
        this.controller = path.toLowerCase(Locale.ENGLISH);
    }

    @Override
    public Boolean exec(List arguments) throws TemplateModelException {
        boolean valid = false;
        if (validArguments(arguments) && matches(arguments)) {
            valid = true;
        }
        
        return Boolean.valueOf(valid);
    }
    
    private boolean validArguments(List arguments) {
        return arguments != null && arguments.size() == NUM_ARGUMENTS && StringUtils.isNotBlank(this.controller);
    }
    
    private boolean matches(List arguments) {
        return this.controller.equalsIgnoreCase(((SimpleScalar) arguments.get(0)).getAsString().toLowerCase(Locale.ENGLISH));
    }
}