package io.mangoo.maven;

import io.mangoo.constants.Default;
import io.mangoo.constants.Key;
import io.mangoo.crypto.Argon2Settings;
import io.mangoo.utils.internal.MangooUtils;
import org.apache.commons.lang3.StringUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

final class HashingSettings {
    private HashingSettings() {
    }

    @SuppressWarnings("unchecked")
    static Argon2Settings read(InputStream input, String mode) {
        var loaderOptions = new LoaderOptions();
        loaderOptions.setAllowDuplicateKeys(false);
        loaderOptions.setMaxAliasesForCollections(5);

        Object loaded = new Yaml(loaderOptions).load(input);
        if (!(loaded instanceof Map<?, ?> root)) {
            throw new IllegalArgumentException("config.yaml has no configuration");
        }

        Object defaults = root.get("default");
        Object environments = root.get("environments");
        if (!(defaults instanceof Map<?, ?> defaultConfig) || !(environments instanceof Map<?, ?> environmentConfig)) {
            throw new IllegalArgumentException("config.yaml must contain default and environments");
        }

        Object active = environmentConfig.get(mode);
        if (!(active instanceof Map<?, ?> activeEnvironment)) {
            throw new IllegalArgumentException("Active environment '" + mode + "' not found in config.yaml");
        }

        Map<String, Object> merged = new HashMap<>((Map<String, Object>) defaultConfig);
        MangooUtils.mergeMaps(merged, (Map<String, Object>) activeEnvironment);

        Map<String, String> values = MangooUtils.flattenMap(merged);
        return new Argon2Settings(
                number(values, Key.AUTHENTICATION_HASHING_MEMORY, Default.AUTHENTICATION_HASHING_MEMORY),
                number(values, Key.AUTHENTICATION_HASHING_ITERATIONS, Default.AUTHENTICATION_HASHING_ITERATIONS),
                number(values, Key.AUTHENTICATION_HASHING_PARALLELISM, Default.AUTHENTICATION_HASHING_PARALLELISM))
                .requireNotWeakerThanMinimum();
    }

    static String cleartext(char[] password, char[] repeated) {
        if (password == null || password.length == 0) {
            throw new IllegalArgumentException("Password can not be empty");
        }
        if (repeated == null || !Arrays.equals(password, repeated)) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        return new String(password);
    }

    private static int number(Map<String, String> values, String key, int fallback) {
        String value = values.get(key);
        if (StringUtils.isBlank(value)) {
            return fallback;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(key + " must be a number, but is " + value, e);
        }
    }
}
