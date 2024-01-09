package io.mangoo.templating.methods;

import freemarker.template.*;
import io.mangoo.i18n.Messages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

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
            var messageKey = ((SimpleScalar) arguments.getFirst()).getAsString();
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

            var messageKey = strings.getFirst();
            strings.removeFirst();
            messageValue = messages.get(messageKey, strings.toArray());
        } else {
            LOG.warn("Invalid number of arguments for i18n");
        }

        return new SimpleScalar(messageValue);
    }
}