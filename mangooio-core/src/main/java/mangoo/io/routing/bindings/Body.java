package mangoo.io.routing.bindings;

import java.util.Map;

import org.boon.json.JsonFactory;

/**
 *
 * @author svenkubiak
 *
 */
@SuppressWarnings("unchecked")
public class Body {
    private String content;

    public void setContent(String content) {
        this.content = content;
    }

    public String asString() {
        return this.content;
    }

    public Map<String, Object> asJson() {
        return JsonFactory.create().readValue(this.content, Map.class);
    }
}