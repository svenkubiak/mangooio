package io.mangoo.templating.freemarker.methods;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

@SuppressWarnings("rawtypes")
public class LocationMethod implements TemplateMethodModelEx {
   private static final int NUM_ARGUMENTS = 1;
   private String path;

    public LocationMethod(String path) {
        this.path = path;
    }

    @Override
    public Boolean exec(List arguments) throws TemplateModelException {
        boolean valid = false;
        if (arguments != null && arguments.size() == NUM_ARGUMENTS && StringUtils.isNotBlank(this.path)) {
            if (this.path.equalsIgnoreCase(((SimpleScalar) arguments.get(0)).getAsString())) {
                valid = true;
            }
        }
        
        return Boolean.valueOf(valid);
    }
}