package io.mangoo.templating.freemarker.methods;

import java.util.ArrayList;
import java.util.List;

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
@SuppressWarnings("rawtypes")
public class I18nMethod implements TemplateMethodModelEx {
    private static final int NUM_ARGUMENTS = 1;
    private final Messages messages;

    public I18nMethod(Messages messages) {
        this.messages = messages;
    }

    @Override
    public TemplateModel exec(List args) throws TemplateModelException {
        String messageValue = "";
        if (args.size() == NUM_ARGUMENTS) {
            String messageKey = ((SimpleScalar) args.get(0)).getAsString();
            messageValue = messages.get(messageKey);

        } else if (args.size() > NUM_ARGUMENTS) {
            List<String> strings = new ArrayList<>();
            for (Object object : args) {
                if (object instanceof SimpleScalar) {
                    strings.add(((SimpleScalar) object).getAsString());
                } else if (object instanceof SimpleNumber) {
                    strings.add(object.toString());
                }
            }

            String messageKey = strings.get(0);
            strings.remove(0);
            messageValue = messages.get(messageKey, strings.toArray());
        }

        return new SimpleScalar(messageValue);
    }
}