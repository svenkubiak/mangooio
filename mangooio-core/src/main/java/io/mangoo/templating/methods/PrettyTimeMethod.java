package io.mangoo.templating.methods;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.ocpsoft.prettytime.PrettyTime;

import freemarker.template.SimpleDate;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import io.mangoo.enums.Required;
import io.mangoo.utils.DateUtils;
import no.api.freemarker.java8.time.LocalDateAdapter;
import no.api.freemarker.java8.time.LocalDateTimeAdapter;
import no.api.freemarker.java8.time.TemporalDialerAdapter;

@SuppressWarnings("rawtypes")
public class PrettyTimeMethod implements TemplateMethodModelEx {
    private static final int NUM_ARGUMENTS = 1;
    private PrettyTime prettyTime;
    
    public PrettyTimeMethod (Locale locale) {
        Objects.requireNonNull(locale, Required.LOCALE.toString());
        this.prettyTime = new PrettyTime(locale);
    }

    @Override
    public String exec(List arguments) throws TemplateModelException {
        String result = null;
        if (arguments != null && arguments.size() == NUM_ARGUMENTS) {
            Object temporal = null;
            Object object = arguments.get(0);
            
            if (object != null) {
                if (object instanceof TemporalDialerAdapter) {
                    TemporalDialerAdapter temporalDialerAdapter = (TemporalDialerAdapter) object;
                    temporal = temporalDialerAdapter.getObject();   
                }
            }

            if (temporal != null) {
                object = temporal;
            }
            
            if (object instanceof LocalDateTimeAdapter) {
                Date date = DateUtils.localDateTimeToDate(((LocalDateTimeAdapter) object).getObject());
                result = this.prettyTime.format(date);
            } else if (object instanceof LocalDateAdapter) {
                Date date = DateUtils.localDateToDate(((LocalDateAdapter) object).getObject());
                result = this.prettyTime.format(date);              
            } else if (object instanceof LocalDateTime) {
                Date date = DateUtils.localDateTimeToDate((LocalDateTime) object);
                result = this.prettyTime.format(date);              
            } else if (object instanceof LocalDate) {
                Date date = DateUtils.localDateToDate(((LocalDate) object));
                result = this.prettyTime.format(date);   
            } else if (object instanceof SimpleDate) {
                Date date = ((SimpleDate) object).getAsDate();
                result = this.prettyTime.format(date);   
            } else if (object instanceof Date) {
                result = this.prettyTime.format((Date) object);   
            } else {
                throw new TemplateModelException("Invalid object found for prettytime function. Must be of type: SimpleDate, Date, LocalDateTime or LocalDate - Is: " + object.getClass());
            }

            
        } else if (arguments != null && arguments.size() > NUM_ARGUMENTS) {
            throw new TemplateModelException("Too many arguments for prettytime function. Allowed arguments: " + NUM_ARGUMENTS);
        } else {
            throw new TemplateModelException("Invalid number of arguments passed to prettytime function.");
        }
        
        return result;
    }
}