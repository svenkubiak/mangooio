package io.mangoo.utils;

import java.io.IOException;
import java.util.Objects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.ReadContext;

import io.mangoo.enums.Required;

/**
 * 
 * @author svenkubiak
 *
 */
public final class JsonUtils {
    private static final Logger LOG = LogManager.getLogger(JsonUtils.class);
    private static ObjectMapper mapper = new ObjectMapper();
    static {
        mapper.setSerializationInclusion(Include.NON_NULL);
    }
    
    private JsonUtils(){
    }
    
    /**
     * @return Default object mapper
     */
    public static ObjectMapper getObjectMapper() {
        return mapper;
    }
    
    /**
     * Converts a given object to a Json string
     * 
     * @param object The object to convert
     * @return json string or null if conversion fails
     */
    public static String toJson(Object object) {
        Objects.requireNonNull(object, Required.OBJECT.toString());
        
        String json = null;
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
        
        String json = null;
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            LOG.error("Failed to convert object to json",  e);
        }
        
        return json;
    }
    
    /**
     * Converts a given Json string to an JSONPath ReadContext
     * 
     * @param json The json string to convert
     * @return JSPNPath read context
     */
    public static ReadContext fromJson(String json) {
        Objects.requireNonNull(json, Required.JSON.toString());
        
        return JsonPath.parse(json);
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