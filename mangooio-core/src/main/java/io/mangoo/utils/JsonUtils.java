package io.mangoo.utils;

import java.util.Collection;
import java.util.Objects;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

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
    private static final String JSON_CAN_NOT_BE_NULL = "json can not be null";
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
     * Converts a given Json string to an JSONPath ReadContext
     * 
     * @param json The json string to convert
     * @return JSPNPath read context
     */
    public static ReadContext fromJson(String json) {
        Objects.requireNonNull(json, JSON_CAN_NOT_BE_NULL);
        
        return JsonPath.parse(json);
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
        Objects.requireNonNull(json, JSON_CAN_NOT_BE_NULL);
        Objects.requireNonNull(clazz, "clazz can not be null");
        
        return objectMapper.fromJson(json, clazz);
    }

    /**
     * Converts a given Json string to a collection of a given Class
     * 
     * @param json The json string to convert
     * @param componentType The collection type to convert to
     * @param clazz The Class to convert to
     * 
     * @param <T> JavaDoc wants this, just ignore it
     * 
     * @return A collection of converted classes
     */
    public static <T extends Collection<C>, C> T fromJson(String json, Class<C> componentType, Class<T> clazz) {
        Objects.requireNonNull(json, JSON_CAN_NOT_BE_NULL);
        Objects.requireNonNull(clazz, "clazz can not be null");
        Objects.requireNonNull(componentType, "componentType can not be null");
        
        return objectMapper.readValue(json, clazz, componentType);
    }
}