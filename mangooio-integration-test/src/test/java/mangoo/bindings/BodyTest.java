package mangoo.bindings;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;

import mangoo.io.routing.bindings.Body;

public class BodyTest {
    public static String content = "{\"phonetype\":\"iPhone\",\"cat\":\"good\"}";

    @Test
    public void testBody() {
        Body body = new Body();
        body.setContent(content);

        assertEquals(content, body.asString());

        Map<String, Object> json = body.asJson();
        assertEquals("iPhone", json.get("phonetype"));
        assertEquals("good", json.get("cat"));
    }
}