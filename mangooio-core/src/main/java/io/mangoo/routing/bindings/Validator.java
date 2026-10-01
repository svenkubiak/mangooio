package io.mangoo.routing.bindings;

import io.mangoo.constants.Required;
import io.mangoo.constants.Validation;
import io.mangoo.core.Application;
import io.mangoo.i18n.Messages;
import io.mangoo.utils.FileUtils;
import jakarta.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.DomainValidator;
import org.apache.commons.validator.routines.EmailValidator;
import org.apache.commons.validator.routines.InetAddressValidator;
import org.apache.commons.validator.routines.UrlValidator;
import org.apache.logging.log4j.util.Strings;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serial;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;

public class Validator implements Serializable {
    @Serial
    private static final long serialVersionUID = -714400230978999709L;
    private final Map<String, String> errors = new HashMap<>();
    private transient Messages messages; // NOSONAR Transient by design, see messages()
    protected Map<String, String> values = new HashMap<>(); // NOSONAR Intentionally not transient
    protected transient Map<String, byte[]> files = new HashMap<>(); // NOSONAR Transient by design, uploaded file bytes must never be serialized into the flash cookie

    @Inject
    public Validator(Messages messages) {
        this.messages = Objects.requireNonNull(messages, Required.MESSAGES);
    }

    public Validator() {
        this.messages = Application.getInstance(Messages.class);
    }

    // Files are not serialized into the flash cookie, so the map is recreated after a restore from the flash scope.
    protected Map<String, byte[]> files() {
        if (files == null) {
            files = new HashMap<>();
        }

        return files;
    }

    // Messages are not serialized into the flash cookie to save cookie space; the form handler rebinds them,
    // so this fallback only applies to a validator used outside of a request.
    protected Messages messages() {
        if (messages == null) {
            messages = Application.getInstance(Messages.class);
        }

        return messages;
    }

    public Validator withMessages(Messages messages) {
        this.messages = Objects.requireNonNull(messages, Required.MESSAGES);
        return this;
    }

    public void expectFileMimeType(String name, String message, List<String> allowedMimeTypes) {
        Objects.requireNonNull(name, Required.NAME);
        Objects.requireNonNull(allowedMimeTypes, Required.ALLOWED_MIME_TYPES);

        byte[] bytes = files().get(name);
        if (bytes != null) {
            try (var bais = new ByteArrayInputStream(bytes)) {
                String detectedType = FileUtils.getMimeType(bais);

                boolean allowed = allowedMimeTypes.stream()
                        .anyMatch(type -> type.equalsIgnoreCase(detectedType));

                if (!allowed) {
                    addError(name, Optional.ofNullable(message)
                            .orElse(messages().get(Validation.MIME_TYPE_KEY, name)));
                }
            } catch (IOException e) {
                addError(name, Optional.ofNullable(message)
                        .orElse(messages().get(Validation.MIME_TYPE_KEY, name)));
            }
        } else {
            addError(name, Optional.ofNullable(message)
                    .orElse(messages().get(Validation.MIME_TYPE_KEY, name)));
        }
    }

    public void expectFileMimeType(String name, List<String> allowedMimeTypes) {
        expectFileMimeType(name, null, allowedMimeTypes);
    }

    public void expectFileMaxSize(String name, long maxFileSizeBytes, String message) {
        Objects.requireNonNull(name, Required.NAME);

        byte[] bytes = files().get(name);
        if (bytes != null) {
            try {
                long totalSize = bytes.length;
                if (totalSize > maxFileSizeBytes) {
                    addError(name, Optional.ofNullable(message)
                            .orElse(messages().get(Validation.FILE_SIZE_KEY, name)));
                }
            } catch (Exception e) {
                addError(name, Optional.ofNullable(message)
                        .orElse(messages().get(Validation.FILE_SIZE_KEY, name)));
            }
        } else {
            addError(name, Optional.ofNullable(message)
                    .orElse(messages().get(Validation.FILE_SIZE_KEY, name)));
        }
    }

    public void expectFileMaxSize(String name, long maxFileSizeBytes) {
        expectFileMaxSize(name, maxFileSizeBytes, null);
    }

    public boolean hasError(String name) {
        Objects.requireNonNull(name, Required.NAME);
        return errors.containsKey(name);
    }

    public String getError(String name) {
        Objects.requireNonNull(name, Required.NAME);
        return hasError(name) ? errors.get(name) : Strings.EMPTY;
    }

    public void expectValue(String name) {
        Objects.requireNonNull(name, Required.NAME);
        expectValue(name, null);
    }
    
    public void expectValue(String name, String message) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (StringUtils.isBlank(StringUtils.trimToNull(value))) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.REQUIRED_KEY, name)));
        }
    }

    public void expectFile(String name, String message) {
        Objects.requireNonNull(name, Required.NAME);
        byte[] bytes = files().get(name);

        if (bytes == null || bytes.length == 0) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.FILE_KEY, name)));
        }
    }

    public void expectFile(String name) {
        Objects.requireNonNull(name, Required.NAME);
        expectFile(name, null);
    }

    public void expectMinValue(String name, double minValue) {
        Objects.requireNonNull(name, Required.NAME);
        expectMinValue(name, minValue, null);
    }
    
    public void expectMinValue(String name, double minValue, String message) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (StringUtils.isNumeric(value)) {
            if (Double.parseDouble(value) < minValue) {
                addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.MIN_VALUE_KEY, name, minValue)));
            }
        } else {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.MIN_VALUE_KEY, name, minValue)));
        }
    }

    public void expectMinLength(String name, double minLength) {
        Objects.requireNonNull(name, Required.NAME);
        expectMinLength(name, minLength, null);
    }
    
    public void expectMinLength(String name, double minLength, String message) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (value.length() < minLength) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.MIN_LENGTH_KEY, name, minLength)));
        }
    }
    
    public void expectMaxValue(String name, double maxValue) {
        Objects.requireNonNull(name, Required.NAME);
        expectMaxValue(name, maxValue, null);
    }
    
    public void expectMaxLength(String name, double maxLength) {
        Objects.requireNonNull(name, Required.NAME);
        expectMaxLength(name, maxLength, null);
    }
    
    public void expectNumeric(String name) {
        Objects.requireNonNull(name, Required.NAME);
        expectNumeric(name, null);
    }

    public void expectNumeric(String name, String message) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!StringUtils.isNumeric(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.NUMERIC_KEY, name)));
        }
    }
    
    public void expectMaxLength(String name, double maxLength, String message) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (value.length() > maxLength) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.MAX_LENGTH_KEY, name, maxLength)));
        }
    }

    public void expectMaxValue(String name, double maxValue, String message) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (StringUtils.isNumeric(value)) {
            if (Double.parseDouble(value) > maxValue) {
                addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.MAX_VALUE_KEY, name, maxValue)));
            }
        } else {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.MAX_VALUE_KEY, name, maxValue)));
        }
    }

    /**
     * Case-sensitive; also fails if both fields are blank.
     */
    public void expectExactMatch(String name, String anotherName) {
        Objects.requireNonNull(name, Required.NAME);
        expectExactMatch(name, anotherName, null);
    }

    /**
     * Case-sensitive; also fails if both fields are blank.
     */
    public void expectExactMatch(String name, String anotherName, String message) {
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);
        String anotherValue = Optional.ofNullable(get(anotherName)).orElse(Strings.EMPTY);

        if (( StringUtils.isBlank(value) && StringUtils.isBlank(anotherValue) ) || ( StringUtils.isNotBlank(value) && !value.equals(anotherValue) )) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.EXACT_MATCH_KEY, name, anotherName)));
        }
    }

    /**
     * Case-insensitive; also fails if both fields are blank.
     */
    public void expectMatch(String name, String anotherName) {
        expectMatch(name, anotherName, messages().get(Validation.MATCH_KEY, name, anotherName));
    }
    
    /**
     * Case-insensitive; also fails if both fields are blank.
     */
    public void expectMatch(String name, String anotherName, String message) {
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);
        String anotherValue = Optional.ofNullable(get(anotherName)).orElse(Strings.EMPTY);

        if (( StringUtils.isBlank(value) && StringUtils.isBlank(anotherValue) ) || ( StringUtils.isNotBlank(value) && !value.equalsIgnoreCase(anotherValue.toLowerCase()))) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.MATCH_KEY, name, anotherName)));
        }
    }

    /**
     * Case-sensitive, unlike {@link #expectMatch(String, String)}.
     */
    public void expectMatch(String name, List<String> values) {
        Objects.requireNonNull(name, Required.NAME);
        expectMatch(name, messages().get(Validation.MATCH_VALUES_KEY, name), values);
    }

    public void expectMatch(String name, String message, List<String> values) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!(values).contains(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.MATCH_VALUES_KEY, name)));
        }
    }

    public void expectEmail(String name) {
        Objects.requireNonNull(name, Required.NAME);
        expectEmail(name, null);
    }

    public void expectEmail(String name, String message) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!EmailValidator.getInstance().isValid(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.EMAIL_KEY, name)));
        }
    }

    public void expectIpv4(String name) {
        Objects.requireNonNull(name, Required.NAME);
        expectIpv4(name, null);
    }

    public void expectIpv4(String name, String message) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!InetAddressValidator.getInstance().isValidInet4Address(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.IPV4_KEY, name)));
        }
    }
    
    public void expectDomainName(String name) {
        Objects.requireNonNull(name, Required.NAME);
        expectDomainName(name, null);
    }

    public void expectDomainName(String name, String message) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!DomainValidator.getInstance().isValid(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.DOMAIN_NAME_KEY, name)));
        }
    }

    public void expectIpv6(String name) {
        Objects.requireNonNull(name, Required.NAME);
        expectIpv6(name, null);
    }

    public void expectIpv6(String name, String message) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!InetAddressValidator.getInstance().isValidInet6Address(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.IPV6_KEY, name)));
        }
    }
    
    public void expectRangeLength(String name, int minLength, int maxLength) {
        Objects.requireNonNull(name, Required.NAME);
        expectRangeLength(name, minLength, maxLength, null);
    }
    
    public void expectRangeValue(String name, int minValue, int maxValue) {
        Objects.requireNonNull(name, Required.NAME);
        expectRangeValue(name, minValue, maxValue, null);
    } 
    
    public void expectRangeValue(String name, int minValue, int maxValue, String message) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (StringUtils.isNumeric(value)) {
            var doubleValue = Double.parseDouble(value);
            if (doubleValue < minValue || doubleValue > maxValue) {
                addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.RANGE_VALUE_KEY, name, minValue, maxValue)));
            }
        } else {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.RANGE_VALUE_KEY, name, minValue, maxValue)));
        }
    }
    
    public void expectRangeLength(String name, int minLength, int maxLength, String message) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (value.length() < minLength || value.length() > maxLength) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.RANGE_LENGTH_KEY, name, minLength, maxLength)));
        }
    }

    public void expectRegex(String name, Pattern pattern) {
        Objects.requireNonNull(name, Required.NAME);
        expectRegex(name, pattern, null);
    }

    public void expectRegex(String name, Pattern pattern, String message) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!pattern.matcher(value).matches()) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.REGEX_KEY, name)));
        }
    }

    public void expectUrl(String name) {
        Objects.requireNonNull(name, Required.NAME);
        expectUrl(name, null);
    }

    public void expectUrl(String name, String message) {
        Objects.requireNonNull(name, Required.NAME);
        String value = Optional.ofNullable(get(name)).orElse(Strings.EMPTY);

        if (!UrlValidator.getInstance().isValid(value)) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.URL_KEY, name)));
        }
    }

    public void expectTrue(String name, boolean value, String message) {
        Objects.requireNonNull(name, Required.NAME);
        if (!value) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.TRUE_KEY, name)));
        }
    }
    
    public void expectTrue(String name, boolean value) {
        Objects.requireNonNull(name, Required.NAME);
        expectTrue(name, value, null);
    }
    
    public void expectFalse(String name, boolean value, String message) {
        Objects.requireNonNull(name, Required.NAME);
        if (value) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.FALSE_KEY, name)));
        }
    }
    
    public void expectFalse(String name, boolean value) {
        Objects.requireNonNull(name, Required.NAME);
        expectFalse(name, value, null);
    }
    
    public void expectNotNull(String name, Object object, String message) {
        Objects.requireNonNull(name, Required.NAME);
        if (object == null) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.NOTNULL_KEY, name)));
        }
    }
    
    public void expectNotNull(String name, Object object) {
        Objects.requireNonNull(name, Required.NAME);
        expectNotNull(name, object, null);
    }
    
    public void expectNull(String name, Object object, String message) {
        Objects.requireNonNull(name, Required.NAME);
        if (object != null) {
            addError(name, Optional.ofNullable(message).orElse(messages().get(Validation.NULL_KEY, name)));
        }
    }
    
    public void expectNull(String name, Object object) {
        Objects.requireNonNull(name, Required.NAME);
        expectNull(name, object, messages().get(Validation.NULL_KEY, name));
    }
    
    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public String get(String name) {
        Objects.requireNonNull(name, Required.NAME);

        return values.get(name);
    }

    private void addError(String name, String message) {
        Objects.requireNonNull(name, Required.NAME);
        Objects.requireNonNull(message, Required.MESSAGE);

        errors.computeIfAbsent(name, k -> message);
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }
    
    public void addValue(String key, String value) {
        values.put(key, value);
    }
    
    public boolean isValid() {
        return !hasErrors();
    }
    
    public void invalidate() {
        errors.put(Strings.EMPTY, Strings.EMPTY);
    }
}