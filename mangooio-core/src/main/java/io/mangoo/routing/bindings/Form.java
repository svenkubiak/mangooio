package io.mangoo.routing.bindings;

import io.mangoo.constants.Required;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

public class Form extends Validator {
    @Serial
    private static final long serialVersionUID = 2228639200039277653L;
    private boolean submitted;
    private boolean keep;
    
    public Form() {
        // Empty constructor for Google Guice
    }
    
    public Optional<String> getString(String key) {
        Objects.requireNonNull(key, Required.KEY);

        String value = values.get(key);
        if (StringUtils.isNotBlank(value)) {
            return Optional.of(value);
        }

        return Optional.empty();
    }
    
    public String getValue(String key) {
        Objects.requireNonNull(key, Required.KEY);

        String value = values.get(key);
        if (StringUtils.isNotBlank(value)) {
            return value;
        }

        return "";
    }

    @SuppressWarnings("fb-contrib:BL_BURYING_LOGIC")
    public Optional<Boolean> getBoolean(String key) {
        Objects.requireNonNull(key, Required.KEY);

        String value = values.get(key);
        if (StringUtils.isNotBlank(value)) {
            return switch (value) {
                case "1", "true" -> Optional.of(Boolean.TRUE);
                case "0", "false" -> Optional.of(Boolean.FALSE);
                default -> Optional.empty();
            };
        }

        return Optional.empty();
    }

    public Optional<Integer> getInteger(String key) {
        return parse(key, Integer::valueOf);
    }

    public Optional<Double> getDouble(String key) {
        return parse(key, Double::valueOf).filter(Double::isFinite);
    }

    public Optional<Float> getFloat(String key) {
        return parse(key, Float::valueOf).filter(Float::isFinite);
    }

    public Optional<Long> getLong(String key) {
        return parse(key, Long::valueOf);
    }

    // Parsing decides whether the value fits the target type, a value it rejects is treated as absent
    private <T> Optional<T> parse(String key, Function<String, T> parser) {
        Objects.requireNonNull(key, Required.KEY);

        String value = values.get(key);
        if (StringUtils.isBlank(value)) {
            return Optional.empty();
        }

        try {
            return Optional.of(parser.apply(value));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public Optional<byte[]> getFile(String key) {
        Objects.requireNonNull(key, Required.KEY);

        return Optional.ofNullable(files().get(key));
    }

    public Map<String, String> getValues() {
        return values;
    }
    
    public void addFile(String key, InputStream inputStream) throws IOException {
        Objects.requireNonNull(key, Required.KEY);
        Objects.requireNonNull(inputStream, Required.INPUT_STREAM);

        try (var in = inputStream) {
            files().put(key, in.readAllBytes());
        }
    }
 
    /**
     * Keeps the submitted values and validation errors in the flash scope for the next request; uploaded files are never kept.
     */
    public void keep() {
        keep = true;
    }

    public boolean isKept() {
        return keep;
    }
    
    public void discard() {
        if (files != null) {
            files.clear();
        }
        files = new HashMap<>();

        if (values != null) {
            values.clear();
        }
        values = new HashMap<>();
    }
    
    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }
}