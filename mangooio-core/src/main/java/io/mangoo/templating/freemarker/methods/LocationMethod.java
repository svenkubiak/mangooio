package io.mangoo.templating.freemarker.methods;

import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import io.mangoo.enums.Required;

@SuppressWarnings("rawtypes")
public class LocationMethod implements TemplateMethodModelEx {
   private static final int NUM_ARGUMENTS = 1;
   private String path;

    public LocationMethod(String path) {
        Objects.requireNonNull(path, Required.PATH.toString());
        this.path = path.toLowerCase();
    }

    @Override
    public Boolean exec(List arguments) throws TemplateModelException {
        boolean valid = false;
        if (validArguments(arguments) && pathMatches(arguments)) {
            valid = true;
        }
        
        return Boolean.valueOf(valid);
    }
    
    private boolean validArguments(List arguments) {
        return arguments != null && arguments.size() == NUM_ARGUMENTS && StringUtils.isNotBlank(this.path);
    }
    
    private boolean pathMatches(List arguments) {
        return this.path.startsWith(((SimpleScalar) arguments.get(0)).getAsString().toLowerCase());
    }
}