package io.mangoo.utils;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.module.afterburner.AfterburnerModule;
import io.mangoo.enums.Required;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Objects;

public final class JsonUtils {
    private static final Logger LOG = LogManager.getLogger(JsonUtils.class);
    private static final ObjectMapper mapper = JsonMapper.builder()
            .addModule(new AfterburnerModule())
            .build();
    
    static {
        mapper.setSerializationInclusion(Include.NON_NULL);
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
        Objects.requireNonNull(object, Required.OBJECT.toString());
        
        String json = "";
        try {
            json = mapper.writeValueAsString(object);
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
        Objects.requireNonNull(object, Required.OBJECT.toString());
        
        String json = "";
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to convert object to json",  e);
        }
        
        return json;
    }

    /**
     * Converts a given Json string to given Class
     * 
     * @param json The json string to convert
     * @param clazz The Class to convert to
     * @param <T> JavaDoc wants this, just ignore it
     * 
     * @return The converted class or null if conversion fails
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        Objects.requireNonNull(json, Required.JSON.toString());
        Objects.requireNonNull(clazz, Required.CLASS.toString());
        
        T object = null;
        try {
            object = mapper.readValue(json, clazz);
        } catch (IOException e) {
            LOG.error("Failed to convert json to object class",  e);
        }
    
        return object;
    }
}