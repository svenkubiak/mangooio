package io.mangoo.routing.bindings;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author svenkubiak
 *
 */
public class Payload {
    private Map<String, Object> content = new HashMap<String, Object>();

    public void addContent(String key, Object value) {
        this.content.put(key, value);
    }

    public Map<String, Object> getContent() {
        return this.content;
    }
}