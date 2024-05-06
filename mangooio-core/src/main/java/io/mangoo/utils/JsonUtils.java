package io.mangoo.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import io.mangoo.constants.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

public final class JsonUtils {
    private static final Logger LOG = LogManager.getLogger(JsonUtils.class);
    private static final ObjectMapper MAPPER = JsonMapper.builder()
            .findAndAddModules()
            .addModule(new AfterburnerModule())
            .addModule(new JavaTimeModule())
            .build();
    
    static {
        MAPPER.setSerializationInclusion(Include.NON_NULL);
    }
    
    private JsonUtils(){
    }
    
    /**
     * Converts a given object to a Json string
     * 
     * @param object The object to convert
     * @return json string or null if conversion fails
     */
    public static String toJson(Object object) {
        Objects.requireNonNull(object, NotNull.OBJECT);
        
        String json = Strings.EMPTY;
        try {
            json = MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to convert object to json",  e);
        }
        
        return json;
    }
    
    /**
     * Converts a given object to a Json string
     * 
     * @param object The object to convert
     * @return json string or null if conversion fails
     */
    public static String toPrettyJson(Object object) {
        Objects.requireNonNull(object, NotNull.OBJECT);
        
        var json = Strings.EMPTY;
        try {
            json = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to convert object to json",  e);
        }
        
        return json;
    }

    /**
     * Converts a given Json string to given Class
     * @deprecated
     * Use {@link #toObject(String, Class)} instead
     *
     * @param json The json string to convert
     * @param clazz The Class to convert to
     * @param <T> JavaDoc wants this, just ignore it
     * 
     * @return The converted class or null if conversion fails
     */
    @Deprecated(since = "8.5.0", forRemoval = true)
    public static <T> T fromJson(String json, Class<T> clazz) {
        Objects.requireNonNull(json, NotNull.JSON);
        Objects.requireNonNull(clazz, NotNull.CLASS);

        T object = null;
        try {
            object = MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            LOG.error("Failed to convert json to object class",  e);
        }
    
        return object;
    }

    /**
     * Converts a given Json string to a given Class
     *
     * @param json The json string to convert
     * @param clazz The Class to convert to
     * @param <T> JavaDoc wants this, just ignore it
     *
     * @return The converted class or null if conversion fails
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        Objects.requireNonNull(json, NotNull.JSON);
        Objects.requireNonNull(clazz, NotNull.CLASS);

        T object = null;
        try {
            object = MAPPER.readValue(json, clazz);
        } catch (IOException e) {
            LOG.error("Failed to convert json to object class",  e);
        }

        return object;
    }

    /**
     * Converts a given Json to a map while fattening all keys, e.g
     * one.two.three
     *
     * @param json The json string to convert
     * @return A flat map containing the json data
     */
    public static Map<String, String> toFlatMap(String json) {
        Objects.requireNonNull(json, NotNull.JSON);

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
            Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
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