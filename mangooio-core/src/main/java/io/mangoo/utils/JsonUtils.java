package io.mangoo.utils;

import java.util.Objects;

import io.advantageous.boon.json.JsonFactory;
import io.advantageous.boon.json.JsonSerializer;
import io.advantageous.boon.json.JsonSerializerFactory;
import io.advantageous.boon.json.ObjectMapper;

/**
 * 
 * @author svenkubiak
 *
 */
public final class JsonUtils {
    private static volatile JsonSerializer jsonSerializer = createJsonSerializer();
    private static ObjectMapper objectMapper = JsonFactory.create();
    
    private JsonUtils(){
    }
    
    private static JsonSerializer createJsonSerializer() {
        JsonSerializerFactory jsonSerializerFactory = new JsonSerializerFactory();
        jsonSerializerFactory.useAnnotations();
        return jsonSerializerFactory.create();
    }
    
    /**
     * Sets a new custom JsonSerializer
     * 
     * @param serializer The JsonSerializer to set
     */
    public static void withJsonSerializer(JsonSerializer serializer) {
        jsonSerializer = serializer;
    }
    
    /**
     * Converts a given object to a Json string
     * 
     * @param object The object to convert
     * @return json string
     */
    public static String toJson(Object object) {
        Objects.requireNonNull(object, "object can not be null");
        
        return jsonSerializer.serialize(object).toString();
    }
    
    /**
     * Converts a given Json string to an object
     * 
     * @param json The json string to convert
     * @return The converted object
     */
    public static Object fromJson(String json) {
        Objects.requireNonNull(json, "json can not be null");
        
        return objectMapper.fromJson(json);
    }
    
    /**
     * Converts a given Json string to given Class
     * 
     * @param json The json string to convert
     * @param clazz The Class to convert to
     * @param <T> JavaDoc wants this, just ignore it
     * 
     * @return The converted class
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        Objects.requireNonNull(json, "json can not be null");
        Objects.requireNonNull(clazz, "clazz can not be null");
        
        return objectMapper.fromJson(json, clazz);
    }
}