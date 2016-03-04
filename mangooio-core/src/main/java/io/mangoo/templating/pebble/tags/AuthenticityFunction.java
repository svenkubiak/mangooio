package io.mangoo.templating.pebble.tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.mitchellbosecke.pebble.extension.Function;

/**
 * 
 * @author svenkubiak
 *
 */
public class AuthenticityFunction implements Function {
    private final List<String> argumentNames = new ArrayList<>();

    public AuthenticityFunction() {
        argumentNames.add("type");
    }
    
    @Override
    public List<String> getArgumentNames() {
        return argumentNames;
    }

    @Override
    public Object execute(Map<String, Object> arguments) {
        String value = "";
        if (arguments != null) {
            String type = (String) arguments.get("type");
            if (StringUtils.isNotEmpty(type)) {
                if (type.equalsIgnoreCase("form")) {

                } else if (type.equalsIgnoreCase("token")) {
                    
                }
            }
        }
        
        
        return value;
    }

}