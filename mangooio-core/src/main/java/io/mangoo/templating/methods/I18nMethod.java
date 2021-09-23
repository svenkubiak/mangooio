package io.mangoo.templating.methods;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import freemarker.template.SimpleNumber;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import io.mangoo.i18n.Messages;

/**
 *
 * @author svenkubiak
 *
 */
public class I18nMethod implements TemplateMethodModelEx {
    private static final Logger LOG = LogManager.getLogger(I18nMethod.class);
    private static final int NUM_ARGUMENTS = 1;
    private final Messages messages;

    public I18nMethod(Messages messages) {
        this.messages = messages;
    }

    @Override
    public TemplateModel exec(List arguments) throws TemplateModelException {
        var messageValue = "";
        if (arguments.size() == NUM_ARGUMENTS) {
            String messageKey = ((SimpleScalar) arguments.get(0)).getAsString();
            messageValue = messages.get(messageKey);

        } else if (arguments.size() > NUM_ARGUMENTS) {
            List<String> strings = new ArrayList<>();
            for (Object object : arguments) {
                if (object instanceof SimpleScalar scalar) {
                    strings.add(scalar.getAsString());
                } else if (object instanceof SimpleNumber number) {
                    strings.add(number.toString());
                } else {
                    LOG.warn("Argument can only be of type SimpleScalar or SimpleNumber. Is: {}", object.getClass());
                }
            }

            var messageKey = strings.get(0);
            strings.remove(0);
            messageValue = messages.get(messageKey, strings.toArray());
        } else {
            LOG.warn("Invalid number of arguments for i18n");
        }

        return new SimpleScalar(messageValue);
    }
}