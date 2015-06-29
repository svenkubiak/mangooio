package mangoo.io.templating.methods;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import mangoo.io.i18n.Messages;

@SuppressWarnings("rawtypes")
public class I18nMethod implements TemplateMethodModelEx {
    private static final Logger LOG = LoggerFactory.getLogger(I18nMethod.class);
    private static final int NUM_ARGUMENTS = 1;
    private Messages messages;

    public I18nMethod(Messages messages) {
        this.messages = messages;
    }

    @Override
    public TemplateModel exec(List args) throws TemplateModelException {
        String messageValue = "";
        if (args.size() == NUM_ARGUMENTS) {
            String messageKey = ((SimpleScalar) args.get(0)).getAsString();
            messageValue = messages.get(messageKey);

            logError(messageKey, messageValue);
        } else if (args.size() > NUM_ARGUMENTS) {
            List<String> strings = new ArrayList<String>();
            for (Object object : args) {
                if (object instanceof SimpleScalar) {
                    strings.add(((SimpleScalar) object).getAsString());
                } else if (object instanceof SimpleNumber) {
                    strings.add(((SimpleNumber) object).toString());
                }
            }

            String messageKey = strings.get(0);
            strings.remove(0);
            messageValue = messages.get(messageKey, strings.toArray());

            logError(messageKey, messageValue);
        }

        return new SimpleScalar(messageValue);
    }

    public void logError(String messageKey, String messageValue) {
        if (messageKey.equals(messageValue)) {
            LOG.error("Message key {} missing. Using key as value inside template - but this is most likely not what you want.", messageKey);
        }
    }
}