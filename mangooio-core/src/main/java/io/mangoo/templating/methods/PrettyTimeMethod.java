package io.mangoo.templating.methods;

import freemarker.template.SimpleDate;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import io.mangoo.constants.NotNull;
import io.mangoo.utils.DateUtils;
import no.api.freemarker.java8.time.LocalDateAdapter;
import no.api.freemarker.java8.time.LocalDateTimeAdapter;
import no.api.freemarker.java8.time.TemporalDialerAdapter;
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
        Objects.requireNonNull(locale, NotNull.LOCALE);
        this.prettyTime = new PrettyTime(locale);
    }

    @Override
    public String exec(List arguments) throws TemplateModelException {
        String result = null;
        if (arguments != null && arguments.size() == NUM_ARGUMENTS) {
            Object temporal = null;
            Object object = arguments.getFirst();
            
            if (object instanceof TemporalDialerAdapter temporalDialerAdapter) {
                temporal = temporalDialerAdapter.getObject();   
            }

            if (temporal != null) {
                object = temporal;
            }

            switch (object) {
                case LocalDateTimeAdapter localDateTimeAdapter -> {
                    Date date = DateUtils.localDateTimeToDate(localDateTimeAdapter.getObject());
                    result = prettyTime.format(date);
                }
                case LocalDateAdapter localDateAdapter -> {
                    Date date = DateUtils.localDateToDate(localDateAdapter.getObject());
                    result = prettyTime.format(date);
                }
                case LocalDateTime localDateTime -> {
                    Date date = DateUtils.localDateTimeToDate(localDateTime);
                    result = prettyTime.format(date);
                }
                case LocalDate localDate -> {
                    Date date = DateUtils.localDateToDate(localDate);
                    result = prettyTime.format(date);
                }
                case SimpleDate simpleDate -> {
                    Date date = simpleDate.getAsDate();
                    result = prettyTime.format(date);
                }
                case Date date -> result = prettyTime.format(date);
                case null, default ->
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