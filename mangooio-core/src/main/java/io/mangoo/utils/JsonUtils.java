package io.mangoo.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.blackbird.BlackbirdModule;
import io.mangoo.constants.Required;
import io.mangoo.routing.bindings.UnprocessableContent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public final class JsonUtils {
    private static final Logger LOG = LogManager.getLogger(JsonUtils.class);
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .build()
            .registerModule(new JavaTimeModule())
            .registerModule(new BlackbirdModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setDefaultPropertyInclusion(JsonInclude.Value.construct(
                    JsonInclude.Include.NON_NULL,
                    JsonInclude.Include.ALWAYS))
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private JsonUtils(){
    }
    
    /**
     * Converts a given object to a JSON string
     * 
     * @param object The object to convert
     * @return JSON string or null if conversion fails
     */
    public static String toJson(Object object) {
        Objects.requireNonNull(object, Required.OBJECT);
        
        String json = Strings.EMPTY;
        try {
            json = MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to convert object to json",  e);
        }
        
        return json;
    }
    
    /**
     * Converts a given object to a JSON string
     * 
     * @param object The object to convert
     * @return JSON string or null if conversion fails
     */
    public static String toPrettyJson(Object object) {
        Objects.requireNonNull(object, Required.OBJECT);
        
        var json = Strings.EMPTY;
        try {
            json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to convert object to json",  e);
        }
        
        return json;
    }

    /**
     * Converts a given JSON string to a given Class
     *
     * @param json The JSON string to convert
     * @param clazz The Class to convert to
     * @param <T> Javadoc wants this, just ignore it
     *
     * @return The converted class or null if conversion fails
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        Argument.requireNonBlank(json, Required.JSON);
        Objects.requireNonNull(clazz, Required.CLASS);

        T object = null;
        try {
            object = MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            LOG.error("Failed to convert json to object class",  e);
        }

        return object;
    }

    /**
     * Converts a given JSON string to a given Class and
     * tries to create an "empty" instance of the class if conversion fails
     *
     * @param json The JSON string to convert
     * @param clazz The Class to convert to
     * @param <T> Javadoc wants this, just ignore it
     *
     * @return The converted class or an instance of UnprocessableContent if conversion and fallback fails
     */
    @SuppressWarnings("unchecked")
    public static <T> T toObjectWithFallback(String json, Class<T> clazz) {
        Argument.requireNonBlank(json, Required.JSON);
        Objects.requireNonNull(clazz, Required.CLASS);

        T object = null;
        try {
            object = MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            //Intentionally left blank
        }

        try {
            return (object != null) ? object : clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            //Intentionally left blank
        }

        return (T) new UnprocessableContent();
    }

    /**
     * Converts a given JSON to a map while fattening all keys, e.g
     * one.two.three
     *
     * @param json The JSON string to convert
     * @return A flat map containing the JSON data
     */
    public static Map<String, String> toFlatMap(String json) {
        Argument.requireNonBlank(json, Required.JSON);

        Map<String, String> map = new HashMap<>();
        try {
            addKeys(Strings.EMPTY, new ObjectMapper().readTree(json), map);
        } catch (Exception e) {
            //Intentionally left blank
        }

        return map;
    }

    /**
     * @return The used Jackson ObjectMapper
     */
    public static ObjectMapper getMapper() {
        return MAPPER;
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
            map.put(currentPath, valueNode.asText());
        }
    }
}