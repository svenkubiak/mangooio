package io.mangoo.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.mangoo.constants.Required;
import io.mangoo.core.Application;
import io.mangoo.core.Config;
import io.mangoo.routing.bindings.UnprocessableContent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.cfg.DateTimeFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.ValueNode;
import tools.jackson.module.blackbird.BlackbirdModule;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class JsonUtils {
    private static final Logger LOG = LogManager.getLogger(JsonUtils.class);
    private static volatile ObjectMapper mapper;

    private JsonUtils(){
    }

    /**
     * Returns an empty string if the conversion fails.
     */
    public static String toJson(Object object) {
        Objects.requireNonNull(object, Required.OBJECT);
        
        String json = Strings.EMPTY;
        try {
            json = mapper().writeValueAsString(object);
        } catch (JacksonException e) {
            // A failed conversion returns an empty string
        }
        
        return json;
    }
    
    /**
     * Returns an empty string if the conversion fails.
     */
    public static String toPrettyJson(Object object) {
        Objects.requireNonNull(object, Required.OBJECT);
        
        var json = Strings.EMPTY;
        try {
            json = mapper().writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JacksonException e) {
            // A failed conversion returns an empty string
        }
        
        return json;
    }

    /**
     * Returns null if the conversion fails.
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        Argument.requireNonBlank(json, Required.JSON);
        Objects.requireNonNull(clazz, Required.CLASS);

        T object = null;
        try {
            object = mapper().readValue(json, clazz);
        } catch (JacksonException e) {
            // A failed conversion returns null
        }

        return object;
    }

    /**
     * Falls back to a new instance of the class, or to UnprocessableContent if that fails too.
     */
    @SuppressWarnings("unchecked")
    public static <T> T toObjectWithFallback(String json, Class<T> clazz) {
        Argument.requireNonBlank(json, Required.JSON);
        Objects.requireNonNull(clazz, Required.CLASS);

        T object = null;
        try {
            object = mapper().readValue(json, clazz);
        } catch (JacksonException e) {
            // Fall back to a new instance below
        }

        try {
            return (object != null) ? object : clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            // Fall back to UnprocessableContent below
        }

        return (T) new UnprocessableContent();
    }

    /**
     * Flattens nested keys, e.g. one.two.three; returns an empty map if the JSON is invalid.
     */
    public static Map<String, String> toFlatMap(String json) {
        Argument.requireNonBlank(json, Required.JSON);

        Map<String, String> map = new HashMap<>();
        try {
            addKeys(Strings.EMPTY, mapper().readTree(json), map);
        } catch (Exception e) {
            // Invalid JSON returns an empty map
        }

        return map;
    }

    public static ObjectMapper getMapper() {
        return mapper();
    }

    private static ObjectMapper mapper() {
        var current = mapper;
        if (current == null) {
            synchronized (JsonUtils.class) {
                current = mapper;
                if (current == null) {
                    mapper = current = buildMapper();
                }
            }
        }
        return current;
    }

    /**
     * Built on first use, after Module has applied application.timezone.
     * configureForJackson2() keeps the previous defaults. Unknown properties stay
     * ignored and dates stay ISO-8601 strings, as before.
     */
    private static ObjectMapper buildMapper() {
        return JsonMapper.builder()
                .addModule(new BlackbirdModule())
                .defaultTimeZone(applicationTimeZone())
                .changeDefaultPropertyInclusion(inclusion -> inclusion
                        .withValueInclusion(JsonInclude.Include.NON_NULL)
                        .withContentInclusion(JsonInclude.Include.ALWAYS))
                .configureForJackson2()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .disable(DateTimeFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build();
    }

    private static TimeZone applicationTimeZone() {
        var injector = Application.getInjector();
        if (injector != null) {
            return TimeZone.getTimeZone(injector.getInstance(Config.class).getApplicationTimeZone());
        }
        return TimeZone.getDefault();
    }

    private static void addKeys(String currentPath, JsonNode jsonNode, Map<String, String> map) {
        if (jsonNode.isObject()) {
            var objectNode = (ObjectNode) jsonNode;
            Iterator<Map.Entry<String, JsonNode>> iter = objectNode.properties().iterator();
            String pathPrefix = currentPath.isEmpty() ? Strings.EMPTY : currentPath + ".";

            while (iter.hasNext()) {
                Map.Entry<String, JsonNode> entry = iter.next();
                addKeys(pathPrefix + entry.getKey(), entry.getValue(), map);
            }
        } else if (jsonNode.isArray()) {
            var arrayNode = (ArrayNode) jsonNode;
            for (var i = 0; i < arrayNode.size(); i++) {
                addKeys(currentPath + "[" + i + "]", arrayNode.get(i), map);
            }
        } else if (jsonNode.isValueNode()) {
            var valueNode = (ValueNode) jsonNode;
            map.put(currentPath, valueNode.isNull() ? "null" : valueNode.asString());
        }
    }
}
