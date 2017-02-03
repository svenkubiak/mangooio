package io.mangoo.templating.freemarker.methods;

import java.util.Date;
import java.util.List;

import org.ocpsoft.prettytime.PrettyTime;

import freemarker.template.SimpleDate;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;
import io.mangoo.utils.DateUtils;
import no.api.freemarker.java8.time.LocalDateAdapter;
import no.api.freemarker.java8.time.LocalDateTimeAdapter;

@SuppressWarnings("rawtypes")
public class PrettyTimeMethod implements TemplateMethodModelEx {
    private static final int NUM_ARGUMENTS = 1;
    private PrettyTime prettyTime = new PrettyTime();

    @Override
    public String exec(List arguments) throws TemplateModelException {
        String result = "";
        if (arguments != null && arguments.size() == NUM_ARGUMENTS) {
            Object object = arguments.get(0);
            if (object instanceof SimpleDate) {
                Date date = ((SimpleDate) object).getAsDate();
                result = this.prettyTime.format(date);
            } else if (object instanceof LocalDateTimeAdapter) {
                Date date = DateUtils.localDateTimeToDate(((LocalDateTimeAdapter) object).getObject());
                result = this.prettyTime.format(date);
            } else if (object instanceof LocalDateAdapter) {
                Date date = DateUtils.localDateToDate(((LocalDateAdapter) object).getObject());
                result = this.prettyTime.format(date);              
            } else {
                throw new TemplateModelException("Invalid object found for pretty time. Must be of type: SimpleDate, Date, LocalDateTime or LocalDate - Is: " + object.getClass());
            }
        }
        
        return result;
    }
}