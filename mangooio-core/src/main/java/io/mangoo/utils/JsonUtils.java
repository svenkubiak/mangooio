package io.mangoo.utils;

import java.util.Objects;

import org.boon.json.JsonFactory;
import org.boon.json.JsonSerializer;
import org.boon.json.JsonSerializerFactory;
import org.boon.json.ObjectMapper;

/**
 * 
 * @author svenkubiak
 *
 */
public class JsonUtils {
    private static JsonSerializer jsonSerializer = createJsonSerializer();
    private static ObjectMapper objectMapper = JsonFactory.create();
    
    private static JsonSerializer createJsonSerializer() {
        JsonSerializerFactory jsonSerializerFactory = new JsonSerializerFactory();
        jsonSerializerFactory.useAnnotations();
        return jsonSerializerFactory.create();
    }
    
    public static void withJsonSerializer(JsonSerializer serializer) {
        jsonSerializer = serializer;
    }
    
    public static String toJson(Object object) {
        Objects.requireNonNull(object, "object can not be null");
        
        return jsonSerializer.serialize(object).toString();
    }
    
    public static Object fromJson(String json) {
        Objects.requireNonNull(json, "json can not be null");
        
        return objectMapper.fromJson(json);
    }
    
    public static <T> T fromJson(String json, Class<T> clazz) {
        Objects.requireNonNull(json, "json can not be null");
        Objects.requireNonNull(clazz, "clazz can not be null");
        
        return objectMapper.fromJson(json, clazz);
    }
}