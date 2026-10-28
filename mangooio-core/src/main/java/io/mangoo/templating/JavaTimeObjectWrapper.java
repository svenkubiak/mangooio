package io.mangoo.templating;

import freemarker.core.Environment;
import freemarker.ext.beans.BeanModel;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.*;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalAmount;
import java.util.List;
import java.util.Locale;

/**
 * FreeMarker 2.3 has no java.time support. This wrapper keeps those values printable
 * and exposes {@code format(pattern)} with a {@link DateTimeFormatter} pattern.
 */
class JavaTimeObjectWrapper extends DefaultObjectWrapper {

    JavaTimeObjectWrapper(Version incompatibleImprovements) {
        super(incompatibleImprovements);
    }

    @Override
    protected TemplateModel handleUnknownType(Object obj) throws TemplateModelException {
        if (obj instanceof TemporalAccessor || obj instanceof TemporalAmount || obj instanceof Clock || obj instanceof ZoneId) {
            return new JavaTimeTemplateModel(obj, this);
        }
        return super.handleUnknownType(obj);
    }

    private static final class JavaTimeTemplateModel implements TemplateScalarModel, TemplateHashModel, AdapterTemplateModel {
        private final Object value;
        private final BeanModel beanModel;

        private JavaTimeTemplateModel(Object value, BeansWrapper wrapper) {
            this.value = value;
            this.beanModel = new BeanModel(value, wrapper);
        }

        @Override
        public String getAsString() {
            return value.toString();
        }

        @Override
        public TemplateModel get(String key) throws TemplateModelException {
            if ("format".equals(key) && value instanceof TemporalAccessor temporal) {
                return new FormatMethod(temporal);
            }
            return beanModel.get(key);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public Object getAdaptedObject(Class<?> hint) {
            return value;
        }
    }

    private static final class FormatMethod implements TemplateMethodModelEx {
        private static final int MAX_ARGUMENTS = 1;
        private final TemporalAccessor temporal;

        private FormatMethod(TemporalAccessor temporal) {
            this.temporal = temporal;
        }

        @Override
        public Object exec(List arguments) throws TemplateModelException {
            if (arguments != null && arguments.size() > MAX_ARGUMENTS) {
                throw new TemplateModelException("format accepts one pattern argument");
            }
            if (arguments == null || arguments.isEmpty()) {
                return temporal.toString();
            }

            Object argument = arguments.getFirst();
            if (!(argument instanceof TemplateScalarModel scalar)) {
                throw new TemplateModelException("format pattern must be a string");
            }

            var pattern = scalar.getAsString();
            try {
                return DateTimeFormatter.ofPattern(pattern, locale()).format(temporal);
            } catch (IllegalArgumentException | DateTimeException e) {
                throw new TemplateModelException("Failed to format temporal with pattern: " + pattern, e);
            }
        }

        private static Locale locale() {
            var environment = Environment.getCurrentEnvironment();
            if (environment != null && environment.getLocale() != null) {
                return environment.getLocale();
            }
            return Locale.getDefault();
        }
    }
}
