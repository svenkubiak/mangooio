package io.mangoo.templating.pebble;

import java.util.HashMap;
import java.util.Map;

import com.mitchellbosecke.pebble.extension.AbstractExtension;
import com.mitchellbosecke.pebble.extension.Function;

import io.mangoo.templating.pebble.tags.AuthenticityFunction;

public class TemplateEnginePebbleExtension extends AbstractExtension {

    @Override
    public Map<String, Function> getFunctions() {
        Map<String, Function> functions = new HashMap<>();
        functions.put("authenticity", new AuthenticityFunction());
        return functions;
    }
}
