package io.mangoo.routing.bindings;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.inject.Inject;

import io.mangoo.interfaces.MangooValidator;

/**
 *
 * @author svenkubiak
 *
 */
public class Form implements MangooValidator {
    private boolean submitted;
    private List<File> files = new ArrayList<File>();
    private Map<String, String> values = new HashMap<String, String>();
    private Validator validator;

    @Inject
    public Form (Validator validator) {
        this.validator = Objects.requireNonNull(validator, "Validator can not be null");
    }

    @Override
    public Validator validation() {
        return this.validator;
    }

    @Override
    public String getError(String fieldName) {
        return this.validator.hasError(fieldName) ? this.validator.getError(fieldName) : "";
    }

    public String get(String key) {
        return this.values.get(key);
    }

    public List<File> getFiles() {
        return this.files;
    }

    public File getFile() {
        File file = null;
        if (!this.files.isEmpty()) {
            file = this.files.get(0);
        }

        return file;
    }

    public void addFile(File file) {
        this.files.add(file);
    }

    public String getValue(String fieldName) {
        return this.values.get(fieldName);
    }

    public Map<String, String> getValues() {
        return this.values;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public void addValue(String key, String value) {
        this.values.put(key, value);
        this.validator.add(key, value);
    }
}