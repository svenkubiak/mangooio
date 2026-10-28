package io.mangoo.templating.methods;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateDateModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import io.mangoo.constants.Required;
import io.mangoo.utils.DateUtils;
import org.ocpsoft.prettytime.PrettyTime;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PrettyTimeMethod implements TemplateMethodModelEx {
    private static final int NUM_ARGUMENTS = 1;
    private final PrettyTime prettyTime;
    
    public PrettyTimeMethod (Locale locale) {
        Objects.requireNonNull(locale, Required.LOCALE);
        this.prettyTime = new PrettyTime(locale);
    }

    @Override
    @SuppressWarnings("java:S6212")
    public String exec(List arguments) throws TemplateModelException {
        String result = null;
        if (arguments != null && arguments.size() == NUM_ARGUMENTS) {
            Object object = arguments.getFirst();
            if (object instanceof AdapterTemplateModel adapter) {
                object = adapter.getAdaptedObject(Object.class);
            } else if (object instanceof TemplateDateModel dateModel) {
                object = dateModel.getAsDate();
            }

            switch (object) {
                case LocalDateTime localDateTime -> {
                    Date date = DateUtils.localDateTimeToDate(localDateTime);
                    result = prettyTime.format(date);
                }
                case LocalDate localDate -> {
                    Date date = DateUtils.localDateToDate(localDate);
                    result = prettyTime.format(date);
                }
                case Date date -> result = prettyTime.format(date);
                case null, default ->
                        throw new TemplateModelException("Invalid object found for prettytime function. Must be of type: Date, LocalDateTime or LocalDate - Is: " + (object == null ? "null" : object.getClass().getName()));
            }
            
        } else if (arguments != null && arguments.size() > NUM_ARGUMENTS) {
            throw new TemplateModelException("Too many arguments for prettytime function. Allowed arguments: " + NUM_ARGUMENTS);
        } else {
            throw new TemplateModelException("Invalid number of arguments passed to prettytime function.");
        }
        
        return result;
    }
}